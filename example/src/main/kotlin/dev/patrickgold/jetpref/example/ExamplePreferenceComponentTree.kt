package dev.patrickgold.jetpref.example

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FormatPaint
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import dev.patrickgold.jetpref.datastore.component.PreferenceComponentScreen
import dev.patrickgold.jetpref.datastore.component.PreferenceComponentTree
import dev.patrickgold.jetpref.datastore.component.buildScreen
import dev.patrickgold.jetpref.example.ui.theme.Theme
import dev.patrickgold.jetpref.example.ui.theme.defaultColors

object ExamplePreferenceComponentTree : PreferenceComponentTree<ExamplePreferenceModel>(ExamplePreferenceStore) {
    override val mainEntryPoint: PreferenceComponentScreen
        get() = HomeScreen

    val ColorPickerDemoScreen = buildScreen(title = { "Color Picker Demo" }) {
        //
    }

    val HomeScreen = buildScreen(title = { "Home Screen" }) {
        addNavigationTo(ColorPickerDemoScreen)
        addListPicker(
            listPref = prefs.theme,
            icon = { Icons.Default.Palette },
            title = { "Theme" },
            entries = { Theme.listEntries() },
        )

        addGroup(title = { "Sliders" }) {
            addSlider(
                pref = prefs.boxFontSize,
                title = { "Box font size" },
                valueLabel = { "$it sp" },
                min = 0.0f,
                max = 100.0f,
                stepIncrement = 5.0f,
            )
            addDualSlider(
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

        addComposableContent {
            HorizontalDivider()
        }

        addLocalTimePicker(
            pref = prefs.exampleTime,
            title = { "Example time picker" },
        )
        addTextField(
            pref = prefs.exampleText,
            title = { "Example test" },
            summaryIfBlank = { "(blank)" },
            summaryIfEmpty = { "(empty)" },
        )

        addComposableContent {
            HorizontalDivider()
        }

        addSwitch(
            pref = prefs.accentColors.show,
            icon = { ImageVector.vectorResource(R.drawable.ic_question_answer_black_24dp) },
            title = { "Show example group" },
            summary = { "Show/hide the example group" },
        )
        addGroup(
            title = { "Accent colors"},
            visibleIf = { prefs.accentColors.show isEqualTo true },
        ) {
            addColorPicker(
                pref = prefs.accentColors.color1,
                title = { "Accent color 1" },
                summary = { "Without advanced and alpha" },
                defaultValueLabel = { "Default" },
                icon = { Icons.Default.FormatPaint },
                showAlphaSlider = false,
                enableAdvancedLayout = false,
                defaultColors = defaultColors,
            )
            addColorPicker(
                pref = prefs.accentColors.color2,
                title = { "Accent color 2" },
                summary = { "Without advanced with alpha" },
                defaultValueLabel = { "Default" },
                icon = { Icons.Default.FormatPaint },
                showAlphaSlider = true,
                enableAdvancedLayout = false,
                defaultColors = defaultColors,
            )
            addColorPicker(
                pref = prefs.accentColors.color3,
                title = { "Accent color 3" },
                summary = { "With advanced without alpha" },
                defaultValueLabel = { "Default" },
                icon = { Icons.Default.FormatPaint },
                showAlphaSlider = false,
                enableAdvancedLayout = true,
                defaultColors = defaultColors,
            )
            addColorPicker(
                pref = prefs.accentColors.color4,
                title = { "Accent color 4" },
                summary = { "With advanced and alpha" },
                defaultValueLabel = { "Default" },
                icon = { Icons.Default.FormatPaint },
                showAlphaSlider = true,
                enableAdvancedLayout = true,
                defaultColors = defaultColors,
            )
        }
    }
}
