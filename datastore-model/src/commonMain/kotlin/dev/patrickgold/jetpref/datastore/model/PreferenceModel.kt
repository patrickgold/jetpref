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

@Suppress("SameParameterValue")
abstract class PreferenceModel {
    abstract val declaredPreferenceEntries: Map<String, PreferenceData<*>>

    protected fun boolean(
        @PreferenceKey key: String,
        default: Boolean,
    ): PreferenceData<Boolean> {
        return preferenceDataOf(
            key = key,
            default = default,
            type = PreferenceType.boolean(),
            serializer = BooleanPreferenceSerializer,
        )
    }

    protected fun double(
        @PreferenceKey key: String,
        default: Double,
    ): PreferenceData<Double> {
        return preferenceDataOf(
            key = key,
            default = default,
            type = PreferenceType.double(),
            serializer = DoublePreferenceSerializer,
        )
    }

    protected fun float(
        @PreferenceKey key: String,
        default: Float,
    ): PreferenceData<Float> {
        return preferenceDataOf(
            key = key,
            default = default,
            type = PreferenceType.float(),
            serializer = FloatPreferenceSerializer,
        )
    }

    protected fun int(
        @PreferenceKey key: String,
        default: Int,
    ): PreferenceData<Int> {
        return preferenceDataOf(
            key = key,
            default = default,
            type = PreferenceType.integer(),
            serializer = IntPreferenceSerializer,
        )
    }

    protected fun long(
        @PreferenceKey key: String,
        default: Long,
    ): PreferenceData<Long> {
        return preferenceDataOf(
            key = key,
            default = default,
            type = PreferenceType.long(),
            serializer = LongPreferenceSerializer,
        )
    }

    protected fun string(
        @PreferenceKey key: String,
        default: String,
    ): PreferenceData<String> {
        return preferenceDataOf(
            key = key,
            default = default,
            type = PreferenceType.string(),
            serializer = StringPreferenceSerializer,
        )
    }

    protected fun <V : Any> custom(
        @PreferenceKey key: String,
        default: V,
        serializer: PreferenceSerializer<V>,
    ): PreferenceData<V> {
        return preferenceDataOf(
            key = key,
            default = default,
            type = PreferenceType.string(),
            serializer = serializer,
        )
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
                } catch (_: Exception) {
                    null
                }
            }
        }
        return custom(key, default, serializer)
    }

    protected fun localTime(
        @PreferenceKey key: String,
        default: LocalTime,
    ): PreferenceData<LocalTime> {
        return custom(key, default, TimePreferenceSerializer)
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
    open fun migrate(entry: PreferenceMigrationEntry): PreferenceMigrationEntry {
        // By default keep as is
        return entry.keepAsIs()
    }
}
