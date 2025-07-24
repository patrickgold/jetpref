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

import dev.patrickgold.jetpref.datastore.annotations.PreferenceKey
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Interface for an implementation of a preference data accessor for
 * type [V]. A preference data is the heart of all the jetpref logic
 * and allows to observe value changes. The default implementation and
 * behavior internally is very similar to Android's LiveData, but this
 * interface allows for any kind of custom implementation.
 *
 * @since 0.1.0
 */
interface PreferenceData<V : Any> {
    /**
     * The type of this preference, useful especially in the serialization
     * process. If the preference type attribute is invalid, this preference
     * data will not work correctly.
     *
     * @since 0.1.0
     */
    val type: PreferenceType

    /**
     * The serializer for this preference data, used by the serialization
     * process to persist the cached preferences onto the device storage.
     *
     * @since 0.1.0
     */
    val serializer: PreferenceSerializer<V>

    /**
     * The key of this value. Generally a key must only contain the following
     * characters and symbols:
     *  - `a-z`
     *  - `A-Z`
     *  - `0-9` (not at the start though)
     *  - `_`
     *  - `-` (not at the start or end though)
     *
     * @since 0.1.0
     */
    @PreferenceKey val key: String

    /**
     * The default value for this preference data. Is used if no valid persisted
     * value is existent or when resetting this preference data.
     *
     * @since 0.1.0
     */
    val default: V

    /**
     * Gets the cached value of this preference data.
     *
     * @return The value of this preference data or [default].
     *
     * @since 0.1.0
     */
    fun get(): V

    /**
     * Gets the cached value of this preference data.
     *
     * @return The value of this preference data or null.
     *
     * @since 0.1.0
     */
    fun getOrNull(): V?

    /**
     * Sets the value of this preference data.
     *
     * @param value The new value to set for this preference data.
     * @param requestSync If true the [PreferenceModel] is requested to
     *  persist the cached values to storage.
     *
     * @since 0.1.0
     */
    fun set(value: V, requestSync: Boolean = true)

    /**
     * Resets this preference data to [default].
     *
     * @param requestSync If true the [PreferenceModel] is requested to
     *  persist the cached values to storage.
     *
     * @since 0.1.0
     */
    fun reset(requestSync: Boolean = true)
}

internal class PreferenceDataImpl<V : Any>(
    private val model: PreferenceModel,
    override val key: String,
    override val default: V,
    override val type: PreferenceType,
    override val serializer: PreferenceSerializer<V>,
) : PreferenceData<V> {
    private val cacheGuard = Mutex()
    private var cachedValue: V? = null
    private var cachedValueVersion: Int = 0

    init {
        Validator.validateKey(key)
    }

    override fun get(): V = cachedValue ?: default

    override fun getOrNull(): V? = cachedValue

    override fun set(value: V, requestSync: Boolean) {
        model.mainScope.launch {
            cacheGuard.withLock {
                if (cachedValue != value) {
                    cachedValue = value
                    cachedValueVersion++
                    if (requestSync) {
                        model.notifyValueChanged()
                    }
                    //dispatchValue(null)
                }
            }
        }
    }

    override fun reset(requestSync: Boolean) {
        model.mainScope.launch {
            cacheGuard.withLock {
                if (cachedValue != null) {
                    cachedValue = null
                    cachedValueVersion++
                    if (requestSync) {
                        model.notifyValueChanged()
                    }
                    //dispatchValue(null)
                }
            }
        }
    }
}
