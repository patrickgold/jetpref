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

import android.util.Log
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FormatPaint
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import dev.patrickgold.jetpref.datastore.model.observeAsState
import dev.patrickgold.jetpref.datastore.ui.ColorPickerPreference
import dev.patrickgold.jetpref.datastore.ui.DialogSliderPreference
import dev.patrickgold.jetpref.datastore.ui.ExperimentalJetPrefDatastoreUi
import dev.patrickgold.jetpref.datastore.ui.ListPreference
import dev.patrickgold.jetpref.datastore.ui.Preference
import dev.patrickgold.jetpref.datastore.ui.PreferenceGroup
import dev.patrickgold.jetpref.datastore.ui.ScrollablePreferenceLayout
import dev.patrickgold.jetpref.datastore.ui.SwitchPreference
import dev.patrickgold.jetpref.datastore.ui.TextFieldPreference
import dev.patrickgold.jetpref.datastore.ui.isMaterialYou
import dev.patrickgold.jetpref.datastore.ui.listPrefEntries
import dev.patrickgold.jetpref.datastore.ui.vectorResource
import dev.patrickgold.jetpref.example.LocalNavController
import dev.patrickgold.jetpref.example.R
import dev.patrickgold.jetpref.example.examplePreferenceModel
import dev.patrickgold.jetpref.example.ui.theme.Theme

