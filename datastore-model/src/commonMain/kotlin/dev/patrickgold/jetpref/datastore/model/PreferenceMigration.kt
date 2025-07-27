/*
 * Copyright (C) 2022 Patrick Goldinger
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
 * Data class-like class which holds a migration entry's [type], [key], [rawValue] and internal action. It then provides
 * verbose methods to either keep as is, reset or transform the entry for returning a result in the migration process.
 *
 * @property type The type ID of the preference entry. Can be used to determine which type the raw value is supposed to
 *  be if there is ambiguity. MUST be a valid ID, else this preference does not work correctly.
 * @property key The key of the preference entry. Must conform to [PreferenceData.key]'s rules.
 * @property rawValue The raw value of the preference entry. If the preference is a string, the string is guaranteed to
 *  be properly decoded.
 *
 * @since 0.1.0
 */
class PreferenceMigrationEntry internal constructor(
    internal val action: Action,
    val type: PreferenceType,
    @PreferenceKey val key: String,
    val rawValue: String,
) {
    /**
     * Keep this entry as is in the migration process.
     *
     * @since 0.1.0
     */
    fun keepAsIs() = if (action == Action.KEEP_AS_IS) this else copy(action = Action.KEEP_AS_IS)

    /**
     * Reset this entry back to the default value in the migration process.
     *
     * @since 0.1.0
     */
    fun reset() = if (action == Action.RESET) this else copy(action = Action.RESET)

    /**
     * Transform this entry's type, key, and/or raw value in the migration process.
     *
     * @since 0.1.0
     */
    fun transform(
        type: PreferenceType = this.type,
        @PreferenceKey key: String = this.key,
        rawValue: String = this.rawValue,
    ) = PreferenceMigrationEntry(Action.TRANSFORM, type, key, rawValue)

    private fun copy(
        action: Action = this.action,
        type: PreferenceType = this.type,
        @PreferenceKey key: String = this.key,
        rawValue: String = this.rawValue,
    ) = PreferenceMigrationEntry(action, type, key, rawValue)

    internal enum class Action {
        KEEP_AS_IS,
        RESET,
        TRANSFORM;
    }
}
