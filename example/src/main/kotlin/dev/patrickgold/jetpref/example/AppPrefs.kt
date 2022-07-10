package dev.patrickgold.jetpref.example

import dev.patrickgold.jetpref.datastore.JetPref
import dev.patrickgold.jetpref.datastore.model.PreferenceModel
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
    }
}
