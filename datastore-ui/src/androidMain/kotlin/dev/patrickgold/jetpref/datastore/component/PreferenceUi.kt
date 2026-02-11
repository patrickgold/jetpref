package dev.patrickgold.jetpref.datastore.component

import androidx.compose.runtime.Composable
import dev.patrickgold.jetpref.datastore.model.PreferenceModel
import dev.patrickgold.jetpref.datastore.runtime.DataStore

abstract class PreferenceUi<T : PreferenceModel>(store: DataStore<T>) {
    abstract val mainEntryPoint: PreferenceScreen

    val prefs by store

    fun declareScreen(
        title: @Composable () -> String,
        block: PreferenceGroupBuilder.() -> Unit,
    ): PreferenceScreen {
        val builder = PreferenceGroupBuilder()
        builder.block()
        return PreferenceScreenImpl(title, builder.components.toList())
    }
}
