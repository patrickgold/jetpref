package dev.patrickgold.jetpref.example

import android.os.Build
import androidx.compose.ui.graphics.Color
import dev.patrickgold.jetpref.datastore.JetPref
import dev.patrickgold.jetpref.datastore.model.LocalTime
import dev.patrickgold.jetpref.datastore.model.PreferenceMigrationEntry
import dev.patrickgold.jetpref.datastore.model.PreferenceModel
import dev.patrickgold.jetpref.datastore.model.PreferenceSerializer
import dev.patrickgold.jetpref.datastore.model.PreferenceType
import dev.patrickgold.jetpref.example.ui.theme.Theme

// Defining a getter function for easy retrieval of the AppPrefs model.
// You can name this however you want, the convention is <projectName>PreferenceModel
fun examplePreferenceModel() = JetPref.getOrCreatePreferenceModel(AppPrefs::class, ::AppPrefs)

// Defining a preference model for our app prefs
// The name we give here is the file name of the preferences and is saved
// within the app's `jetpref_datastore` directory.
class AppPrefs : PreferenceModel("example-app-preferences") {
    val theme = enum(
        key = "theme",
        default = Theme.AUTO,
    )
    val color1 = custom(
        key = "accent_color1",
        default = when (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            true -> Color.Unspecified
            false -> Color.Red
        },
        serializer = ColorPreferenceSerializer,
    )
    val color2 = custom(
        key = "accent_color2",
        default = when (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            true -> Color.Unspecified
            false -> Color.Red
        },
        serializer = ColorPreferenceSerializer,
    )
    val color3 = custom(
        key = "accent_color3",
        default = when (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            true -> Color.Unspecified
            false -> Color.Red
        },
        serializer = ColorPreferenceSerializer,
    )
    val color4 = custom(
        key = "accent_color4",
        default = when (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            true -> Color.Unspecified
            false -> Color.Red
        },
        serializer = ColorPreferenceSerializer,
    )
    val boxSizePortrait = int(
        key = "box_size_portrait",
        default = 40,
    )
    val boxSizeLandscape = int(
        key = "box_size_landscape",
        default = 20,
    )
    val showExampleGroup = boolean(
        key = "show_example_group",
        default = true,
    )

    // You can also define groups for preferences by packing them into an inner class like below. Groups are only for
    // improved usage in your code, for the model this is completely irrelevant. Thus a key must still be completely
    // unique to not only this group, but all groups in this model.
    val example = Example()
    inner class Example {
        val isButtonShowing = boolean(
            key = "test__is_button_showing",
            default = true,
        )
        val isButtonShowing2 = boolean(
            key = "test__is_button_showing2",
            default = true,
        )
        val mainFontSize = double(
            key = "test__main_font_size",
            default = 20.0,
        )
        val fontSize = float(
            key = "test__font_size",
            default = 30.0f,
        )
        val buttonSize = int(
            key = "test__button_size",
            default = 10,
        )
        val buttonWidth = long(
            key = "test__button_width",
            default = 40,
        )
        val title = string(
            key = "test__title",
            default = "str1",
        )
        val showTitle = boolean(
            key = "test__show_title",
            default = true,
        )
        val description = string(
            key = "test__description",
            default = "",
        )
        val itemKey = string(
            key = "test__item_key",
            default = "abc_item_key",
        )

        val longListPref = string(
            key = "test__long_list",
            default = "str1"
        )

        val time = time(
            key = "test__time",
            default = LocalTime(18, 0)
        )
    }

    // To migrate preferences, this method can be overridden (optional, if not overridden all entries are kept as is).
    override fun migrate(entry: PreferenceMigrationEntry): PreferenceMigrationEntry {
        return when {
            // Given migration example situation: The app theme was previously saved as either AUTO, DAY or NIGHT, but
            // since then it has changed to AUTO, LIGHT and DARK. As such we need to transform the DAY and NIGHT values.
            entry.key == "theme" && entry.rawValue == "DAY" -> entry.transform(rawValue = Theme.LIGHT.toString())
            entry.key == "theme" && entry.rawValue == "NIGHT" -> entry.transform(rawValue = Theme.DARK.toString())

            // Given migration example situation: We renamed a preference.
            entry.key == "show_group" -> entry.transform(key = "show_example_group")

            // Given migration example situation: We expanded and renamed a simple switch pref to a list pref
            entry.key == "foo_box_enabled" -> entry.transform(
                type = PreferenceType.string(), // Important: we change the type and thus must set the new one!
                key = "foo_box_mode", // New key
                rawValue = if (entry.rawValue.toBoolean()) "ENABLED_COLLAPSING_MODE" else "DISABLED", // New value
            )

            // Given migration example situation: We changed the value format of a pref and want to reset the pref
            // value it is in the old format (e.g. if there's a certain character in it, you can also use regex...)
            // You could also provide a new value directly via transform(), using reset() however guarantees to reset
            // it back to the default value you set above.
            entry.key == "foo_box_names" && entry.rawValue.contains("#") -> entry.reset()

            // If we have a pref that does not exist nor is needed anymore we need to do nothing, the delete happens
            // automatically!

            // By default we keep each entry as is (you could also return entry directly but this is more readable)
            else -> entry.keepAsIs()
        }
    }
}

object ColorPreferenceSerializer : PreferenceSerializer<Color> {
    @OptIn(ExperimentalStdlibApi::class)
    override fun deserialize(value: String): Color {
        return Color(value.hexToULong())
    }

    @OptIn(ExperimentalStdlibApi::class)
    override fun serialize(value: Color): String = value.value.toHexString()
}
