/*
 * Copyright 2025 Patrick Goldinger
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.patrickgold.jetpref.datastore

import dev.patrickgold.jetpref.datastore.model.PreferenceData
import dev.patrickgold.jetpref.datastore.model.PreferenceMigrationEntry
import dev.patrickgold.jetpref.datastore.model.PreferenceModel
import dev.patrickgold.jetpref.datastore.model.PreferenceType
import dev.patrickgold.jetpref.datastore.model.StringEncoder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

class JetPrefDataStore<T : PreferenceModel>(
    private val model: T,
): ReadOnlyProperty<Any?, T> {
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private val eventQueue = Channel<Event>(Channel.UNLIMITED)
    private var currentStoreRef: Store? = null

    init {
        scope.launch {
            eventQueue.consumeEach { event ->
                val result = runCatchingCancellationAware {
                    when (event) {
                        is Event.LoadFromStorage -> handleLoadFromStorage(event)
                        is Event.SetValueAndPersistToStorage -> handleSetValueAndPersistToStorage(event)
                    }
                }
                event.done.send(result)
            }
        }
    }

    suspend fun init(storageProvider: JetPrefStorageProvider, shouldPersist: Boolean = true): Result<Unit> {
        val store = Store(
            id = generateDataStoreId(),
            shouldPersist = shouldPersist,
            storageProvider = storageProvider,
            values = model.declaredPreferenceEntries.mapValues { (_, data) ->
                TypedRawEncodedValue(data.type, null)
            }.toMutableMap(),
        )
        val event = Event.LoadFromStorage(store)
        eventQueue.send(event)
        return event.done.consumeFirst()
    }

    // Delegate for getting the model with Kotlin's by syntax
    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return model
    }

    private suspend fun handleLoadFromStorage(event: Event.LoadFromStorage) {
        val currentStore = currentStoreRef
        val newStore = event.store
        try {
            require(currentStore == null || currentStore.id != newStore.id)
            val rawDataStoreContent = newStore.storageProvider.load().getOrThrow()
            for (line in rawDataStoreContent.lines()) {
                if (line.isBlank()) continue
                val del1 = line.indexOf(JetPref.DELIMITER)
                if (del1 < 0) continue
                var type = PreferenceType.from(line.substring(0, del1))
                val del2 = line.indexOf(JetPref.DELIMITER, del1 + 1)
                if (del2 < 0) continue
                var key = line.substring(del1 + 1, del2)
                var rawEncodedValue = if (del2 + 1 == line.length) "" else line.substring(del2 + 1)

                // Handle preference data migration
                val migrationResult = model.migrate(
                    PreferenceMigrationEntry(
                        action = PreferenceMigrationEntry.Action.KEEP_AS_IS,
                        type = type,
                        key = key,
                        rawValue = if (type.isString()) StringEncoder.decode(rawEncodedValue) else rawEncodedValue,
                    )
                )
                when (migrationResult.action) {
                    PreferenceMigrationEntry.Action.KEEP_AS_IS -> {
                        /* Do nothing and continue as no migration is needed */
                    }

                    PreferenceMigrationEntry.Action.RESET -> {
                        continue
                    }

                    PreferenceMigrationEntry.Action.TRANSFORM -> {
                        type = migrationResult.type
                        key = migrationResult.key
                        rawEncodedValue = if (type.isString()) {
                            StringEncoder.encode(migrationResult.rawValue)
                        } else {
                            migrationResult.rawValue
                        }
                    }
                }
                val cachedEntry = newStore.values[key]
                if (cachedEntry == null || cachedEntry.type != type) {
                    continue
                }
                newStore.values.put(key, cachedEntry.copy(rawEncodedValue = rawEncodedValue))
            }
        } finally {
            model.declaredPreferenceEntries.forEach { (key, entry) ->
                val value = requireNotNull(newStore.values[key]) {
                    buildString {
                        append("Key '$key'")
                        append(" for datastore '${newStore.storageProvider.datastoreName}")
                        append(" not found, should never happen!")
                    }
                }
                entry.init(value)
            }
            currentStoreRef = newStore
        }
    }

    private suspend fun <V : Any> PreferenceData<V>.init(typedRawEncodedValue: TypedRawEncodedValue) {
        require(type == typedRawEncodedValue.type)
        val value = typedRawEncodedValue.rawEncodedValue?.let { rawEncodedValue ->
            if (type.isString()) {
                serializer.deserialize(StringEncoder.decode(rawEncodedValue))
            } else {
                serializer.deserialize(rawEncodedValue)
            }
        }
        init(value) { newValue ->
            val rawEncodedValue = newValue?.let { newValue ->
                if (type.isString()) {
                    serializer.serialize(newValue)?.let { StringEncoder.encode(it) }
                } else {
                    serializer.serialize(newValue)
                }
            }
            val event = Event.SetValueAndPersistToStorage(
                key = key,
                rawEncodedValue = rawEncodedValue,
            )
            eventQueue.send(event)
            event.done.consumeFirst()
        }
    }

    private suspend fun handleSetValueAndPersistToStorage(event: Event.SetValueAndPersistToStorage) {
        val currentStore = currentStoreRef
        if (currentStore == null || !currentStore.shouldPersist) {
            return
        }
        val cachedEntry = requireNotNull(currentStore.values[event.key])
        currentStore.values.put(event.key, cachedEntry.copy(rawEncodedValue = event.rawEncodedValue))
        val rawDatastoreContent = buildString {
            for ((key, typedRawEncodedValue) in currentStore.values) {
                if (typedRawEncodedValue.rawEncodedValue == null) {
                    continue
                }
                append(typedRawEncodedValue.type)
                append(JetPref.DELIMITER)
                append(key)
                append(JetPref.DELIMITER)
                append(typedRawEncodedValue.rawEncodedValue)
                appendLine()
            }
        }
        currentStore.storageProvider.persist(rawDatastoreContent)
    }

    private data class Store(
        val id: Long,
        val shouldPersist: Boolean,
        val storageProvider: JetPrefStorageProvider,
        val values: MutableMap<String, TypedRawEncodedValue>,
    )

    private data class TypedRawEncodedValue(
        val type: PreferenceType,
        val rawEncodedValue: String?,
    )

    private sealed class Event {
        val done: Channel<Result<Unit>> = Channel(Channel.CONFLATED)

        data class LoadFromStorage(
            val store: Store,
        ) : Event()

        data class SetValueAndPersistToStorage(
            val key: String,
            val rawEncodedValue: String?,
        ) : Event()
    }
}

class JetPrefModelNotFoundException(
    modelQualifiedName: String,
    causedBy: Throwable,
) : Exception(
    "No model with qualified name '$modelQualifiedName' could be found",
    causedBy,
)

/**
 * Creates a preference model store and returns it.
 *
 * @param kClass The class of the preference model to create.
 *
 * @since 0.3.0
 */
@Throws(JetPrefModelNotFoundException::class)
expect fun <T : PreferenceModel> jetprefDataStoreOf(
    kClass: KClass<T>,
): JetPrefDataStore<T>

internal expect fun generateDataStoreId(): Long
