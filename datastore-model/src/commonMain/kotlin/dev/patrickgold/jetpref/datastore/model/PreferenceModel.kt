/*
 * Copyright 2021 Patrick Goldinger
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

package dev.patrickgold.jetpref.datastore.model

import dev.patrickgold.jetpref.datastore.JetPref
import dev.patrickgold.jetpref.datastore.annotations.PreferenceKey
import dev.patrickgold.jetpref.datastore.runSafely
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean

@Suppress("SameParameterValue", "MemberVisibilityCanBePrivate")
abstract class PreferenceModel {
    companion object {
        private const val INTERNAL_PREFIX = "__internal"
    }

    internal val mainScope: CoroutineScope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())
    internal val ioScope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val registryGuard = Mutex()
    private val registry: MutableList<PreferenceData<*>> = mutableListOf()
    private var persistReq: AtomicBoolean = AtomicBoolean(false)

    val datastoreReadyStatus = boolean(
        key = "${INTERNAL_PREFIX}_datastore_ready_status",
        default = false,
    )
    var datastorePersistenceHandler: PersistenceHandler? = null
        private set

    init {
        datastoreReadyStatus.set(false, requestSync = false)
    }

    internal fun notifyValueChanged() = persistReq.set(true)

    private fun registryAdd(prefData: PreferenceData<*>) = ioScope.launch {
        if (!prefData.key.startsWith(INTERNAL_PREFIX)) {
            registryGuard.withLock { registry.add(prefData) }
        }
    }

    protected fun boolean(
        @PreferenceKey key: String,
        default: Boolean,
    ): PreferenceData<Boolean> {
        val prefData = BooleanPreferenceData(this, key, default)
        registryAdd(prefData)
        return prefData
    }

    protected fun double(
        @PreferenceKey key: String,
        default: Double,
    ): PreferenceData<Double> {
        val prefData = DoublePreferenceData(this, key, default)
        registryAdd(prefData)
        return prefData
    }

    protected fun float(
        @PreferenceKey key: String,
        default: Float,
    ): PreferenceData<Float> {
        val prefData = FloatPreferenceData(this, key, default)
        registryAdd(prefData)
        return prefData
    }

    protected fun int(
        @PreferenceKey key: String,
        default: Int,
    ): PreferenceData<Int> {
        val prefData = IntPreferenceData(this, key, default)
        registryAdd(prefData)
        return prefData
    }

    protected fun long(
        @PreferenceKey key: String,
        default: Long,
    ): PreferenceData<Long> {
        val prefData = LongPreferenceData(this, key, default)
        registryAdd(prefData)
        return prefData
    }

    protected fun string(
        @PreferenceKey key: String,
        default: String,
    ): PreferenceData<String> {
        val prefData = StringPreferenceData(this, key, default)
        registryAdd(prefData)
        return prefData
    }

    protected fun time(
        @PreferenceKey key: String,
        default: LocalTime,
    ): PreferenceData<LocalTime> {
        val prefData = CustomPreferenceData(this, key, default, TimePreferenceSerializer)
        registryAdd(prefData)
        return prefData
    }

    protected inline fun <reified V : Enum<V>> enum(
        @PreferenceKey key: String,
        default: V,
    ): PreferenceData<V> {
        val serializer = object : PreferenceSerializer<V> {
            override fun serialize(value: V): String {
                return value.toString()
            }

            override fun deserialize(value: String): V? {
                return try {
                    enumValueOf<V>(value)
                } catch (e: Exception) {
                    null
                }
            }
        }
        return custom(key, default, serializer)
    }

    protected fun <V : Any> custom(
        @PreferenceKey key: String,
        default: V,
        serializer: PreferenceSerializer<V>,
    ): PreferenceData<V> {
        val prefData = CustomPreferenceData(this, key, default, serializer)
        registryAdd(prefData)
        return prefData
    }

    suspend fun initialize(persistenceHandler: dev.patrickgold.jetpref.datastore.PersistenceHandler, readOnly: Boolean = false) = registryGuard.withLock {
        runSafely { datastorePersistenceHandler?.cancelJobsAndJoin() }
        persistReq.set(false)
        runSafely { datastorePersistenceHandler = PersistenceHandler(persistenceHandler, readOnly) }
    }

    /**
     * Called for each entry during the loading process to allow for potential migration of preference entries. In
     * general, one of the following three results can be returned for each entry:
     *
     *  - `entry.keepAsIs()`        - if the entry has not changed
     *  - `entry.reset()`           - if the entry should be set to default value (internally `null`)
     *  - `entry.transform(...)`    - if the entry should be transformed (type ID, key and/or raw value)
     *
     * The order in which the entries are given for migration processing is unspecified. All entries, regardless of
     * their actual type, are delivered with raw value strings (string preference values are properly decoded though).
     * This means comparing values requires manual conversion and if needed type ID verification. The same rules apply
     * for the result entry's raw value.
     *
     * @param entry The migration entry, which contains all the necessary data and verbose methods for returning an
     *  entry result for migration.
     *
     * @return A result migration entry as described above.
     */
    protected open fun migrate(entry: PreferenceMigrationEntry): PreferenceMigrationEntry {
        // By default keep as is
        return entry.keepAsIs()
    }

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
                    for (prefData in registry) {
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

                        val prefData = registry.find { it.key == key }
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
                    for (prefData in registry) {
                        val serializedData = prefData.serialize() ?: continue
                        appendLine(serializedData)
                    }
                }
                persistenceHandler.persist(rawDatastoreContent)
            }
        }
    }
}
