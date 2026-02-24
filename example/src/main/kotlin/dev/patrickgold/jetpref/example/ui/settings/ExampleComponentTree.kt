package dev.patrickgold.jetpref.example.ui.settings

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavKey
import dev.patrickgold.jetpref.datastore.component.PreferenceComponentGroupBuilder
import dev.patrickgold.jetpref.datastore.component.PreferenceComponentTree
import dev.patrickgold.jetpref.datastore.component.PreferencePage
import dev.patrickgold.jetpref.datastore.ui.Preference
import dev.patrickgold.jetpref.example.LocalNavigator

object ExampleComponentTree : PreferenceComponentTree() {
    override val mainEntryPoint: PreferencePage
        get() = HomePage
}

fun PreferenceComponentGroupBuilder.linkedExampleScreen(
    navKey: NavKey,
    title: @Composable () -> String,
) {
    content(title) {
        val navigator = LocalNavigator.current
        Preference(
            title = title(),
            onClick = {
                navigator.navigateTo(navKey)
            },
        )
    }
}
