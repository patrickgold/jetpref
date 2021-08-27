package dev.patrickgold.jetpref.example

import dev.patrickgold.jetpref.datastore.JetPrefDataStore
import dev.patrickgold.jetpref.datastore.Singleton
import dev.patrickgold.jetpref.datastore.singleton

enum class Step {
    ONE,
    TWO,
    THREE;
}

class AppPrefs : JetPrefDataStore("test-file") {
    companion object : Singleton<AppPrefs> by singleton({ AppPrefs() })

    val test = Test()
    inner class Test {
        var isButtonShowing = boolean {
            key = "test__is_button_showing"
            defaultValue = true
        }
        val buttonSize = int {
            key = "test__button_size"
            defaultValue = 10
        }
        val title = string {
            key = "test__title"
            defaultValue = "Hello!"
        }
        val step = custom<Step> {
            key = "test__step"
            defaultValue = Step.ONE
            convertFromString = { Step.valueOf(it) }
            convertToString = { it.toString() }
        }
    }

    fun xxx() {
        test.isButtonShowing.value = true
    }
}
