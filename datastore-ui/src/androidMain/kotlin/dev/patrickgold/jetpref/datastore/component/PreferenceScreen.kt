package dev.patrickgold.jetpref.datastore.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import dev.patrickgold.jetpref.datastore.model.PreferenceDataEvaluator

interface PreferenceScreen : PreferenceGroup

internal data class PreferenceScreenImpl(
    override val title: @Composable () -> String,
    override val components: List<PreferenceComponent>,
) : PreferenceScreen {
    override val icon: @Composable (() -> ImageVector)? = null
    override val enabledIf: PreferenceDataEvaluator = { true }
    override val visibleIf: PreferenceDataEvaluator = { true }
}
