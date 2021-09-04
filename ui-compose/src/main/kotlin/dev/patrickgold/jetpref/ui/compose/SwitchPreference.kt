/*
 * Copyright 2021 Patrick Goldinger
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
import dev.patrickgold.jetpref.datastore.model.PreferenceDataEvaluator
import dev.patrickgold.jetpref.datastore.model.observeAsState

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SwitchPreference(
    ref: PreferenceData<Boolean>,
    @DrawableRes iconId: Int? = null,
    iconSpaceReserved: Boolean = false,
    title: String,
    summary: String? = null,
    summaryOn: String? = null,
    summaryOff: String? = null,
    enabledIf: @Composable PreferenceDataEvaluator.() -> Boolean = { true },
    visibleIf: @Composable PreferenceDataEvaluator.() -> Boolean = { true },
) {
    val pref = ref.observeAsState()
    if (visibleIf(PreferenceDataEvaluator.instance())) {
        val isEnabled = enabledIf(PreferenceDataEvaluator.instance())
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
                    onCheckedChange = null,
                    enabled = isEnabled
                )
            },
            modifier = Modifier.toggleable(
                value = pref.value,
                enabled = isEnabled,
                role = Role.Switch,
                onValueChange = { ref.set(it) }
            )
        )
    }
}
