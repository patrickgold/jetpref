package dev.patrickgold.jetpref.ui.compose

import androidx.annotation.DrawableRes
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ListItem
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import dev.patrickgold.jetpref.datastore.model.PreferenceData
import dev.patrickgold.jetpref.datastore.model.observeAsState

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SwitchPreference(
    @DrawableRes iconId: Int? = null,
    iconSpaceReserved: Boolean = false,
    data: PreferenceData<Boolean>,
    evaluateEnabled: () -> Boolean = { true },
    evaluateVisible: () -> Boolean = { true },
    title: String,
    summary: String? = null,
    summaryOn: String? = null,
    summaryOff: String? = null,
) {
    val pref = data.observeAsState()
    if (evaluateVisible()) {
        ListItem(
            icon = maybeJetIcon(iconId, iconSpaceReserved),
            text = { Text(title) },
            secondaryText = maybeJetText(when {
                pref.value && summaryOn != null -> summaryOn
                !pref.value && summaryOff != null -> summaryOff
                summary != null -> summary
                else -> null
            }),
            trailing = {
                Switch(
                    checked = pref.value,
                    onCheckedChange = null
                )
            },
            modifier = Modifier.toggleable(
                value = pref.value,
                enabled = evaluateEnabled(),
                role = Role.Switch,
                onValueChange = { data.set(it) }
            )
        )
    }
}
