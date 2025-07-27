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

/**
 * Main class for preference model declaration.
 *
 * Example declaration of a simple model:
 * ```kt
 * val AppPrefsStore = jetprefDataStoreOf(AppPrefsModel::class)
 *
 * @Preferences
 * abstract class AppPrefsModel : PreferenceModel() {
 *     val numericPref = int(
 *         key = "numeric_pref",
 *         default = 0,
 *     )
 *
 *     // Models may contain arbitrary nesting of entries, as long as nested entries
 *     // are contained within inner classes of the model class:
 *     val group = Group()
 *     inner class Group() {
 *         val numericPref = int(
 *             key = "numeric_pref",
 *             default = 0,
 *         )
 *     }
 * }
 * ```
 *
 * @since 0.1.0
 */
abstract class PreferenceModel {
    abstract val declaredPreferenceEntries: Map<TypedKey, PreferenceData<*>>

    /**
     * Declare a `boolean` preference entry. Its [key] must be unique within this preference model.
     *
     * @param key The key of the preference, used for uniquely identifying this entry.
     * @param default The default value of this entry.
     *
     * @since 0.1.0
     */
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

    /**
     * Declare a `double` preference entry. Its [key] must be unique within this preference model.
     *
     * @param key The key of the preference, used for uniquely identifying this entry.
     * @param default The default value of this entry.
     *
     * @since 0.1.0
     */
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

    /**
     * Declare a `float` preference entry. Its [key] must be unique within this preference model.
     *
     * @param key The key of the preference, used for uniquely identifying this entry.
     * @param default The default value of this entry.
     *
     * @since 0.1.0
     */
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

    /**
     * Declare an `int` preference entry. Its [key] must be unique within this preference model.
     *
     * @param key The key of the preference, used for uniquely identifying this entry.
     * @param default The default value of this entry.
     *
     * @since 0.1.0
     */
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

    /**
     * Declare a `long` preference entry. Its [key] must be unique within this preference model.
     *
     * @param key The key of the preference, used for uniquely identifying this entry.
     * @param default The default value of this entry.
     *
     * @since 0.1.0
     */
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

    /**
     * Declare a `string` preference entry. Its [key] must be unique within this preference model.
     *
     * @param key The key of the preference, used for uniquely identifying this entry.
     * @param default The default value of this entry.
     *
     * @since 0.1.0
     */
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

    /**
     * Declare a custom preference entry. Its [key] must be unique within this preference model.
     *
     * In addition to the [key] and [default] value, this entry accepts a custom serializer. This allows
     * for any type to be persisted in the preference model. The custom type must be serializable to a
     * string with the given [serializer].
     *
     * @param key The key of the preference, used for uniquely identifying this entry.
     * @param default The default value of this entry.
     * @param serializer The serializer for the custom data type.
     *
     * @since 0.1.0
     */
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

    /**
     * Declare a `enum` preference entry. Its [key] must be unique within this preference model.
     *
     * Enum values will internally be represented as a string, using the constant name as the value.
     *
     * @param key The key of the preference, used for uniquely identifying this entry.
     * @param default The default value of this entry.
     *
     * @since 0.1.0
     */
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

    /**
     * Declare a `local time` preference entry. Its [key] must be unique within this preference model.
     *
     * Local time values will internally be represented as a string.
     *
     * @param key The key of the preference, used for uniquely identifying this entry.
     * @param default The default value of this entry.
     *
     * @since 0.2.0
     */
    protected fun localTime(
        @PreferenceKey key: String,
        default: LocalTime,
    ): PreferenceData<LocalTime> {
        return custom(key, default, LocalTimePreferenceSerializer)
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
     * @return A result migration entry as described above.
     *
     * @since 0.1.0
     */
    open fun migrate(entry: PreferenceMigrationEntry): PreferenceMigrationEntry {
        // By default, keep as is
        return entry.keepAsIs()
    }

    data class TypedKey(
        val type: PreferenceType,
        val key: String,
    ) {
        override fun toString(): String {
            return "$type/$key"
        }
    }
}
