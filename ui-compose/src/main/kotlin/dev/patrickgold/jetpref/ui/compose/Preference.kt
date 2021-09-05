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
import androidx.compose.foundation.clickable
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
import dev.patrickgold.jetpref.datastore.model.PreferenceDataEvaluatorScope
import dev.patrickgold.jetpref.datastore.model.PreferenceModel
import dev.patrickgold.jetpref.datastore.model.observeAsState

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun <T : PreferenceModel> PreferenceUiScope<T>.Preference(
    @DrawableRes iconId: Int? = null,
    iconSpaceReserved: Boolean = this.iconSpaceReserved,
    title: String,
    summary: String? = null,
    enabledIf: PreferenceDataEvaluator = this.enabledIf,
    visibleIf: PreferenceDataEvaluator = this.visibleIf,
    onClick: (() -> Unit)? = null,
) {
    if (visibleIf(PreferenceDataEvaluatorScope.instance())) {
        val isEnabled = enabledIf(PreferenceDataEvaluatorScope.instance())
        ListItem(
            icon = maybeJetIcon(iconId, iconSpaceReserved),
            text = { Text(title) },
            secondaryText = maybeJetText(when {
                summary != null -> summary
                else -> null
            }),
            modifier = if (onClick != null) {
                Modifier.clickable(
                    enabled = isEnabled,
                    role = Role.Button,
                    onClick = onClick,
                )
            } else {
                Modifier
            }
        )
    }
}