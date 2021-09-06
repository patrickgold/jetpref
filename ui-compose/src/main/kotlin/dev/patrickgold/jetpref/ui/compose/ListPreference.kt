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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.ContentAlpha
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ListItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.RadioButton
import androidx.compose.material.RadioButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import dev.patrickgold.jetpref.datastore.model.PreferenceData
import dev.patrickgold.jetpref.datastore.model.PreferenceDataEvaluator
import dev.patrickgold.jetpref.datastore.model.PreferenceDataEvaluatorScope
import dev.patrickgold.jetpref.datastore.model.PreferenceModel
import dev.patrickgold.jetpref.datastore.model.observeAsState

data class ListPreferenceEntry<V : Any>(
    val key: V,
    val label: @Composable () -> Unit,
    val description: @Composable () -> Unit,
    val showDescriptionOnlyIfSelected: Boolean,
)

fun <V : Any> entry(
    key: V,
    label: String,
    showDescriptionOnlyIfSelected: Boolean = false,
): ListPreferenceEntry<V> {
    return ListPreferenceEntry(key, { Text(label) }, { }, showDescriptionOnlyIfSelected)
}

fun <V : Any> entry(
    key: V,
    label: String,
    description: String,
    showDescriptionOnlyIfSelected: Boolean = false,
): ListPreferenceEntry<V> {
    return ListPreferenceEntry(key, { Text(label) }, { Text(description) }, showDescriptionOnlyIfSelected)
}

fun <V : Any> entry(
    key: V,
    label: String,
    description: @Composable () -> Unit,
    showDescriptionOnlyIfSelected: Boolean = false,
): ListPreferenceEntry<V> {
    return ListPreferenceEntry(key, { Text(label) }, description, showDescriptionOnlyIfSelected)
}

fun <V : Any> entry(
    key: V,
    label: @Composable () -> Unit,
    description: @Composable () -> Unit,
    showDescriptionOnlyIfSelected: Boolean = false,
): ListPreferenceEntry<V> {
    return ListPreferenceEntry(key, label, description, showDescriptionOnlyIfSelected)
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun <T : PreferenceModel, V : Any> PreferenceUiScope<T>.ListPreference(
    ref: PreferenceData<V>,
    @DrawableRes iconId: Int? = null,
    iconSpaceReserved: Boolean = this.iconSpaceReserved,
    title: String,
    enabledIf: PreferenceDataEvaluator = this.enabledIf,
    visibleIf: PreferenceDataEvaluator = this.visibleIf,
    entries: List<ListPreferenceEntry<V>>,
) {
    val pref = ref.observeAsState()
    val (optionValue, setOptionValue) = remember { mutableStateOf(ref.get()) }
    val isDialogOpen = remember { mutableStateOf(false) }

    if (visibleIf(PreferenceDataEvaluatorScope.instance())) {
        val isEnabled = enabledIf(PreferenceDataEvaluatorScope.instance())
        ListItem(
            icon = maybeJetIcon(iconId, iconSpaceReserved),
            text = { Text(title) },
            secondaryText = entries.find {
                it.key == pref.value
            }?.label ?: { Text("!! invalid !!") },
            modifier = Modifier.clickable(
                enabled = isEnabled,
                role = Role.Button,
                onClick = {
                    setOptionValue(pref.value)
                    isDialogOpen.value = true
                }
            ).alpha(if (isEnabled) 1.0f else ContentAlpha.disabled)
        )
        if (isDialogOpen.value) {
            JetPrefAlertDialog(
                title = title,
                confirmLabel = stringResource(android.R.string.ok),
                onConfirm = {
                    ref.set(optionValue)
                    isDialogOpen.value = false
                },
                dismissLabel = stringResource(android.R.string.cancel),
                onDismiss = { isDialogOpen.value = false },
                neutralLabel = "Default",
                onNeutral = {
                    pref.value = ref.default
                    isDialogOpen.value = false
                }
            ) {
                Column {
                    for (entry in entries) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = entry.key == optionValue,
                                    onClick = {
                                        setOptionValue(entry.key)
                                    }
                                )
                                .padding(
                                    horizontal = 8.dp,
                                    vertical = 8.dp,
                                )
                        ) {
                            RadioButton(
                                selected = entry.key == optionValue,
                                onClick = null,
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = MaterialTheme.colors.primary,
                                ),
                                modifier = Modifier.padding(end = 12.dp),
                            )
                            Column(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                entry.label()
                                if (entry.showDescriptionOnlyIfSelected) {
                                    if (entry.key == optionValue) {
                                        entry.description()
                                    }
                                } else {
                                    entry.description()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
