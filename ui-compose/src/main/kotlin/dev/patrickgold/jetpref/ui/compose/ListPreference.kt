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
    val label: String,
    val labelComposer: @Composable (String) -> Unit,
    val description: String,
    val descriptionComposer: @Composable (String) -> Unit,
    val showDescriptionOnlyIfSelected: Boolean,
)

fun <V : Any> entry(
    key: V,
    label: String,
): ListPreferenceEntry<V> {
    return ListPreferenceEntry(key, label, { Text(it) }, "", { }, false)
}

fun <V : Any> entry(
    key: V,
    label: String,
    description: String,
    showDescriptionOnlyIfSelected: Boolean = false,
): ListPreferenceEntry<V> {
    return ListPreferenceEntry(key, label, { Text(it) }, description, { Text(it) }, showDescriptionOnlyIfSelected)
}

fun <V : Any> entry(
    key: V,
    label: String,
    description: String,
    descriptionComposer: @Composable (String) -> Unit,
    showDescriptionOnlyIfSelected: Boolean = false,
): ListPreferenceEntry<V> {
    return ListPreferenceEntry(key, label, { Text(it) }, description, descriptionComposer, showDescriptionOnlyIfSelected)
}

fun <V : Any> entry(
    key: V,
    label: String,
    labelComposer: @Composable (String) -> Unit,
    description: String,
    descriptionComposer: @Composable (String) -> Unit,
    showDescriptionOnlyIfSelected: Boolean = false,
): ListPreferenceEntry<V> {
    return ListPreferenceEntry(key, label, labelComposer, description, descriptionComposer, showDescriptionOnlyIfSelected)
}

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
        JetPrefListItem(
            icon = maybeJetIcon(iconId, iconSpaceReserved),
            text = title,
            secondaryText = entries.find {
                it.key == pref.value
            }?.label ?: "!! invalid !!",
            modifier = Modifier
                .clickable(
                    enabled = isEnabled,
                    role = Role.Button,
                    onClick = {
                        setOptionValue(pref.value)
                        isDialogOpen.value = true
                    }
                )
                .alpha(if (isEnabled) 1.0f else ContentAlpha.disabled)
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
                                    horizontal = 16.dp,
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
                                entry.labelComposer(entry.label)
                                if (entry.showDescriptionOnlyIfSelected) {
                                    if (entry.key == optionValue) {
                                        entry.descriptionComposer(entry.description)
                                    }
                                } else {
                                    entry.descriptionComposer(entry.description)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
