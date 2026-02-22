/*
 * Copyright 2021-2026 Patrick Goldinger
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.semantics.Role
import dev.patrickgold.jetpref.datastore.component.PreferenceComponent
import dev.patrickgold.jetpref.datastore.model.collectAsState
import kotlinx.coroutines.launch

/**
 * Material switch preference which provides a list item with a trailing switch.
 *
 * @param component Component describing what to display.
 * @param modifier Modifier to be applied to the underlying preference.
 *
 * @since 0.4.0
 */
@Composable
fun SwitchPreference(
    component: PreferenceComponent.Switch,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    val prefValue by component.pref.collectAsState()

    Preference(
        modifier = modifier,
        eventModifier = {
            Modifier.toggleable(
                value = prefValue,
                enabled = LocalIsPrefEnabled.current,
                role = Role.Switch,
                onValueChange = {
                    scope.launch {
                        component.pref.set(it)
                    }
                },
            )
        },
        icon = component.icon.invoke(),
        title = component.title.invoke(),
        summary = component.summary.invoke(),
        trailing = {
            Switch(
                modifier = Modifier.size(LocalViewConfiguration.current.minimumTouchTargetSize),
                checked = prefValue,
                onCheckedChange = null,
                enabled = LocalIsPrefEnabled.current,
            )
        },
        enabledIf = component.enabledIf,
        visibleIf = component.visibleIf,
    )
}
