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

import android.annotation.SuppressLint
import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.ContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.RadioButton
import androidx.compose.material.RadioButtonDefaults
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import dev.patrickgold.jetpref.datastore.model.PreferenceData
import dev.patrickgold.jetpref.datastore.model.PreferenceDataEvaluator
import dev.patrickgold.jetpref.datastore.model.PreferenceDataEvaluatorScope
import dev.patrickgold.jetpref.datastore.model.PreferenceModel
import dev.patrickgold.jetpref.datastore.model.observeAsState
import dev.patrickgold.jetpref.material.ui.JetPrefAlertDialog
import dev.patrickgold.jetpref.material.ui.JetPrefListItem

/**
 * Data class specifying a single list preference entry.
 *
 * @param V The type of the key, cannot be null.
 *
 * @property key The unique key of this entry, which is set as the value for the datastore model's
 *  corresponding [PreferenceData].
 * @property label The text which is used as the entry's list label.
 * @property labelComposer The composer for the [label]. Should return a composable which uses
 *  the passed description.
 * @property description Optional string explaining an entry's purpose or what it does.
 *  Passing a blank string indicates that no description should be shown.
 * @property descriptionComposer The composer for the [description]. Should return a composable
 *  which uses the passed description.
 * @property showDescriptionOnlyIfSelected True if the description should be shown only if the
 *  list entry is selected, false if it should be shown for all entries. This flag does nothing
 *  if the description is a blank string and not showing for this entry.
 */
data class ListPreferenceEntry<V : Any>(
    val key: V,
    val label: String,
    val labelComposer: @Composable (String) -> Unit,
    val description: String,
    val descriptionComposer: @Composable (String) -> Unit,
    val showDescriptionOnlyIfSelected: Boolean,
)

/**
 * Builder scope for creating a list of list preference entries.
 *
 * @param V The type of the entry keys.
 */
interface ListPreferenceEntriesScope<V : Any> {
    /**
     * Creates an entry without a description.
     */
    fun entry(
        key: V,
        label: String,
    )

    /**
     * Creates an entry with a label + description.
     */
    fun entry(
        key: V,
        label: String,
        description: String,
        showDescriptionOnlyIfSelected: Boolean = false,
    )

    /**
     * Creates an entry with a label + description and custom description composer.
     */
    fun entry(
        key: V,
        label: String,
        description: String,
        descriptionComposer: @Composable (String) -> Unit,
        showDescriptionOnlyIfSelected: Boolean = false,
    )

    /**
     * Creates an entry with a label + description and custom composers for both.
     */
    fun entry(
        key: V,
        label: String,
        labelComposer: @Composable (String) -> Unit,
        description: String,
        descriptionComposer: @Composable (String) -> Unit,
        showDescriptionOnlyIfSelected: Boolean = false,
    )
}

private class ListPreferenceEntriesScopeImpl<V : Any> : ListPreferenceEntriesScope<V> {
    private val entries = mutableListOf<ListPreferenceEntry<V>>()

    override fun entry(
        key: V,
        label: String,
    ) {
        entries.add(ListPreferenceEntry(key, label, { Text(it) }, "", { }, false))
    }

    override fun entry(
        key: V,
        label: String,
        description: String,
        showDescriptionOnlyIfSelected: Boolean,
    ) {
        entries.add(ListPreferenceEntry(key, label, { Text(it) }, description, { Text(it, style = MaterialTheme.typography.body2) }, showDescriptionOnlyIfSelected))
    }

    override fun entry(
        key: V,
        label: String,
        description: String,
        descriptionComposer: @Composable (String) -> Unit,
        showDescriptionOnlyIfSelected: Boolean,
    ) {
        entries.add(ListPreferenceEntry(key, label, { Text(it) }, description, descriptionComposer, showDescriptionOnlyIfSelected))
    }

    override fun entry(
        key: V,
        label: String,
        labelComposer: @Composable (String) -> Unit,
        description: String,
        descriptionComposer: @Composable (String) -> Unit,
        showDescriptionOnlyIfSelected: Boolean,
    ) {
        entries.add(ListPreferenceEntry(key, label, labelComposer, description, descriptionComposer, showDescriptionOnlyIfSelected))
    }

    fun build(): List<ListPreferenceEntry<V>> {
        return entries.toList()
    }
}

/**
 * List entries builder DSL for use with [ListPreference].
 *
 * @param V The type of the entry keys.
 * @param scope THe builder scope for the entries list.
 *
 * @return A list of [ListPreferenceEntry] items.
 */
@Composable
fun <V : Any> listPrefEntries(
    scope: @Composable ListPreferenceEntriesScope<V>.() -> Unit,
): List<ListPreferenceEntry<V>> {
    val builder = ListPreferenceEntriesScopeImpl<V>()
    scope(builder)
    return builder.build()
}

