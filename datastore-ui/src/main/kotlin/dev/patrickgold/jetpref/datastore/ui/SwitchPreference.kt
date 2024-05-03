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

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.semantics.Role
import dev.patrickgold.jetpref.datastore.model.PreferenceData
import dev.patrickgold.jetpref.datastore.model.PreferenceDataEvaluator
import dev.patrickgold.jetpref.datastore.model.PreferenceDataEvaluatorScope
import dev.patrickgold.jetpref.datastore.model.PreferenceModel
import dev.patrickgold.jetpref.datastore.model.observeAsState
import dev.patrickgold.jetpref.material.ui.JetPrefListItem

/**
 * Material switch preference which provides a list item with a trailing switch.
 *
 * @param pref The boolean preference data entry from the datastore.
 * @param modifier Modifier to be applied to the underlying list item.
 * @param icon The [ImageVector] of the list entry.
 * @param title The title of this preference, shown as the list item primary text (max 1 line).
 * @param summary The summary of this preference, shown as the list item secondary text (max 2 lines).
 * @param summaryOn The summary of this preference if the state is `true`. If this is specified it will override
 *     provided [summary]. Shown as the list item secondary text (max 2 lines).
 * @param summaryOff The summary of this preference if the state is `false`. If this is specified it will override
 *     provided [summary]. Shown as the list item secondary text (max 2 lines).
 * @param enabledIf Evaluator scope which allows to dynamically decide if this preference should be enabled (true) or
 *     disabled (false).
 * @param visibleIf Evaluator scope which allows to dynamically decide if this preference should be visible (true) or
 *     hidden (false).
 * @see maybeJetIcon
 * @since 0.1.0
 */
@Composable
fun <T : PreferenceModel> PreferenceUiScope<T>.SwitchPreference(
    pref: PreferenceData<Boolean>,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
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
            icon = maybeJetIcon(imageVector = icon, iconSpaceReserved = iconSpaceReserved),
            text = title,
            secondaryText = when {
                prefValue && summaryOn != null -> summaryOn
                !prefValue && summaryOff != null -> summaryOff
                summary != null -> summary
                else -> null
            },
            trailing = {
                Switch(
                    modifier = Modifier.size(LocalViewConfiguration.current.minimumTouchTargetSize),
                    checked = prefValue,
                    onCheckedChange = null,
                    enabled = isEnabled
                )
            },
            enabled = isEnabled,
        )
    }
}