@OptIn(ExperimentalJetPrefDatastoreUi::class)
@Composable
fun HomeScreen() = ScrollablePreferenceLayout(examplePreferenceModel()) {
    val navController = LocalNavController.current
    val context = LocalContext.current

    val isDatastoreReady by prefs.datastoreReadyStatus.observeAsState()
    Text(text = "is datastore ready = $isDatastoreReady")

    Preference(
        onClick = { navController.navigate("color-picker-demo") },
        title = "Color Picker Demo",
    )
    ListPreference(
        prefs.theme,
        icon = Icons.Default.Palette,
        title = "Theme",
        entries = Theme.listEntries(),
    )
    ColorPickerPreference(
        pref = prefs.accentColor,
        title = "Accent Color",
        summary = "Without advanced and alpha",
        defaultValueLabel = "Default",
        icon = Icons.Default.FormatPaint,
        defaultColors = arrayOf<Color>(
            Color(0xFFF44336), // RED 500
            Color(0xFFE91E63), // PINK 500
            Color(0xFFFF2C93), // LIGHT PINK 500
            Color(0xFF9C27B0), // PURPLE 500
            Color(0xFF673AB7), // DEEP PURPLE 500
            Color(0xFF3F51B5), // INDIGO 500
            Color(0xFF2196F3), // BLUE 500
            Color(0xFF03A9F4), // LIGHT BLUE 500
            Color(0xFF00BCD4), // CYAN 500
            Color(0xFF009688), // TEAL 500
            Color(0xFF4CAF50), // GREEN 500
            Color(0xFF8BC34A), // LIGHT GREEN 500
            Color(0xFFCDDC39), // LIME 500
            Color(0xFFFFEB3B), // YELLOW 500
            Color(0xFFFFC107), // AMBER 500
            Color(0xFFFF9800), // ORANGE 500
            Color(0xFF795548), // BROWN 500
            Color(0xFF607D8B), // BLUE GREY 500
            Color(0xFF9E9E9E), // GREY 500
        ),
        showAlphaSlider = false,
        enableAdvancedLayout = false,
        colorOverride = {
            if (it.isMaterialYou(context)) {
                Color.Unspecified
            } else {
                it
            }
        }
    )
    ColorPickerPreference(
        pref = prefs.accentColor,
        title = "Accent Color",
        summary = "Without advanced with alpha",
        defaultValueLabel = "Default",
        icon = Icons.Default.FormatPaint,
        defaultColors = arrayOf<Color>(
            Color(0xFFF44336), // RED 500
            Color(0xFFE91E63), // PINK 500
            Color(0xFFFF2C93), // LIGHT PINK 500
            Color(0xFF9C27B0), // PURPLE 500
            Color(0xFF673AB7), // DEEP PURPLE 500
            Color(0xFF3F51B5), // INDIGO 500
            Color(0xFF2196F3), // BLUE 500
            Color(0xFF03A9F4), // LIGHT BLUE 500
            Color(0xFF00BCD4), // CYAN 500
            Color(0xFF009688), // TEAL 500
            Color(0xFF4CAF50), // GREEN 500
            Color(0xFF8BC34A), // LIGHT GREEN 500
            Color(0xFFCDDC39), // LIME 500
            Color(0xFFFFEB3B), // YELLOW 500
            Color(0xFFFFC107), // AMBER 500
            Color(0xFFFF9800), // ORANGE 500
            Color(0xFF795548), // BROWN 500
            Color(0xFF607D8B), // BLUE GREY 500
            Color(0xFF9E9E9E), // GREY 500
        ),
        showAlphaSlider = true,
        enableAdvancedLayout = false,
        colorOverride = {
            if (it.isMaterialYou(context)) {
                Color.Unspecified
            } else {
                it
            }
        }
    )
    ColorPickerPreference(
        pref = prefs.accentColor,
        title = "Accent Color",
        summary = "With advanced without alpha",
        defaultValueLabel = "Default",
        icon = Icons.Default.FormatPaint,
        defaultColors = arrayOf<Color>(
            Color(0xFFF44336), // RED 500
            Color(0xFFE91E63), // PINK 500
            Color(0xFFFF2C93), // LIGHT PINK 500
            Color(0xFF9C27B0), // PURPLE 500
            Color(0xFF673AB7), // DEEP PURPLE 500
            Color(0xFF3F51B5), // INDIGO 500
            Color(0xFF2196F3), // BLUE 500
            Color(0xFF03A9F4), // LIGHT BLUE 500
            Color(0xFF00BCD4), // CYAN 500
            Color(0xFF009688), // TEAL 500
            Color(0xFF4CAF50), // GREEN 500
            Color(0xFF8BC34A), // LIGHT GREEN 500
            Color(0xFFCDDC39), // LIME 500
            Color(0xFFFFEB3B), // YELLOW 500
            Color(0xFFFFC107), // AMBER 500
            Color(0xFFFF9800), // ORANGE 500
            Color(0xFF795548), // BROWN 500
            Color(0xFF607D8B), // BLUE GREY 500
            Color(0xFF9E9E9E), // GREY 500
        ),
        showAlphaSlider = false,
        enableAdvancedLayout = true,
        colorOverride = {
            if (it.isMaterialYou(context)) {
                Color.Unspecified
            } else {
                it
            }
        }
    )
    ColorPickerPreference(
        pref = prefs.accentColor,
        title = "Accent Color",
        summary = "With advanced and alpha",
        defaultValueLabel = "Default",
        icon = Icons.Default.FormatPaint,
        defaultColors = arrayOf<Color>(
            Color(0xFFF44336), // RED 500
            Color(0xFFE91E63), // PINK 500
            Color(0xFFFF2C93), // LIGHT PINK 500
            Color(0xFF9C27B0), // PURPLE 500
            Color(0xFF673AB7), // DEEP PURPLE 500
            Color(0xFF3F51B5), // INDIGO 500
            Color(0xFF2196F3), // BLUE 500
            Color(0xFF03A9F4), // LIGHT BLUE 500
            Color(0xFF00BCD4), // CYAN 500
            Color(0xFF009688), // TEAL 500
            Color(0xFF4CAF50), // GREEN 500
            Color(0xFF8BC34A), // LIGHT GREEN 500
            Color(0xFFCDDC39), // LIME 500
            Color(0xFFFFEB3B), // YELLOW 500
            Color(0xFFFFC107), // AMBER 500
            Color(0xFFFF9800), // ORANGE 500
            Color(0xFF795548), // BROWN 500
            Color(0xFF607D8B), // BLUE GREY 500
            Color(0xFF9E9E9E), // GREY 500
        ),
        showAlphaSlider = true,
        enableAdvancedLayout = true,
        colorOverride = {
            if (it.isMaterialYou(context)) {
                Color.Unspecified
            } else {
                it
            }
        }
    )
    DialogSliderPreference(
        primaryPref = prefs.boxSizePortrait,
        secondaryPref = prefs.boxSizeLandscape,
        title = "Example integer slider",
        valueLabel = { if (it == -1) "Automatic" else "$it%" },
        primaryLabel = "Portrait",
        secondaryLabel = "Landscape",
        min = -1,
        max = 100,
        stepIncrement = 1,
        // Tapping causes an incorrect state to be print, see https://issuetracker.google.com/issues/181415195
        // Dragging works fine though
        onPreviewSelectedPrimaryValue = { Log.d("preview primary", it.toString()) },
        onPreviewSelectedSecondaryValue = { Log.d("preview secondary", it.toString()) },
    )
    SwitchPreference(
        prefs.showExampleGroup,
        icon = vectorResource(R.drawable.ic_question_answer_black_24dp),
        title = "Show example group",
        summary = "Show/hide the example group",
    )
    PreferenceGroup(
        title = "Example group",
        visibleIf = { prefs.showExampleGroup isEqualTo true },
        iconSpaceReserved = true,
    ) {
        SwitchPreference(
            prefs.example.isButtonShowing,
            title = "isBtnShow",
        )
        SwitchPreference(
            prefs.example.isButtonShowing2,
            icon = vectorResource(R.drawable.ic_question_answer_black_24dp),
            title = "isBtnShow2",
            summaryOn = "Hello",
            summaryOff = "Bye",
            enabledIf = { prefs.example.isButtonShowing isEqualTo true },
        )
        DialogSliderPreference(
            prefs.example.buttonSize,
            title = "Button Size",
            valueLabel = { "$it%" },
            min = 0,
            max = 100,
            stepIncrement = 1,
        )
        DialogSliderPreference(
            prefs.example.buttonWidth,
            title = "Button Size",
            valueLabel = { "$it dp" },
            min = 0,
            max = 100,
            stepIncrement = 5,
        )
        DialogSliderPreference(
            prefs.example.mainFontSize,
            title = "Main Font Size",
            valueLabel = { "$it sp" },
            min = 0.0,
            max = 100.0,
            stepIncrement = 5.0,
        )
        DialogSliderPreference(
            prefs.example.fontSize,
            title = "Font Size",
            valueLabel = { "$it sp" },
            min = 0.0f,
            max = 100.0f,
            stepIncrement = 5.0f,
        )
    }
    ListPreference(
        prefs.example.title,
        title = "Some lengthy title about this entry some lengthy title about this entry.",
        entries = listPrefEntries {
            entry(
                key = "str1",
                label = "String 1",
                description = "Some lengthy description about this entry.",
                showDescriptionOnlyIfSelected = true,
            )
            entry(
                key = "str2",
                label = "String 2",
                description = "Some lengthy description about this entry.",
                showDescriptionOnlyIfSelected = true,
            )
            entry(
                key = "str3",
                label = "String 3",
                description = "Some lengthy description about this entry.",
                showDescriptionOnlyIfSelected = true,
            )
        },
    )
    ListPreference(
        listPref = prefs.example.title,
        switchPref = prefs.example.showTitle,
        title = "Some lengthy title about this entry some lengthy title about this entry.",
        summarySwitchDisabled = "off",
        entries = listPrefEntries {
            entry(
                key = "str1",
                label = "String 1",
                description = "Some lengthy description about this entry.",
                showDescriptionOnlyIfSelected = true,
            )
            entry(
                key = "str2",
                label = "String 2",
                description = "Some lengthy description about this entry.",
                showDescriptionOnlyIfSelected = true,
            )
            entry(
                key = "str3",
                label = "String 3",
                description = "Some lengthy description about this entry.",
                showDescriptionOnlyIfSelected = true,
            )
        },
    )
    TextFieldPreference(
        prefs.example.description,
        title = "Description",
        summaryIfBlank = "(blank)",
        summaryIfEmpty = "(empty)",
    )
    TextFieldPreference(
        prefs.example.itemKey,
        title = "Item key",
        validateValue = {
            "[a-z0-9_]+".toRegex().matches(it) || error("Invalid key")
        },
        transformValue = { it.trim() },
    )
    ListPreference(
        listPref = prefs.example.longListPref,
        title = "Test the scroll behaviour",
        entries = listPrefEntries {
            entry(
                "str1",
                "String 1"
            )
            entry(
                "str2",
                "String 2"
            )
            entry(
                "str3",
                "String 3"
            )
            entry(
                "str4",
                "String 4"
            )
            entry(
                "str5",
                "String 5"
            )
            entry(
                "str6",
                "String 6"
            )
            entry(
                "str7",
                "String 7"
            )
            entry(
                "str8",
                "String 8"
            )
            entry(
                "str9",
                "String 9"
            )
            entry(
                "str10",
                "String 10"
            )
        }
    )
}
