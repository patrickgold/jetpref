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
import dev.patrickgold.jetpref.example.R
import dev.patrickgold.jetpref.example.Theme
import dev.patrickgold.jetpref.example.examplePreferenceModel
import dev.patrickgold.jetpref.ui.compose.DialogSliderPreference
import dev.patrickgold.jetpref.ui.compose.ListPreference
import dev.patrickgold.jetpref.ui.compose.PreferenceGroup
import dev.patrickgold.jetpref.ui.compose.PreferenceLayout
import dev.patrickgold.jetpref.ui.compose.SwitchPreference
import dev.patrickgold.jetpref.ui.compose.annotations.ExperimentalJetPrefUi
import dev.patrickgold.jetpref.ui.compose.entry

@OptIn(ExperimentalJetPrefUi::class)
@Composable
fun HomeScreen() = PreferenceLayout(examplePreferenceModel()) {
    ListPreference(
        prefs.theme,
        title = "Theme",
        entries = Theme.listEntries(),
    )
    DialogSliderPreference(
        primaryPref = prefs.boxSizePortrait,
        secondaryPref = prefs.boxSizeLandscape,
        title = "Example integer slider",
        unit = "{v}%",
        primaryLabel = "Portrait",
        secondaryLabel = "Landscape",
        min = 0,
        max = 100,
        stepIncrement = 1,
    )
    SwitchPreference(
        prefs.showExampleGroup,
        iconId = R.drawable.ic_question_answer_black_24dp,
        title = "Show example group",
        summary = "Show/hide the example group",
    )
    PreferenceGroup(title = "Example group", visibleIf = { prefs.showExampleGroup isEqualTo true }) {
        SwitchPreference(
            prefs.example.isButtonShowing,
            title = "isBtnShow",
        )
        SwitchPreference(
            prefs.example.isButtonShowing2,
            iconId = R.drawable.ic_question_answer_black_24dp,
            title = "isBtnShow2",
            summaryOn = "Hello",
            summaryOff = "Bye",
            enabledIf = { prefs.example.isButtonShowing isEqualTo true },
        )
        DialogSliderPreference(
            prefs.example.buttonSize,
            title = "Button Size",
            min = 0,
            max = 100,
            stepIncrement = 1,
            unit = "{v}%",
        )
        DialogSliderPreference(
            prefs.example.buttonWidth,
            title = "Button Size",
            min = 0,
            max = 100,
            stepIncrement = 5,
            unit = "{v} dp",
        )
        DialogSliderPreference(
            prefs.example.mainFontSize,
            title = "Main Font Size",
            min = 0.0,
            max = 100.0,
            stepIncrement = 5.0,
            unit = "{v} sp",
        )
        DialogSliderPreference(
            prefs.example.fontSize,
            title = "Font Size",
            min = 0.0f,
            max = 100.0f,
            stepIncrement = 5.0f,
            unit = "{v} sp",
        )
    }
    ListPreference(
        prefs.example.title,
        title = "Some lengthy title about this entry some lengthy title about this entry.",
        entries = listOf(
            entry(
                key = "str1",
                label = "String 1",
                description = "Some lengthy description about this entry.",
                showDescriptionOnlyIfSelected = true,
            ),
            entry(
                key = "str2",
                label = "String 2",
                description = "Some lengthy description about this entry.",
                showDescriptionOnlyIfSelected = true,
            ),
            entry(
                key = "str3",
                label = "String 3",
                description = "Some lengthy description about this entry.",
                showDescriptionOnlyIfSelected = true,
            ),
        ),
    )
    ListPreference(
        listPref = prefs.example.title,
        switchPref = prefs.example.showTitle,
        title = "Some lengthy title about this entry some lengthy title about this entry.",
        summarySwitchDisabled = "off",
        entries = listOf(
            entry(
                key = "str1",
                label = "String 1",
                description = "Some lengthy description about this entry.",
                showDescriptionOnlyIfSelected = true,
            ),
            entry(
                key = "str2",
                label = "String 2",
                description = "Some lengthy description about this entry.",
                showDescriptionOnlyIfSelected = true,
            ),
            entry(
                key = "str3",
                label = "String 3",
                description = "Some lengthy description about this entry.",
                showDescriptionOnlyIfSelected = true,
            ),
        ),
    )
}
