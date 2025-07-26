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

package dev.patrickgold.jetpref.datastore.runtime

import dev.patrickgold.jetpref.datastore.consumeFirst
import dev.patrickgold.jetpref.datastore.model.PreferenceData
import dev.patrickgold.jetpref.datastore.model.PreferenceMigrationEntry
import dev.patrickgold.jetpref.datastore.model.PreferenceModel
import dev.patrickgold.jetpref.datastore.model.PreferenceType
import dev.patrickgold.jetpref.datastore.model.StringEncoder
import dev.patrickgold.jetpref.datastore.runCatchingCancellationAware
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import kotlin.collections.iterator
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

private typealias RawEncodedValues = MutableMap<PreferenceModel.TypedKey, String?>

private const val DELIMITER = ";"

class DataStore<T : PreferenceModel>(
    private val model: T,
): ReadOnlyProperty<Any?, T> {
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val eventQueue = Channel<Event>(Channel.UNLIMITED)
    private var currentStoreRef: Store? = null

    init {
        scope.launch {
            eventQueue.consumeEach { event ->
                val result = runCatchingCancellationAware {
                    handleEvent(event)
                }
                event.done.send(result)
            }
        }
    }

    private suspend fun handleEvent(event: Event) {
        val currentStore = currentStoreRef
        when (event) {
            is Event.Init -> {
                val reader = when (event.loadStrategy) {
                    is LoadStrategy.Disabled -> null
                    is LoadStrategy.UseReader -> event.loadStrategy.reader
                }
                val rawEncodedValues = loadAndUpdate(reader)
                currentStoreRef = Store(
                    loadStrategy = event.loadStrategy,
                    persistStrategy = event.persistStrategy,
                    rawEncodedValues = rawEncodedValues,
                )
            }
            is Event.SetValueAndTryPersist -> {
                requireNotNull(currentStore)
                require(currentStore.rawEncodedValues.contains(event.typedKey))
                currentStore.rawEncodedValues.put(event.typedKey, event.rawEncodedValue)
                when (currentStore.persistStrategy) {
                    is PersistStrategy.Disabled -> {}
                    is PersistStrategy.UseWriter -> {
                        persist(currentStore.persistStrategy.writer, currentStore.rawEncodedValues)
                    }
                }
            }
            is Event.Import -> {
                requireNotNull(currentStore)
                val rawEncodedValues = loadAndUpdate(event.importReader)
                currentStoreRef = currentStore.copy(rawEncodedValues = rawEncodedValues)
                when (currentStore.persistStrategy) {
                    is PersistStrategy.Disabled -> {}
                    is PersistStrategy.UseWriter -> {
                        persist(currentStore.persistStrategy.writer, rawEncodedValues)
                    }
                }
            }
            is Event.Export -> {
                requireNotNull(currentStore)
                persist(event.exportWriter, currentStore.rawEncodedValues)
            }
        }
    }

    suspend fun init(
        loadStrategy: LoadStrategy,
        persistStrategy: PersistStrategy,
    ): Result<Unit> {
        val event = Event.Init(
            loadStrategy = loadStrategy,
            persistStrategy = persistStrategy,
        )
        eventQueue.send(event)
        return event.done.consumeFirst()
    }

    // Delegate for getting the model with Kotlin's by syntax
    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return model
    }

    private suspend fun loadAndUpdate(reader: DataStoreReader?): RawEncodedValues {
        val rawEncodedValues: RawEncodedValues = model.declaredPreferenceEntries.mapValues { null }.toMutableMap()
        try {
            val rawDataStoreContent = reader?.read() ?: ""
            for (line in rawDataStoreContent.lines()) {
                if (line.isBlank()) continue
                val del1 = line.indexOf(DELIMITER)
                if (del1 < 0) continue
                var type = PreferenceType.from(line.substring(0, del1))
                val del2 = line.indexOf(DELIMITER, del1 + 1)
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
                val typedKey = PreferenceModel.TypedKey(type, key)
                if (rawEncodedValues.contains(typedKey)) {
                    rawEncodedValues.put(typedKey, rawEncodedValue)
                }
            }
        } finally {
            model.declaredPreferenceEntries.forEach { (typedKey, entry) ->
                val value = rawEncodedValues[typedKey]
                entry.init(value)
            }
        }
        return rawEncodedValues
    }

    private suspend fun <V : Any> PreferenceData<V>.init(rawEncodedValue: String?) {
        val value = rawEncodedValue?.let { rawEncodedValue ->
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
            val event = Event.SetValueAndTryPersist(
                typedKey = PreferenceModel.TypedKey(type, key),
                rawEncodedValue = rawEncodedValue,
            )
            eventQueue.send(event)
            event.done.consumeFirst()
        }
    }

    private suspend fun persist(writer: DataStoreWriter, rawEncodedValues: RawEncodedValues) {
        val rawDatastoreContent = buildString {
            for ((typedKey, rawEncodedValue) in rawEncodedValues) {
                if (rawEncodedValue == null) {
                    continue
                }
                append(typedKey.type)
                append(DELIMITER)
                append(typedKey.key)
                append(DELIMITER)
                append(rawEncodedValue)
                appendLine()
            }
        }
        writer.write(rawDatastoreContent)
    }

    private data class Store(
        val loadStrategy: LoadStrategy,
        val persistStrategy: PersistStrategy,
        val rawEncodedValues: RawEncodedValues,
    )

    private sealed class Event {
        val done: Channel<Result<Unit>> = Channel(Channel.CONFLATED)

        data class Init(
            val loadStrategy: LoadStrategy,
            val persistStrategy: PersistStrategy,
        ) : Event()

        data class SetValueAndTryPersist(
            val typedKey: PreferenceModel.TypedKey,
            val rawEncodedValue: String?,
        ) : Event()

        data class Import(
            val importReader: DataStoreReader,
        ) : Event()

        data class Export(
            val exportWriter: DataStoreWriter,
        ) : Event()
    }
}
