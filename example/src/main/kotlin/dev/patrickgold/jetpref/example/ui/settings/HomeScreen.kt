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

package dev.patrickgold.jetpref.example.ui.settings

import androidx.compose.runtime.Composable
import dev.patrickgold.jetpref.example.AppPrefs
import dev.patrickgold.jetpref.ui.compose.Preference
import dev.patrickgold.jetpref.ui.compose.PreferenceGroup
import dev.patrickgold.jetpref.ui.compose.PreferenceScreen
import dev.patrickgold.jetpref.ui.compose.SwitchPreference

@Composable
fun HomeScreen() = PreferenceScreen(::AppPrefs, iconSpaceReserved = true) {
    Preference(
        title = "Hello",
        summary = "Test",
        onClick = { },
    )
    SwitchPreference(
        prefs.showTestGroup,
        title = "Show Test Group",
    )
    PreferenceGroup(title = "Hello", visibleIf = { prefs.showTestGroup isEqualTo true }) {
        SwitchPreference(
            prefs.test.isButtonShowing,
            title = "isBtnShow",
        )
        SwitchPreference(
            prefs.test.isButtonShowing2,
            title = "isBtnShow2",
            summaryOn = "Hello",
            summaryOff = "Bye",
            enabledIf = { prefs.test.isButtonShowing isEqualTo true },
        )
    }
}