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

package dev.patrickgold.jetpref.datastore.model

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.atomic.AtomicReference

private class PreferenceDataImpl<V : Any>(
    override val key: String,
    override val default: V,
    override val type: PreferenceType,
    override val serializer: PreferenceSerializer<V>,
) : PreferenceData<V> {
    override val typedKey = PreferenceModel.TypedKey(type, key)
    private val cachedValue = AtomicReference<V?>(null)
    private var cachedValueFlow = MutableStateFlow(default)
    private val cachedValueWriteGuard = Mutex()
    private var valuePersistHandler: PreferenceData.ValuePersistHandler<V>? = null

    init {
        Validator.validateKey(key)
    }

    override fun get(): V = cachedValue.get() ?: default

    override fun getOrNull(): V? = cachedValue.get()

    override fun getAsFlow(): StateFlow<V> = cachedValueFlow

    override suspend fun set(value: V): Unit = cachedValueWriteGuard.withLock {
        cachedValue.set(value)
        cachedValueFlow.value = value
        valuePersistHandler?.onValueChanged(value)
    }

    override suspend fun reset(): Unit = cachedValueWriteGuard.withLock {
        cachedValue.set(null)
        cachedValueFlow.value = default
        valuePersistHandler?.onValueChanged(default)
    }

    override suspend fun init(
        value: V?,
        handler: PreferenceData.ValuePersistHandler<V>,
    ): Unit = cachedValueWriteGuard.withLock {
        cachedValue.set(value)
        cachedValueFlow.value = value ?: default
        valuePersistHandler = handler
    }
}

internal actual fun <V : Any> preferenceDataOf(
    key: String,
    default: V,
    type: PreferenceType,
    serializer: PreferenceSerializer<V>
): PreferenceData<V> {
    return PreferenceDataImpl(key, default, type, serializer)
}
