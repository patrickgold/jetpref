package dev.patrickgold.jetpref.example

import dev.patrickgold.jetpref.datastore.model.PreferenceModel
import dev.patrickgold.jetpref.datastore.model.PreferenceSerializer

enum class Step {
    ONE,
    TWO,
    THREE;
}

class AppPrefs : PreferenceModel("test-file") {
    var showTestGroup = boolean(
        key = "show_test_group",
        default = true,
    )
    val language = string(
        key = "language",
        default = "auto",
    )
    val boxSizePortrait = int(
        key = "box_size_portrait",
        default = 40,
    )
    val boxSizeLandscape = int(
        key = "box_size_landscape",
        default = 20,
    )
    val test = Test()
    inner class Test {
        var isButtonShowing = boolean(
            key = "test__is_button_showing",
            default = true,
        )
        var isButtonShowing2 = boolean(
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
        val step = custom(
            key = "test__step",
            default = Step.ONE,
            serializer = object : PreferenceSerializer<Step> {
                override fun serialize(value: Step): String = value.toString()
                override fun deserialize(value: String): Step = Step.valueOf(value)
            }
        )
    }
}
