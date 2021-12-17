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

package dev.patrickgold.jetpref.datastore.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import dev.patrickgold.jetpref.datastore.model.PreferenceData
import dev.patrickgold.jetpref.datastore.model.PreferenceDataEvaluator
import dev.patrickgold.jetpref.datastore.model.PreferenceDataEvaluatorScope
import dev.patrickgold.jetpref.datastore.model.PreferenceModel
import dev.patrickgold.jetpref.datastore.model.observeAsState
import dev.patrickgold.jetpref.material.ui.JetPrefListItem

@Composable
fun <T : PreferenceModel> PreferenceUiScope<T>.SwitchPreference(
    pref: PreferenceData<Boolean>,
    modifier: Modifier = Modifier,
    @DrawableRes iconId: Int? = null,
    iconSpaceReserved: Boolean = this.iconSpaceReserved,
    title: String,
    summary: String? = null,
    summaryOn: String? = null,
    summaryOff: String? = null,
    enabledIf: PreferenceDataEvaluator = { true },
    visibleIf: PreferenceDataEvaluator = { true },
) {
    val prefValue by pref.observeAsState()

    val evalScope = PreferenceDataEvaluatorScope.instance()
    if (this.visibleIf(evalScope) && visibleIf(evalScope)) {
        val isEnabled = this.enabledIf(evalScope) && enabledIf(evalScope)
        JetPrefListItem(
            modifier = modifier
                .toggleable(
                    value = prefValue,
                    enabled = isEnabled,
                    role = Role.Switch,
                    onValueChange = { pref.set(it) }
                ),
            icon = maybeJetIcon(iconId, iconSpaceReserved),
            text = title,
            secondaryText = when {
                prefValue && summaryOn != null -> summaryOn
                !prefValue && summaryOff != null -> summaryOff
                summary != null -> summary
                else -> null
            },
            trailing = {
                Switch(
                    checked = prefValue,
                    onCheckedChange = null,
                    enabled = isEnabled
                )
            },
            enabled = isEnabled,
        )
    }
}
