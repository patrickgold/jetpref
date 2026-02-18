package dev.patrickgold.jetpref.example

import dev.patrickgold.jetpref.datastore.component.PreferenceComponentScreen
import dev.patrickgold.jetpref.datastore.component.PreferenceComponentTree
import dev.patrickgold.jetpref.example.ui.settings.HomeScreen

object ExamplePreferenceComponentTree : PreferenceComponentTree<ExamplePreferenceModel>(ExamplePreferenceStore) {
    override val mainEntryPoint: PreferenceComponentScreen
        get() = HomeScreen
}
