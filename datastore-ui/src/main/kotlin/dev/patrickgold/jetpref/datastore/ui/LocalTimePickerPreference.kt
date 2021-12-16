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

package dev.patrickgold.jetpref.datastore.ui

@ExperimentalJetPrefDatastoreUi
data class ClockFormat(
    val is24Hour: Boolean,
    val showHours: Boolean,
    val showMinutes: Boolean,
    val showSeconds: Boolean,
    val showMilliseconds: Boolean,
)

object TimePickerDefaults {
    /**
     * Creates a clock format config, which can be used to determine
     * what UI controls a time picker should show.
     */
    @ExperimentalJetPrefDatastoreUi
    fun clockFormat(
        is24Hour: Boolean = true,
        showHours: Boolean = true,
        showMinutes: Boolean = true,
        showSeconds: Boolean = false,
        showMilliseconds: Boolean = false,
    ) = ClockFormat(is24Hour, showHours, showMinutes, showSeconds, showMilliseconds)
}

// TODO: re-implement a time picker preference which does not depend on the
//       time picker Android view and core lib desugaring