@SuppressLint("ModifierParameter")
@Composable
fun <T : PreferenceModel, V : Any> PreferenceUiScope<T>.ListPreference(
    listPref: PreferenceData<V>,
    switchPref: PreferenceData<Boolean>? = null,
    modifier: Modifier = Modifier,
    @DrawableRes iconId: Int? = null,
    iconSpaceReserved: Boolean = this.iconSpaceReserved,
    title: String,
    summarySwitchDisabled: String? = null,
    dialogStrings: DialogPrefStrings = LocalDefaultDialogPrefStrings.current,
    enabledIf: PreferenceDataEvaluator = { true },
    visibleIf: PreferenceDataEvaluator = { true },
    entries: List<ListPreferenceEntry<V>>,
) {
    val listPrefValue by listPref.observeAsState()
    val switchPrefValue = switchPref?.observeAsState() // can't use delegate because nullable
    val (tmpListPrefValue, setTmpListPrefValue) = remember { mutableStateOf(listPref.get()) }
    val (tmpSwitchPrefValue, setTmpSwitchPrefValue) = remember { mutableStateOf(false) }
    val isDialogOpen = remember { mutableStateOf(false) }

    val evalScope = PreferenceDataEvaluatorScope.instance()
    if (this.visibleIf(evalScope) && visibleIf(evalScope)) {
        val isEnabled = this.enabledIf(evalScope) && enabledIf(evalScope)
        JetPrefListItem(
            modifier = modifier
                .clickable(
                    enabled = isEnabled,
                    role = Role.Button,
                    onClick = {
                        setTmpListPrefValue(listPrefValue)
                        if (switchPrefValue != null) {
                            setTmpSwitchPrefValue(switchPrefValue.value)
                        }
                        isDialogOpen.value = true
                    }
                ),
            icon = maybeJetIcon(iconId, iconSpaceReserved),
            text = title,
            secondaryText = if (switchPrefValue?.value == true || switchPrefValue == null) {
                entries.find {
                    it.key == listPrefValue
                }?.label ?: "!! invalid !!"
            } else { summarySwitchDisabled },
            trailing = {
                if (switchPrefValue != null) {
                    val dividerColor = MaterialTheme.colors.onSurface.copy(alpha = 0.12f)
                    Box(
                        modifier = Modifier
                            .size(LocalViewConfiguration.current.minimumTouchTargetSize + DpSize(8.dp, 0.dp))
                            .toggleable(
                                value = switchPrefValue.value,
                                enabled = isEnabled,
                                role = Role.Switch,
                                onValueChange = { switchPref.set(it) },
                            )
                            .drawBehind {
                                drawLine(
                                    color = dividerColor,
                                    start = Offset(0f, size.height * 0.1f),
                                    end = Offset(0f, size.height * 0.9f),
                                )
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Switch(
                            modifier = Modifier.padding(start = 8.dp),
                            checked = switchPrefValue.value,
                            onCheckedChange = null,
                            enabled = isEnabled,
                        )
                    }
                }
            },
            enabled = isEnabled,
        )
        if (isDialogOpen.value) {
            JetPrefAlertDialog(
                title = title,
                confirmLabel = dialogStrings.confirmLabel,
                onConfirm = {
                    listPref.set(tmpListPrefValue)
                    switchPref?.set(tmpSwitchPrefValue)
                    isDialogOpen.value = false
                },
                dismissLabel = dialogStrings.dismissLabel,
                onDismiss = { isDialogOpen.value = false },
                neutralLabel = dialogStrings.neutralLabel,
                onNeutral = {
                    listPref.reset()
                    switchPref?.reset()
                    isDialogOpen.value = false
                },
                trailingIconTitle = {
                    if (switchPrefValue != null) {
                        Switch(
                            modifier = Modifier.padding(start = 16.dp),
                            checked = tmpSwitchPrefValue,
                            onCheckedChange = { setTmpSwitchPrefValue(it) },
                            enabled = true,
                        )
                    }
                },
                contentPadding = PaddingValues(horizontal = 8.dp),
            ) {
                Column {
                    val alpha = when {
                        switchPrefValue == null -> ContentAlpha.high
                        tmpSwitchPrefValue -> ContentAlpha.high
                        else -> ContentAlpha.disabled
                    }
                    for (entry in entries) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = entry.key == tmpListPrefValue,
                                    enabled = switchPrefValue == null || tmpSwitchPrefValue,
                                    onClick = {
                                        setTmpListPrefValue(entry.key)
                                    }
                                )
                                .padding(
                                    horizontal = 16.dp,
                                    vertical = 8.dp,
                                )
                                .alpha(alpha)
                        ) {
                            RadioButton(
                                selected = entry.key == tmpListPrefValue,
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
                                    if (entry.key == tmpListPrefValue) {
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
