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

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import dev.patrickgold.jetpref.datastore.JetPrefManager
import dev.patrickgold.jetpref.datastore.annotations.PreferenceKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.lang.ref.WeakReference
import java.time.LocalTime
import java.util.concurrent.atomic.AtomicBoolean

@Suppress("SameParameterValue")
abstract class PreferenceModel(val name: String) {
    companion object {
        private const val INTERNAL_PREFIX = "__internal"
    }

    internal val scope: CoroutineScope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())

    private var appContext = WeakReference<Context?>(null)
    private val registryGuard = Mutex()
    private val registry: MutableList<PreferenceData<*>> = mutableListOf()
    val datastoreReadyStatus = boolean(
        key = "${INTERNAL_PREFIX}_datastore_ready_status",
        default = false,
    )

    private var persistReq: AtomicBoolean = AtomicBoolean(false)
    private var persistJob: Job? = null
    private var setupJob: Job? = null

    init {
        Validator.validateFileName(name)
        datastoreReadyStatus.set(false, requestSync = false)
    }

    internal fun notifyValueChanged() = persistReq.set(true)

    private fun registryAdd(prefData: PreferenceData<*>) = scope.launch {
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

    protected inline fun <reified V : Enum<V>> enum(
        @PreferenceKey key: String,
        default: V,
    ): PreferenceData<V> {
        @Suppress("DEPRECATION_ERROR") // this is the only intended call site for __enum()
        return __enum(key, default) {
            try {
                enumValueOf(it)
            } catch(e: Throwable) {
                null
            }
        }
    }

    @Deprecated(
        message = "Not for public use.",
        level = DeprecationLevel.ERROR,
        replaceWith = ReplaceWith("enum(key, default)"),
    )
    @Suppress("FunctionName")
    protected fun <V : Enum<V>> __enum(
        @PreferenceKey key: String,
        default: V,
        stringToEnum: (String) -> V?,
    ): PreferenceData<V> {
        val prefData = EnumPreferenceData(this, key, default, stringToEnum)
        registryAdd(prefData)
        return prefData
    }

    /**
     * Requires core library desugaring if minSdk < 26:
     *  https://developer.android.com/studio/write/java8-support#library-desugaring
     *
     * Currently constrained to Android 8+ so no desugaring is needed.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    protected fun localTime(
        @PreferenceKey key: String,
        default: LocalTime,
    ): PreferenceData<LocalTime> {
        val prefData = LocalTimePreferenceData(this, key, default)
        registryAdd(prefData)
        return prefData
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

    fun forceSyncToDisk() = runBlocking {
        syncToDisk()
    }

    private suspend fun syncToDisk() = withContext(Dispatchers.IO) {
        val context = appContext.get() ?: return@withContext
        registryGuard.withLock {
            JetPrefManager.savePrefFile(context, name) {
                for (preferenceData in registry) {
                    val serializedData = preferenceData.serialize() ?: continue
                    it.appendLine(serializedData)
                }
            }
        }
    }

    fun initializeForContext(context: Context) {
        persistJob?.cancel()
        setupJob?.cancel()
        persistReq.set(false)
        appContext = WeakReference(context)
        setupJob = scope.setupModel(context)
    }

    private fun CoroutineScope.setupModel(context: Context) = launch(Dispatchers.IO) {
        datastoreReadyStatus.set(false, requestSync = false)
        JetPrefManager.loadPrefFile(context, name) {
            registryGuard.withLock {
                for (prefData in registry) {
                    prefData.reset(requestSync = false)
                }
                //android.util.Log.i("JetPref", registry.toString())
                for (line in it.lineSequence()) launch line@ {
                    if (line.isBlank()) return@line
                    val del1 = line.indexOf(JetPrefManager.DELIMITER)
                    if (del1 < 0) return@line
                    val type = PreferenceType.from(line.substring(0, del1))
                    val del2 = line.indexOf(JetPrefManager.DELIMITER, del1 + 1)
                    if (del2 < 0) return@line
                    val key = line.substring(del1 + 1, del2)
                    val prefData = registry.find { it.key == key }
                    if (prefData != null) {
                        if (prefData.type.id != type.id) {
                            return@line
                        }
                        prefData.deserialize(
                            if (del2 + 1 == line.length) { "" } else { line.substring(del2 + 1) }
                        )
                    }
                }
            }
        }
        datastoreReadyStatus.set(true, requestSync = false)
        persistJob = launchSyncJob()
    }

    private fun CoroutineScope.launchSyncJob() = launch(Dispatchers.IO) {
        while (isActive) {
            if (persistReq.getAndSet(false)) {
                syncToDisk()
            }
            delay(JetPrefManager.saveIntervalMs)
        }
    }

    private fun <V : Any> PreferenceData<V>.serialize(): String? {
        if (type.isInvalid() || !type.isPrimitive()) return null
        val rawValue = getOrNull()?.let { serializer.serialize(it) } ?: return null
        val sb = StringBuilder()
        sb.append(type.id)
        sb.append(JetPrefManager.DELIMITER)
        sb.append(key)
        sb.append(JetPrefManager.DELIMITER)
        if (type.isString()) {
            sb.append(StringEncoder.encode(rawValue))
        } else {
            sb.append(rawValue)
        }
        return sb.toString()
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
}
