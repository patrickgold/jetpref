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
import kotlinx.coroutines.channels.consume
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicReference
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

class JetPrefDataStore<T : PreferenceModel>(
    private val model: T,
): ReadOnlyProperty<Any?, T> {
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private val eventQueue = Channel<Event>(Channel.UNLIMITED)
    private val currentStoreRef = AtomicReference<Store?>(null)

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

    suspend fun init(storageProvider: JetPrefStorageProvider, readOnly: Boolean = false): Result<Unit> {
        val store = Store(
            id = System.currentTimeMillis(),
            readOnly = readOnly,
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
        val currentStore = currentStoreRef.get()
        val newStore = event.store
        require(currentStore == null || currentStore.id != newStore.id)
        val rawDataStoreContent = withContext(Dispatchers.IO) {
            newStore.storageProvider.load().getOrThrow()
        }
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
            val migrationResult = model.migrate(PreferenceMigrationEntry(
                action = PreferenceMigrationEntry.Action.KEEP_AS_IS,
                type = type,
                key = key,
                rawValue = if (type.isString()) StringEncoder.decode(rawEncodedValue) else rawEncodedValue,
            ))
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
            newStore.values.put(key, TypedRawEncodedValue(type, rawEncodedValue))
        }
        model.declaredPreferenceEntries.forEach { (key, entry) ->
            val value = requireNotNull(newStore.values[key]) {
                buildString {
                    append("Key '$key'")
                    append(" for datastore '${newStore.storageProvider.datastoreName}")
                    append(" not found, should never happen!")
                }
            }
            entry.init(value, newStore)
        }
    }

    private suspend fun <V : Any> PreferenceData<V>.init(typedRawEncodedValue: TypedRawEncodedValue, store: Store) {
        if (type != typedRawEncodedValue.type) return
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
                value = TypedRawEncodedValue(type, rawEncodedValue),
                storeId = store.id,
            )
            eventQueue.send(event)
            event.done.consumeFirst()
        }
    }

    private suspend fun handleSetValueAndPersistToStorage(event: Event.SetValueAndPersistToStorage) {
        val currentStore = currentStoreRef.get()
        if (currentStore == null || event.storeId != currentStore.id) {
            throw Exception("Outdated update request, ignoring")
        }
    }

    private data class Store(
        val id: Long,
        val readOnly: Boolean = false,
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
            val value: TypedRawEncodedValue,
            val storeId: Long,
        ) : Event()
    }

    companion object {
        /**
         * Creates a preference model store and returns it.
         *
         * @param kClass The class of the preference model to create.
         *
         * @since 0.3.0
         */
        @Suppress("unchecked_cast")
        fun <T : PreferenceModel> newInstanceOf(
            kClass: KClass<T>,
        ): JetPrefDataStore<T> {
            val modelImplName = kClass.qualifiedName!! + "Impl"
            val modelImplClass = Class.forName(modelImplName)
            val modelImplInstance = modelImplClass.getDeclaredConstructor().newInstance() as T
            return JetPrefDataStore(modelImplInstance) // TODO
        }
    }
}

/*



    private fun <V : Any> PreferenceData<V>.serialize(): String? {
        if (type.isInvalid() || !type.isPrimitive()) return null
        val rawValue = (if (JetPref.encodeDefaultValues) get() else getOrNull())?.let {
            serializer.serialize(it)
        } ?: return null
        return buildString {
            append(type.id)
            append(JetPref.DELIMITER)
            append(key)
            append(JetPref.DELIMITER)
            if (type.isString()) {
                append(StringEncoder.encode(rawValue))
            } else {
                append(rawValue)
            }
        }
    }

    private fun <V : Any> PreferenceData<V>.deserialize(rawValue: String) {
        if (type.isInvalid() || !type.isPrimitive()) return
        val value = if (type.isString()) {
            serializer.deserialize(StringEncoder.decode(rawValue))
        } else {
            serializer.deserialize(rawValue)
        }
        if (value != null) {
            set(value, requestSync = false)
        }
    }

    inner class PersistenceHandler(val persistenceHandler: dev.patrickgold.jetpref.datastore.PersistenceHandler, readOnly: Boolean) {

        private val ioJob = ioScope.launch(Dispatchers.IO) {
            runSafely { loadPrefs(reset = true) }
            while (isActive) {
                if (datastoreReadyStatus.get() && persistReq.getAndSet(false) && !readOnly) {
                    runSafely { persistPrefs() }
                }
                delay(JetPref.saveIntervalMs)
            }
        }

        internal suspend fun cancelJobsAndJoin() {
            ioJob.cancelAndJoin()
        }

        suspend fun loadPrefs(reset: Boolean) = withContext(Dispatchers.IO) {
            registryGuard.withLock {
                datastoreReadyStatus.set(false, requestSync = false)
                if (reset) {
                    for (prefData in declaredPreferenceEntries) {
                        prefData.reset(requestSync = false)
                    }
                }
                var requiresSyncAfterRead = false
                persistenceHandler.load().onSuccess { rawDatastoreContent ->
                    for (line in rawDatastoreContent.lines()) ioScope.launch line@{
                        if (line.isBlank()) return@line
                        val del1 = line.indexOf(JetPref.DELIMITER)
                        if (del1 < 0) return@line
                        var type = PreferenceType.from(line.substring(0, del1))
                        val del2 = line.indexOf(JetPref.DELIMITER, del1 + 1)
                        if (del2 < 0) return@line
                        var key = line.substring(del1 + 1, del2)
                        var rawValue = if (del2 + 1 == line.length) "" else line.substring(del2 + 1)

                        // Handle preference data migration
                        val migrationResult = migrate(PreferenceMigrationEntry(
                            action = PreferenceMigrationEntry.Action.KEEP_AS_IS,
                            type = type,
                            key = key,
                            rawValue = if (type.isString()) StringEncoder.decode(rawValue) else rawValue,
                        ))
                        when (migrationResult.action) {
                            PreferenceMigrationEntry.Action.KEEP_AS_IS -> {
                                /* Do nothing and continue as no migration is needed */
                            }
                            PreferenceMigrationEntry.Action.RESET -> {
                                requiresSyncAfterRead = true
                                return@line
                            }
                            PreferenceMigrationEntry.Action.TRANSFORM -> {
                                requiresSyncAfterRead = true
                                type = migrationResult.type
                                key = migrationResult.key
                                rawValue = if (type.isString()) {
                                    StringEncoder.encode(migrationResult.rawValue)
                                } else {
                                    migrationResult.rawValue
                                }
                            }
                        }

                        val prefData = declaredPreferenceEntries.find { it.key == key }
                        if (prefData != null) {
                            if (prefData.type.id != type.id) {
                                return@line
                            }
                            prefData.deserialize(rawValue)
                        }
                    }
                }
                datastoreReadyStatus.set(true, requestSync = requiresSyncAfterRead)
            }
        }

        suspend fun persistPrefs() = withContext(Dispatchers.IO) {
            registryGuard.withLock {
                val rawDatastoreContent = buildString {
                    for (prefData in declaredPreferenceEntries) {
                        val serializedData = prefData.serialize() ?: continue
                        appendLine(serializedData)
                    }
                }
                persistenceHandler.persist(rawDatastoreContent)
            }
        }
    }
 */
