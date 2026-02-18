package dev.patrickgold.jetpref.example

import dev.patrickgold.jetpref.datastore.component.PreferenceScreen
import dev.patrickgold.jetpref.datastore.component.PreferenceComponentTree
import dev.patrickgold.jetpref.example.ui.settings.HomeScreen

object ExamplePreferenceComponentTree : PreferenceComponentTree() {
    override val mainEntryPoint: PreferenceScreen
        get() = HomeScreen
}
