package dev.patrickgold.jetpref.example

import dev.patrickgold.jetpref.datastore.model.PreferenceModel
import dev.patrickgold.jetpref.datastore.model.PreferenceSerializer

enum class Step {
    ONE,
    TWO,
    THREE;
}

class AppPrefs : PreferenceModel("test-file") {
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
        val fontSize = float(
            key = "test__font_size",
            default = 10.0f,
        )
        val buttonSize = int(
            key = "test__button_size",
            default = 10,
        )
        val title = string(
            key = "test__title",
            default = "Hello!",
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
