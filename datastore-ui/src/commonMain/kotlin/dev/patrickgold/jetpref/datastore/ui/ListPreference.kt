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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import dev.patrickgold.jetpref.datastore.component.PreferenceComponent
import dev.patrickgold.jetpref.datastore.model.PreferenceData
import dev.patrickgold.jetpref.datastore.model.PreferenceDataEvaluator
import dev.patrickgold.jetpref.datastore.model.collectAsState
import dev.patrickgold.jetpref.material.ui.JetPrefAlertDialog
import dev.patrickgold.jetpref.material.ui.JetPrefAlertDialogDefaults
import dev.patrickgold.jetpref.material.ui.copy
import kotlinx.coroutines.launch

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
 *
 * @since 0.1.0
 */
data class ListPreferenceEntry<V : Any>(
    val key: V,
    val label: @Composable () -> String,
    val labelComposer: @Composable (String) -> Unit,
    val description: @Composable () -> String,
    val descriptionComposer: @Composable (String) -> Unit,
    val showDescriptionOnlyIfSelected: Boolean,
)

/**
 * Builder scope for creating a list of list preference entries.
 *
 * @param V The type of the entry keys.
 *
 * @since 0.1.0
 */
interface ListPreferenceEntriesScope<V : Any> {
    /**
     * Creates an entry without a description.
     *
     * @since 0.4.0
     */
    fun entry(
        key: V,
        label: @Composable () -> String,
    )

    /**
     * Creates an entry with a label + description.
     *
     * @since 0.4.0
     */
    fun entry(
        key: V,
        label: @Composable () -> String,
        description: @Composable () -> String,
        showDescriptionOnlyIfSelected: Boolean = false,
    )

    /**
     * Creates an entry with a label + description and custom description composer.
     *
     * @since 0.4.0
     */
    fun entry(
        key: V,
        label: @Composable () -> String,
        description: @Composable () -> String,
        descriptionComposer: @Composable (String) -> Unit,
        showDescriptionOnlyIfSelected: Boolean = false,
    )

    /**
     * Creates an entry with a label + description and custom composers for both.
     *
     * @since 0.4.0
     */
    fun entry(
        key: V,
        label: @Composable () -> String,
        labelComposer: @Composable (String) -> Unit,
        description: @Composable () -> String,
        descriptionComposer: @Composable (String) -> Unit,
        showDescriptionOnlyIfSelected: Boolean = false,
    )
}

private class ListPreferenceEntriesScopeImpl<V : Any> : ListPreferenceEntriesScope<V> {
    private val entries = mutableListOf<ListPreferenceEntry<V>>()

    override fun entry(
        key: V,
        label: @Composable () -> String,
    ) {
        entries.add(ListPreferenceEntry(key, label, { Text(it) }, { "" }, { }, false))
    }

    override fun entry(
        key: V,
        label: @Composable () -> String,
        description: @Composable () -> String,
        showDescriptionOnlyIfSelected: Boolean,
    ) {
        entries.add(ListPreferenceEntry(key, label, { Text(it) }, description, { Text(it, style = MaterialTheme.typography.bodyMedium) }, showDescriptionOnlyIfSelected))
    }

    override fun entry(
        key: V,
        label: @Composable () -> String,
        description: @Composable () -> String,
        descriptionComposer: @Composable (String) -> Unit,
        showDescriptionOnlyIfSelected: Boolean,
    ) {
        entries.add(ListPreferenceEntry(key, label, { Text(it) }, description, descriptionComposer, showDescriptionOnlyIfSelected))
    }

