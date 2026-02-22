package dev.patrickgold.jetpref.example.ui.settings

import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import dev.patrickgold.jetpref.datastore.component.PreferenceScreen
import dev.patrickgold.jetpref.example.ExamplePreferenceStore
import dev.patrickgold.jetpref.example.R
import dev.patrickgold.jetpref.example.ui.theme.Theme
import dev.patrickgold.jetpref.example.ui.theme.defaultColors

data object HomeScreen : PreferenceScreen({
    title { "Settings" }

    components {
        val prefs by ExamplePreferenceStore

        navigationTo(SearchScreen)
        navigationTo(ColorPickerDemoScreen)
        navigationTo(AdditionalScreen)
        navigationTo(VisualizeSearchIndexScreen)
        listPicker(
            listPref = prefs.theme,
            icon = { ImageVector.vectorResource(R.drawable.ic_palette) },
            title = { "Theme" },
            entries = Theme.listEntries(),
        )

        group(title = { "Sliders" }) {
            slider(
                pref = prefs.boxFontSize,
                title = { "Box font size" },
                valueLabel = { "$it sp" },
                min = 0.0f,
                max = 100.0f,
                stepIncrement = 5.0f,
            )
            dualSlider(
                pref1 = prefs.boxSizePortrait,
                pref2 = prefs.boxSizeLandscape,
                title = { "Box size (dual example)" },
                pref1Label = { "Portrait" },
                pref2Label = { "Landscape" },
                valueLabel = { if (it == -1) "Automatic" else "$it%" },
                min = -1,
                max = 100,
                stepIncrement = 1,
            )
        }

        content {
            HorizontalDivider()
        }

        localTimePicker(
            pref = prefs.exampleTime,
            title = { "Example time picker" },
        )
        textField(
            pref = prefs.exampleText,
            title = { "Example test" },
            summaryIfBlank = { "(blank)" },
            summaryIfEmpty = { "(empty)" },
        )

        content {
            HorizontalDivider()
        }

        switch(
            pref = prefs.accentColors.show,
            icon = { ImageVector.vectorResource(R.drawable.ic_question_answer_black_24dp) },
            title = { "Show example group" },
            summary = { "Show/hide the example group" },
        )
        group(
            title = { "Accent colors" },
            visibleIf = { prefs.accentColors.show isEqualTo true },
        ) {
            colorPicker(
                pref = prefs.accentColors.color1,
                title = { "Accent color 1" },
                summary = { "Without advanced and alpha" },
                defaultValueLabel = { "Default" },
                icon = { ImageVector.vectorResource(R.drawable.ic_format_paint) },
                showAlphaSlider = false,
                enableAdvancedLayout = false,
                defaultColors = defaultColors,
            )
            colorPicker(
                pref = prefs.accentColors.color2,
                title = { "Accent color 2" },
                summary = { "Without advanced with alpha" },
                defaultValueLabel = { "Default" },
                icon = { ImageVector.vectorResource(R.drawable.ic_format_paint) },
                showAlphaSlider = true,
                enableAdvancedLayout = false,
                defaultColors = defaultColors,
            )
            colorPicker(
                pref = prefs.accentColors.color3,
                title = { "Accent color 3" },
                summary = { "With advanced without alpha" },
                defaultValueLabel = { "Default" },
                icon = { ImageVector.vectorResource(R.drawable.ic_format_paint) },
                showAlphaSlider = false,
                enableAdvancedLayout = true,
                defaultColors = defaultColors,
            )
            colorPicker(
                pref = prefs.accentColors.color4,
                title = { "Accent color 4" },
                summary = { "With advanced and alpha" },
                defaultValueLabel = { "Default" },
                icon = { ImageVector.vectorResource(R.drawable.ic_format_paint) },
                showAlphaSlider = true,
                enableAdvancedLayout = true,
                defaultColors = defaultColors,
            )
        }
    }
})
