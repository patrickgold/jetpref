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

import dev.patrickgold.jetpref.datastore.JetPrefManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.atomic.AtomicBoolean

abstract class PreferenceModel(val name: String) {
    internal val scope: CoroutineScope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())

    private val registryGuard = Mutex()
    private val registry: MutableList<PreferenceData<*>> = mutableListOf()

    private var persistReq: AtomicBoolean = AtomicBoolean(false)
    private var persistJob: Job? = null

    init {
        scope.setupModel()
    }

    internal fun notifyValueChanged() = persistReq.set(true)

    private fun registryAdd(prefData: PreferenceData<*>) = scope.launch {
        registryGuard.withLock { registry.add(prefData) }
    }

    protected fun boolean(
        key: String,
        default: Boolean,
    ): PreferenceData<Boolean> {
        val prefData = BooleanPreferenceData(this, key, default)
        registryAdd(prefData)
        return prefData
    }

    protected fun double(
        key: String,
        default: Double,
    ): PreferenceData<Double> {
        val prefData = DoublePreferenceData(this, key, default)
        registryAdd(prefData)
        return prefData
    }

    protected fun float(
        key: String,
        default: Float,
    ): PreferenceData<Float> {
        val prefData = FloatPreferenceData(this, key, default)
        registryAdd(prefData)
        return prefData
    }

    protected fun int(
        key: String,
        default: Int,
    ): PreferenceData<Int> {
        val prefData = IntPreferenceData(this, key, default)
        registryAdd(prefData)
        return prefData
    }

    protected fun long(
        key: String,
        default: Long,
    ): PreferenceData<Long> {
        val prefData = LongPreferenceData(this, key, default)
        registryAdd(prefData)
        return prefData
    }

    protected fun string(
        key: String,
        default: String,
    ): PreferenceData<String> {
        val prefData = StringPreferenceData(this, key, default)
        registryAdd(prefData)
        return prefData
    }

    protected fun <V : Any> custom(
        key: String,
        default: V,
        serializer: PreferenceSerializer<V>,
    ): PreferenceData<V> {
        val prefData = CustomPreferenceData(this, key, default, serializer)
        registryAdd(prefData)
        return prefData
    }

    private fun CoroutineScope.setupModel() = launch(Dispatchers.IO) {
        JetPrefManager.loadPrefFile(name) {
            registryGuard.withLock {
                for (line in lineSequence()) {
                    if (line.isBlank()) continue
                    val type = line.first().toString()
                    val del1 = line.indexOf(JetPrefManager.DELIMITER)
                    if (del1 < 0) continue
                    val del2 = line.indexOf(JetPrefManager.DELIMITER, del1 + 1)
                    if (del2 < 0) continue
                    val key = line.substring(del1 + 1, del2)
                    val preferenceData = registry.find { it.key == key }
                    if (preferenceData != null) {
                        if (preferenceData.type != type) {
                            preferenceData.reset(requestSync = false)
                        }
                        preferenceData.deserialize(
                            if (del2 + 1 == line.length) { "" } else { line.substring(del2 + 1) }
                        )
                    }
                }
            }
        }
        persistJob = launchSyncJob()
    }

    private fun CoroutineScope.launchSyncJob() = launch(Dispatchers.IO) {
        while (isActive) {
            if (persistReq.getAndSet(false)) {
                JetPrefManager.savePrefFile(name) {
                    registryGuard.withLock {
                        for (preferenceData in registry) {
                            val serializedData = preferenceData.serialize() ?: continue
                            appendLine(serializedData)
                        }
                    }
                }
            }
            delay(JetPrefManager.saveIntervalMs)
        }
    }

    private fun <V : Any> PreferenceData<V>.serialize(): String? {
        if (!type.isPrimitive()) return null
        val rawValue = getOrNull()?.let { serializer.serialize(it) } ?: return null
        val sb = StringBuilder()
        sb.append(type)
        sb.append(JetPrefManager.DELIMITER)
        sb.append(key)
        sb.append(JetPrefManager.DELIMITER)
        sb.append(rawValue)
        return sb.toString()
    }

    private fun <V : Any> PreferenceData<V>.deserialize(rawValue: String) {
        if (!type.isPrimitive()) return
        val value = serializer.deserialize(rawValue)
        if (value == null) {
            reset(requestSync = false)
        } else {
            set(value, requestSync = false)
        }
    }
}