    override fun entry(
        key: V,
        label: @Composable () -> String,
        labelComposer: @Composable (String) -> Unit,
        description: @Composable () -> String,
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
 *
 * @since 0.4.0
 */
fun <V : Any> listPrefEntries(
    scope: ListPreferenceEntriesScope<V>.() -> Unit,
): List<ListPreferenceEntry<V>> {
    val builder = ListPreferenceEntriesScopeImpl<V>()
    scope(builder)
    return builder.build()
}

/**
 * Material list preference which allows the user to select a single entry from a list of entries. Optionally, a switch
 * preference can be provided to enable or disable the list preference's entries.
 *
 * @param component Component describing what to display.
 * @param modifier Modifier to be applied to the underlying preference.
 *
 * @since 0.4.0
 *
 * @see listPrefEntries
 */
@Composable
fun <V : Any> ListPreference(
    component: PreferenceComponent.ListPicker<V>,
    modifier: Modifier = Modifier
) {
    ListPreferenceImpl(
        listPref = component.listPref,
        switchPref = null,
        modifier = modifier,
        icon = component.icon.invoke(),
        title = component.title.invoke(),
        summary = component.summary.invoke(),
        enabledIf = component.enabledIf,
        visibleIf = component.visibleIf,
        entries = component.entries,
    )
}

/**
 * Material list preference which allows the user to select a single entry from a list of entries. Optionally, a switch
 * preference can be provided to enable or disable the list preference's entries.
 *
 * @param component Component describing what to display.
 * @param modifier Modifier to be applied to the underlying preference.
 *
 * @since 0.4.0
 *
 * @see listPrefEntries
 */
@Composable
fun <V : Any> ListPreference(
    component: PreferenceComponent.ListPickerWithSwitch<V>,
    modifier: Modifier = Modifier,
) {
    ListPreferenceImpl(
        listPref = component.listPref,
        switchPref = component.switchPref,
        modifier = modifier,
        icon = component.icon.invoke(),
        title = component.title.invoke(),
        summary = component.summary.invoke(),
        enabledIf = component.enabledIf,
        visibleIf = component.visibleIf,
        entries = component.entries,
    )
}

@Composable
private fun <V : Any> ListPreferenceImpl(
    listPref: PreferenceData<V>,
    switchPref: PreferenceData<Boolean>?,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    title: String,
    summary: String?,
    enabledIf: PreferenceDataEvaluator = { true },
    visibleIf: PreferenceDataEvaluator = { true },
    entries: List<ListPreferenceEntry<V>>,
) {
    val dialogStrings = LocalDialogPrefStrings.current
    val scope = rememberCoroutineScope()
    val listPrefValue by listPref.collectAsState()
    val switchPrefValue = switchPref?.collectAsState() // can't use delegate because nullable
    val (tmpListPrefValue, setTmpListPrefValue) = remember { mutableStateOf(listPref.get()) }
    val (tmpSwitchPrefValue, setTmpSwitchPrefValue) = remember { mutableStateOf(false) }
    var isDialogOpen by remember { mutableStateOf(false) }

    Preference(
        modifier = modifier,
        icon = icon,
        title = title,
        summary = summary,
        trailing = {
            if (switchPrefValue != null) {
                val dividerColor = MaterialTheme.colorScheme.outlineVariant
                Box(
                    modifier = Modifier
                        .size(LocalViewConfiguration.current.minimumTouchTargetSize + DpSize(8.dp, 0.dp))
                        .toggleable(
                            value = switchPrefValue.value,
                            enabled = LocalIsPrefEnabled.current,
                            role = Role.Switch,
                            onValueChange = {
                                scope.launch {
                                    switchPref.set(it)
                                }
                            },
                        )
                        .drawBehind {
                            drawLine(
                                color = dividerColor,
                                start = Offset(0f, size.height * 0.1f),
                                end = Offset(0f, size.height * 0.9f),
                                strokeWidth = 2f
                            )
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Switch(
                        modifier = Modifier.padding(start = 8.dp),
                        checked = switchPrefValue.value,
                        onCheckedChange = null,
                        enabled = LocalIsPrefEnabled.current,
                    )
                }
            }
        },
        enabledIf = enabledIf,
        visibleIf = visibleIf,
        onClick = {
            setTmpListPrefValue(listPrefValue)
            if (switchPrefValue != null) {
                setTmpSwitchPrefValue(switchPrefValue.value)
            }
            isDialogOpen = true
        },
    )

    if (isDialogOpen) {
        JetPrefAlertDialog(
            title = title,
            confirmLabel = dialogStrings.confirmLabel,
            onConfirm = {
                scope.launch {
                    listPref.set(tmpListPrefValue)
                    switchPref?.set(tmpSwitchPrefValue)
                }
                isDialogOpen = false
            },
            dismissLabel = dialogStrings.dismissLabel,
            onDismiss = {
                isDialogOpen = false
            },
            neutralLabel = dialogStrings.neutralLabel,
            onNeutral = {
                scope.launch {
                    listPref.reset()
                    switchPref?.reset()
                }
                isDialogOpen = false
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
            titlePadding = remember(switchPrefValue) {
                if (switchPrefValue != null) {
                    JetPrefAlertDialogDefaults.TitlePadding.copy(top = 16.dp)
                } else {
                    JetPrefAlertDialogDefaults.TitlePadding
                }
            },
            contentPadding = PaddingValues(horizontal = 8.dp),
        ) {
            Column {
                val alpha = when {
                    switchPrefValue == null -> 1f
                    tmpSwitchPrefValue -> 1f
                    else -> 0.38f
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
                                selectedColor = MaterialTheme.colorScheme.primary,
                            ),
                            modifier = Modifier.padding(end = 12.dp),
                        )
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.CenterVertically),
                        ) {
                            entry.labelComposer(entry.label.invoke())
                            if (entry.showDescriptionOnlyIfSelected) {
                                if (entry.key == tmpListPrefValue) {
                                    entry.descriptionComposer(entry.description.invoke())
                                }
                            } else {
                                entry.descriptionComposer(entry.description.invoke())
                            }
                        }
                    }
                }
            }
        }
    }
}
