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
import dev.patrickgold.jetpref.datastore.preferenceModel
import dev.patrickgold.jetpref.example.AppPrefs
import dev.patrickgold.jetpref.example.R
import dev.patrickgold.jetpref.ui.compose.DialogSliderPreference
import dev.patrickgold.jetpref.ui.compose.ListPreference
import dev.patrickgold.jetpref.ui.compose.Preference
import dev.patrickgold.jetpref.ui.compose.PreferenceGroup
import dev.patrickgold.jetpref.ui.compose.PreferenceLayout
import dev.patrickgold.jetpref.ui.compose.SwitchPreference
import dev.patrickgold.jetpref.ui.compose.entry
import java.util.*

@Composable
fun HomeScreen() = PreferenceLayout(preferenceModel(AppPrefs::class, ::AppPrefs)) {
    Preference(
        iconId = R.drawable.ic_question_answer_black_24dp,
        title = "Hello",
        summary = "Test",
        onClick = { },
    )
    ListPreference(
        prefs.language,
        title = "Language",
        entries = listOf(
            "auto",
            "ar",
            "bg",
            "bs",
            "ca",
            "ckb-IR",
            "cs",
            "da",
            "de",
            "el",
            "en",
            "eo",
            "es",
            "fa",
            "fi",
            "fr",
            "hr",
            "hu",
            "in",
            "it",
            "iw",
            "kmr-TR",
            "ko-KR",
            "lv-LV",
            "mk",
            "nds-DE",
            "nl",
            "no",
            "pl",
            "pt",
            "pt-BR",
            "ru",
            "sk",
            "sl",
            "sr",
            "sv",
            "tr",
            "uk",
            "zgh",
        ).map {
            if (it == "auto") {
                entry(
                    key = "auto",
                    label = "default",
                )
            } else {
                entry(
                    key = it,
                    label = Locale(it).getDisplayName(Locale(it))
                )
            }
        },
    )
    Preference(
        iconId = R.drawable.ic_question_answer_black_24dp,
        title = "This is a very very long sentence which does not fit in a title",
        summary = "This is an even longer description which spans over multiple lines and is way too long. This is an even longer description which spans over multiple lines and is way too long.",
        onClick = { },
    )
    Preference(
        iconId = R.drawable.ic_question_answer_black_24dp,
        title = "This is a very very long sentence which does not fit in a title",
        summary = "This is an even longer description which spans over multiple lines and is way too long. This is an even longer description which spans over multiple lines and is way too long.",
        onClick = { },
    )
    Preference(
        iconId = R.drawable.ic_question_answer_black_24dp,
        title = "This is a very very long sentence which does not fit in a title",
        summary = "This is an even longer description which spans over multiple lines and is way too long. This is an even longer description which spans over multiple lines and is way too long.",
        onClick = { },
    )
    SwitchPreference(
        prefs.showTestGroup,
        iconId = R.drawable.ic_question_answer_black_24dp,
        title = "Show Test Group",
    )
    PreferenceGroup(title = "Hello", visibleIf = { prefs.showTestGroup isEqualTo true }) {
        SwitchPreference(
            prefs.test.isButtonShowing,
            title = "isBtnShow",
        )
        SwitchPreference(
            prefs.test.isButtonShowing2,
            iconId = R.drawable.ic_question_answer_black_24dp,
            title = "isBtnShow2",
            summaryOn = "Hello",
            summaryOff = "Bye",
            enabledIf = { prefs.test.isButtonShowing isEqualTo true },
        )
        DialogSliderPreference(
            prefs.test.buttonSize,
            title = "Button Size",
            min = 0,
            max = 100,
            stepIncrement = 1,
            unit = "{v}%",
        )
        DialogSliderPreference(
            prefs.test.buttonWidth,
            title = "Button Size",
            min = 0,
            max = 100,
            stepIncrement = 5,
            unit = "{v} dp",
        )
        DialogSliderPreference(
            prefs.test.mainFontSize,
            title = "Main Font Size",
            min = 0.0,
            max = 100.0,
            stepIncrement = 5.0,
            unit = "{v} sp",
        )
        DialogSliderPreference(
            prefs.test.fontSize,
            title = "Font Size",
            min = 0.0f,
            max = 100.0f,
            stepIncrement = 5.0f,
            unit = "{v} sp",
        )
    }
    ListPreference(
        prefs.test.title,
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
        listPref = prefs.test.title,
        switchPref = prefs.test.showTitle,
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
