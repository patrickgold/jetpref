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

import dev.patrickgold.jetpref.datastore.annotations.PreferenceKey
import kotlinx.coroutines.flow.StateFlow

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
     * The typed key of this preference data. Is a composite of [type] and [key].
     *
     * @since 0.3.0
     */
    val typedKey: PreferenceModel.TypedKey

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

    fun getAsFlow(): StateFlow<V>

    /**
     * Sets the value of this preference data.
     *
     * @param value The new value to set for this preference data.
     *
     * @since 0.1.0
     */
    suspend fun set(value: V): Result<Unit>

    /**
     * Resets this preference data to [default].
     *
     * @since 0.1.0
     */
    suspend fun reset(): Result<Unit>

    suspend fun init(value: V?, handler: ValuePersistHandler<V>)

    fun interface ValuePersistHandler<V> {
        suspend fun onValueChanged(newValue: V?): Result<Unit>
    }
}

internal expect fun <V : Any> preferenceDataOf(
    key: String,
    default: V,
    type: PreferenceType,
    serializer: PreferenceSerializer<V>,
): PreferenceData<V>
