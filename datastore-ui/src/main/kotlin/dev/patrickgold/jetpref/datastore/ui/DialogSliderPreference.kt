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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import dev.patrickgold.jetpref.datastore.model.PreferenceData
import dev.patrickgold.jetpref.datastore.model.PreferenceDataEvaluator
import dev.patrickgold.jetpref.datastore.model.PreferenceDataEvaluatorScope
import dev.patrickgold.jetpref.datastore.model.PreferenceModel
import dev.patrickgold.jetpref.datastore.model.observeAsState
import dev.patrickgold.jetpref.material.ui.JetPrefAlertDialog
import dev.patrickgold.jetpref.material.ui.JetPrefListItem
import kotlin.math.roundToInt
import kotlin.math.roundToLong

@ExperimentalJetPrefDatastoreUi
@Composable
internal fun <T : PreferenceModel, V> PreferenceUiScope<T>.DialogSliderPreference(
    pref: PreferenceData<V>,
    modifier: Modifier,
    icon: ImageVector? = null,
    iconSpaceReserved: Boolean,
    title: String,
    valueLabel: @Composable (V) -> String,
    summary: @Composable (V) -> String,
    min: V,
    max: V,
    stepIncrement: V,
    onPreviewSelectedValue: (V) -> Unit,
    dialogStrings: DialogPrefStrings,
    enabledIf: PreferenceDataEvaluator,
    visibleIf: PreferenceDataEvaluator,
    convertToV: (Float) -> V,
) where V : Number, V : Comparable<V> {
    require(stepIncrement > convertToV(0f)) { "Step increment must be greater than 0!" }
    require(max > min) { "Maximum value ($max) must be greater than minimum value ($min)!" }

    val prefValue by pref.observeAsState()
    var sliderValue by remember { mutableFloatStateOf(0.0f) }
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
                        sliderValue = prefValue.toFloat()
                        isDialogOpen.value = true
                    }
                ),
            icon = maybeJetIcon(imageVector = icon, iconSpaceReserved = iconSpaceReserved),
            text = title,
            secondaryText = summary(prefValue),
            enabled = isEnabled,
        )
        if (isDialogOpen.value) {
            JetPrefAlertDialog(
                title = title,
                confirmLabel = dialogStrings.confirmLabel,
                onConfirm = {
                    pref.set(convertToV(sliderValue))
                    isDialogOpen.value = false
                },
                dismissLabel = dialogStrings.dismissLabel,
                onDismiss = { isDialogOpen.value = false },
                neutralLabel = dialogStrings.neutralLabel,
                onNeutral = {
                    pref.reset()
                    isDialogOpen.value = false
                }
            ) {
                Column {
                    Text(
                        text = valueLabel(convertToV(sliderValue)),
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                    )
                    Slider(
                        value = sliderValue,
                        valueRange = min.toFloat()..max.toFloat(),
                        steps = ((max.toFloat() - min.toFloat()) / stepIncrement.toFloat()).roundToInt() - 1,
                        onValueChange = { sliderValue = it },
                        onValueChangeFinished = { onPreviewSelectedValue(convertToV(sliderValue)) },
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary,
                            activeTickColor = Color.Transparent,
                            inactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(
                                alpha = SliderDefaults.colors().inactiveTrackColor.alpha,
                            ),
                            inactiveTickColor = Color.Transparent,
                        ),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

@ExperimentalJetPrefDatastoreUi
@Composable
internal fun <T : PreferenceModel, V> PreferenceUiScope<T>.DialogSliderPreference(
    primaryPref: PreferenceData<V>,
    secondaryPref: PreferenceData<V>,
    modifier: Modifier,
    icon: ImageVector? = null,
    iconSpaceReserved: Boolean,
    title: String,
    primaryLabel: String,
    secondaryLabel: String,
    valueLabel: @Composable (V) -> String,
    summary: @Composable (V, V) -> String,
    min: V,
    max: V,
    stepIncrement: V,
    onPreviewSelectedPrimaryValue: (V) -> Unit,
    onPreviewSelectedSecondaryValue: (V) -> Unit,
    dialogStrings: DialogPrefStrings,
    enabledIf: PreferenceDataEvaluator,
    visibleIf: PreferenceDataEvaluator,
    convertToV: (Float) -> V,
) where V : Number, V : Comparable<V> {
    require(stepIncrement > convertToV(0f)) { "Step increment must be greater than 0!" }
    require(max > min) { "Maximum value ($max) must be greater than minimum value ($min)!" }

    val primaryPrefValue by primaryPref.observeAsState()
    val secondaryPrefValue by secondaryPref.observeAsState()
    var primarySliderValue by remember { mutableStateOf(convertToV(0.0f)) }
    var secondarySliderValue by remember { mutableStateOf(convertToV(0.0f)) }
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
                        primarySliderValue = primaryPrefValue
                        secondarySliderValue = secondaryPrefValue
                        isDialogOpen.value = true
                    }
                ),
            icon = maybeJetIcon(imageVector = icon, iconSpaceReserved = iconSpaceReserved),
            text = title,
            secondaryText = summary(primaryPrefValue, secondaryPrefValue),
            enabled = isEnabled,
        )
        if (isDialogOpen.value) {
            JetPrefAlertDialog(
                title = title,
                confirmLabel = dialogStrings.confirmLabel,
                onConfirm = {
                    primaryPref.set(primarySliderValue)
                    secondaryPref.set(secondarySliderValue)
                    isDialogOpen.value = false
                },
                dismissLabel = dialogStrings.dismissLabel,
                onDismiss = { isDialogOpen.value = false },
                neutralLabel = dialogStrings.neutralLabel,
                onNeutral = {
                    primaryPref.reset()
                    secondaryPref.reset()
                    isDialogOpen.value = false
                }
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(primaryLabel)
                        Text(valueLabel(primarySliderValue))
                    }
                    Slider(
                        value = primarySliderValue.toFloat(),
                        valueRange = min.toFloat()..max.toFloat(),
                        steps = ((max.toFloat() - min.toFloat()) / stepIncrement.toFloat()).toInt() - 1,
                        onValueChange = { primarySliderValue = convertToV(it) },
                        onValueChangeFinished = { onPreviewSelectedPrimaryValue(primarySliderValue) },
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary,
                            activeTickColor = Color.Transparent,
                            inactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(
                                alpha = SliderDefaults.colors().inactiveTrackColor.alpha,
                            ),
                            inactiveTickColor = Color.Transparent,
                        ),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(secondaryLabel)
                        Text(valueLabel(secondarySliderValue))
                    }
                    Slider(
                        value = secondarySliderValue.toFloat(),
                        valueRange = min.toFloat()..max.toFloat(),
                        steps = ((max.toFloat() - min.toFloat()) / stepIncrement.toFloat()).toInt() - 1,
                        onValueChange = { secondarySliderValue = convertToV(it) },
                        onValueChangeFinished = { onPreviewSelectedSecondaryValue(secondarySliderValue) },
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary,
                            activeTickColor = Color.Transparent,
                            inactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(
                                alpha = SliderDefaults.colors().inactiveTrackColor.alpha,
                            ),
                            inactiveTickColor = Color.Transparent,
                        ),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

/**
 * Material preference which provides a dialog with a slider for choosing a value.
 *
 * @param pref The integer preference data entry from the datastore.
 * @param modifier Modifier to be applied to the underlying list item.
 * @param icon The [ImageVector] of the list entry.
 * @param iconSpaceReserved If the space at the start of the list item should be reserved (blank
 *  space) if no icon ID is provided.
 * @param title The title of this preference, shown as the list item primary text (max 1 line).
 * @param valueLabel The label of the value, used to add a unit to a value or to display a different text for special
 *  value (e.g. -1 -> System default).
 * @param summary The summary of this preference, shown as the list item secondary text (max 2 lines). Defaults to
 *  [valueLabel].
 * @param min The minimum value allowed on the slider. Must be smaller than [max].
 * @param max The maximum value allowed on the slider. Must be greater than [min].
 * @param stepIncrement The step increment for the slider. Must be greater than 0.
 * @param onPreviewSelectedValue Optional callback which gets invoked when the slider drag movement is finished. This
 *  allows to preview the effect of the selected value. This value should not be stored, the actual selected new value
 *  will be written to the preference once the user confirms it.
 * @param dialogStrings The dialog strings to use for this dialog. Defaults to the current dialog prefs set.
 * @param enabledIf Evaluator scope which allows to dynamically decide if this preference should be
 *  enabled (true) or disabled (false).
 * @param visibleIf Evaluator scope which allows to dynamically decide if this preference should be
 *  visible (true) or hidden (false).
 *
 * @since 0.1.0
 */
@ExperimentalJetPrefDatastoreUi
@Composable
fun <T : PreferenceModel> PreferenceUiScope<T>.DialogSliderPreference(
    pref: PreferenceData<Int>,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    iconSpaceReserved: Boolean = this.iconSpaceReserved,
    title: String,
    valueLabel: @Composable (Int) -> String = { it.toString() },
    summary: @Composable (Int) -> String = valueLabel,
    min: Int,
    max: Int,
    stepIncrement: Int,
    onPreviewSelectedValue: (Int) -> Unit = { },
    dialogStrings: DialogPrefStrings = LocalDefaultDialogPrefStrings.current,
    enabledIf: PreferenceDataEvaluator = { true },
    visibleIf: PreferenceDataEvaluator = { true },
) {
    DialogSliderPreference(
        pref, modifier, icon, iconSpaceReserved, title, valueLabel, summary, min, max,
        stepIncrement, onPreviewSelectedValue, dialogStrings, enabledIf, visibleIf,
    ) {
        try {
            it.roundToInt()
        } catch (e: IllegalArgumentException) {
            it.toInt()
        }
    }
}

/**
 * Material preference which provides a dialog with two sliders for choosing two values at once.
 *
 * @param primaryPref The primary integer preference data entry from the datastore.
 * @param secondaryPref The secondary integer preference data entry from the datastore.
 * @param modifier Modifier to be applied to the underlying list item.
 * @param icon The [ImageVector] of the list entry.
 * @param iconSpaceReserved If the space at the start of the list item should be reserved (blank
 *  space) if no icon ID is provided.
 * @param title The title of this preference, shown as the list item primary text (max 1 line).
 * @param primaryLabel The label to display above the primary slider in the dialog.
 * @param secondaryLabel The label to display above the secondary slider in the dialog.
 * @param valueLabel The label of the value, used to add a unit to a value or to display a different text for special
 *  value (e.g. -1 -> System default).
 * @param summary The summary of this preference, shown as the list item secondary text (max 2 lines). Defaults to
 *  [valueLabel] / [valueLabel].
 * @param min The minimum value allowed on the slider. Must be smaller than [max].
 * @param max The maximum value allowed on the slider. Must be greater than [min].
 * @param stepIncrement The step increment for the slider. Must be greater than 0.
 * @param onPreviewSelectedPrimaryValue Optional callback which gets invoked when the primary slider drag movement is
 *  finished. This allows to preview the effect of the selected primary value. This value should not be stored, the
 *  actual selected new primary value will be written to the preference once the user confirms it.
 * @param onPreviewSelectedSecondaryValue Optional callback which gets invoked when the secondary slider drag movement
 *  is finished. This allows to preview the effect of the selected secondary value. This value should not be stored, the
 *  actual selected new secondary value will be written to the preference once the user confirms it.
 * @param dialogStrings The dialog strings to use for this dialog. Defaults to the current dialog prefs set.
 * @param enabledIf Evaluator scope which allows to dynamically decide if this preference should be
 *  enabled (true) or disabled (false).
 * @param visibleIf Evaluator scope which allows to dynamically decide if this preference should be
 *  visible (true) or hidden (false).
 *
 * @since 0.1.0
 */
@ExperimentalJetPrefDatastoreUi
@Composable
fun <T : PreferenceModel> PreferenceUiScope<T>.DialogSliderPreference(
    primaryPref: PreferenceData<Int>,
    secondaryPref: PreferenceData<Int>,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    iconSpaceReserved: Boolean = this.iconSpaceReserved,
    title: String,
    primaryLabel: String,
    secondaryLabel: String,
    valueLabel: @Composable (Int) -> String = { it.toString() },
    summary: @Composable (Int, Int) -> String = { p, s -> "${valueLabel(p)} / ${valueLabel(s)}" },
    min: Int,
    max: Int,
    stepIncrement: Int,
    onPreviewSelectedPrimaryValue: (Int) -> Unit = { },
    onPreviewSelectedSecondaryValue: (Int) -> Unit = { },
    dialogStrings: DialogPrefStrings = LocalDefaultDialogPrefStrings.current,
    enabledIf: PreferenceDataEvaluator = { true },
    visibleIf: PreferenceDataEvaluator = { true },
) {
    DialogSliderPreference(
        primaryPref, secondaryPref, modifier, icon, iconSpaceReserved, title, primaryLabel,
        secondaryLabel, valueLabel, summary, min, max, stepIncrement, onPreviewSelectedPrimaryValue,
        onPreviewSelectedSecondaryValue, dialogStrings, enabledIf, visibleIf,
    ) {
        try {
            it.roundToInt()
        } catch (e: IllegalArgumentException) {
            it.toInt()
        }
    }
}

/**
 * Material preference which provides a dialog with a slider for choosing a value.
 *
 * @param pref The long preference data entry from the datastore.
 * @param modifier Modifier to be applied to the underlying list item.
 * @param icon The [ImageVector] of the list entry.
 * @param iconSpaceReserved If the space at the start of the list item should be reserved (blank
 *  space) if no icon ID is provided.
 * @param title The title of this preference, shown as the list item primary text (max 1 line).
 * @param valueLabel The label of the value, used to add a unit to a value or to display a different text for special
 *  value (e.g. -1 -> System default).
 * @param summary The summary of this preference, shown as the list item secondary text (max 2 lines). Defaults to
 *  [valueLabel].
 * @param min The minimum value allowed on the slider. Must be smaller than [max].
 * @param max The maximum value allowed on the slider. Must be greater than [min].
 * @param stepIncrement The step increment for the slider. Must be greater than 0.
 * @param onPreviewSelectedValue Optional callback which gets invoked when the slider drag movement is finished. This
 *  allows to preview the effect of the selected value. This value should not be stored, the actual selected new value
 *  will be written to the preference once the user confirms it.
 * @param dialogStrings The dialog strings to use for this dialog. Defaults to the current dialog prefs set.
 * @param enabledIf Evaluator scope which allows to dynamically decide if this preference should be
 *  enabled (true) or disabled (false).
 * @param visibleIf Evaluator scope which allows to dynamically decide if this preference should be
 *  visible (true) or hidden (false).
 *
 * @since 0.1.0
 */
@ExperimentalJetPrefDatastoreUi
@Composable
fun <T : PreferenceModel> PreferenceUiScope<T>.DialogSliderPreference(
    pref: PreferenceData<Long>,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    iconSpaceReserved: Boolean = this.iconSpaceReserved,
    title: String,
    valueLabel: @Composable (Long) -> String = { it.toString() },
    summary: @Composable (Long) -> String = valueLabel,
    min: Long,
    max: Long,
    stepIncrement: Long,
    onPreviewSelectedValue: (Long) -> Unit = { },
    dialogStrings: DialogPrefStrings = LocalDefaultDialogPrefStrings.current,
    enabledIf: PreferenceDataEvaluator = { true },
    visibleIf: PreferenceDataEvaluator = { true },
) {
    DialogSliderPreference(
        pref, modifier, icon, iconSpaceReserved, title, valueLabel, summary, min, max,
        stepIncrement, onPreviewSelectedValue, dialogStrings, enabledIf, visibleIf,
    ) {
        try {
            it.roundToLong()
        } catch (e: IllegalArgumentException) {
            it.toLong()
        }
    }
}

/**
 * Material preference which provides a dialog with two sliders for choosing two values at once.
 *
 * @param primaryPref The primary long preference data entry from the datastore.
 * @param secondaryPref The secondary long preference data entry from the datastore.
 * @param modifier Modifier to be applied to the underlying list item.
 * @param icon The [ImageVector] of the list entry.
 * @param iconSpaceReserved If the space at the start of the list item should be reserved (blank
 *  space) if no icon ID is provided.
 * @param title The title of this preference, shown as the list item primary text (max 1 line).
 * @param primaryLabel The label to display above the primary slider in the dialog.
 * @param secondaryLabel The label to display above the secondary slider in the dialog.
 * @param valueLabel The label of the value, used to add a unit to a value or to display a different text for special
 *  value (e.g. -1 -> System default).
 * @param summary The summary of this preference, shown as the list item secondary text (max 2 lines). Defaults to
 *  [valueLabel] / [valueLabel].
 * @param min The minimum value allowed on the slider. Must be smaller than [max].
 * @param max The maximum value allowed on the slider. Must be greater than [min].
 * @param stepIncrement The step increment for the slider. Must be greater than 0.
 * @param onPreviewSelectedPrimaryValue Optional callback which gets invoked when the primary slider drag movement is
 *  finished. This allows to preview the effect of the selected primary value. This value should not be stored, the
 *  actual selected new primary value will be written to the preference once the user confirms it.
 * @param onPreviewSelectedSecondaryValue Optional callback which gets invoked when the secondary slider drag movement
 *  is finished. This allows to preview the effect of the selected secondary value. This value should not be stored, the
 *  actual selected new secondary value will be written to the preference once the user confirms it.
 * @param dialogStrings The dialog strings to use for this dialog. Defaults to the current dialog prefs set.
 * @param enabledIf Evaluator scope which allows to dynamically decide if this preference should be
 *  enabled (true) or disabled (false).
 * @param visibleIf Evaluator scope which allows to dynamically decide if this preference should be
 *  visible (true) or hidden (false).
 *
 * @since 0.1.0
 */
@ExperimentalJetPrefDatastoreUi
@Composable
fun <T : PreferenceModel> PreferenceUiScope<T>.DialogSliderPreference(
    primaryPref: PreferenceData<Long>,
    secondaryPref: PreferenceData<Long>,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    iconSpaceReserved: Boolean = this.iconSpaceReserved,
    title: String,
    primaryLabel: String,
    secondaryLabel: String,
    valueLabel: @Composable (Long) -> String = { it.toString() },
    summary: @Composable (Long, Long) -> String = { p, s -> "${valueLabel(p)} / ${valueLabel(s)}" },
    min: Long,
    max: Long,
    stepIncrement: Long,
    onPreviewSelectedPrimaryValue: (Long) -> Unit = { },
    onPreviewSelectedSecondaryValue: (Long) -> Unit = { },
    dialogStrings: DialogPrefStrings = LocalDefaultDialogPrefStrings.current,
    enabledIf: PreferenceDataEvaluator = { true },
    visibleIf: PreferenceDataEvaluator = { true },
) {
    DialogSliderPreference(
        primaryPref, secondaryPref, modifier, icon, iconSpaceReserved, title, primaryLabel,
        secondaryLabel, valueLabel, summary, min, max, stepIncrement, onPreviewSelectedPrimaryValue,
        onPreviewSelectedSecondaryValue, dialogStrings, enabledIf, visibleIf,
    ) {
        try {
            it.roundToLong()
        } catch (e: IllegalArgumentException) {
            it.toLong()
        }
    }
}

/**
 * Material preference which provides a dialog with a slider for choosing a value.
 *
 * @param pref The double preference data entry from the datastore.
 * @param modifier Modifier to be applied to the underlying list item.
 * @param icon The [ImageVector] of the list entry.
 * @param iconSpaceReserved If the space at the start of the list item should be reserved (blank
 *  space) if no icon ID is provided.
 * @param title The title of this preference, shown as the list item primary text (max 1 line).
 * @param valueLabel The label of the value, used to add a unit to a value or to display a different text for special
 *  value (e.g. -1 -> System default).
 * @param summary The summary of this preference, shown as the list item secondary text (max 2 lines). Defaults to
 *  [valueLabel].
 * @param min The minimum value allowed on the slider. Must be smaller than [max].
 * @param max The maximum value allowed on the slider. Must be greater than [min].
 * @param stepIncrement The step increment for the slider. Must be greater than 0.
 * @param onPreviewSelectedValue Optional callback which gets invoked when the slider drag movement is finished. This
 *  allows to preview the effect of the selected value. This value should not be stored, the actual selected new value
 *  will be written to the preference once the user confirms it.
 * @param dialogStrings The dialog strings to use for this dialog. Defaults to the current dialog prefs set.
 * @param enabledIf Evaluator scope which allows to dynamically decide if this preference should be
 *  enabled (true) or disabled (false).
 * @param visibleIf Evaluator scope which allows to dynamically decide if this preference should be
 *  visible (true) or hidden (false).
 *
 * @since 0.1.0
 */
@ExperimentalJetPrefDatastoreUi
@Composable
fun <T : PreferenceModel> PreferenceUiScope<T>.DialogSliderPreference(
    pref: PreferenceData<Double>,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    iconSpaceReserved: Boolean = this.iconSpaceReserved,
    title: String,
    valueLabel: @Composable (Double) -> String = { it.toString() },
    summary: @Composable (Double) -> String = valueLabel,
    min: Double,
    max: Double,
    stepIncrement: Double,
    onPreviewSelectedValue: (Double) -> Unit = { },
    dialogStrings: DialogPrefStrings = LocalDefaultDialogPrefStrings.current,
    enabledIf: PreferenceDataEvaluator = { true },
    visibleIf: PreferenceDataEvaluator = { true },
) {
    DialogSliderPreference(
        pref, modifier, icon, iconSpaceReserved, title, valueLabel, summary, min, max,
        stepIncrement, onPreviewSelectedValue, dialogStrings, enabledIf, visibleIf,
    ) { it.toDouble() }
}

/**
 * Material preference which provides a dialog with two sliders for choosing two values at once.
 *
 * @param primaryPref The primary double preference data entry from the datastore.
 * @param secondaryPref The secondary double preference data entry from the datastore.
 * @param modifier Modifier to be applied to the underlying list item.
 * @param icon The [ImageVector] of the list entry.
 * @param iconSpaceReserved If the space at the start of the list item should be reserved (blank
 *  space) if no icon ID is provided.
 * @param title The title of this preference, shown as the list item primary text (max 1 line).
 * @param primaryLabel The label to display above the primary slider in the dialog.
 * @param secondaryLabel The label to display above the secondary slider in the dialog.
 * @param valueLabel The label of the value, used to add a unit to a value or to display a different text for special
 *  value (e.g. -1 -> System default).
 * @param summary The summary of this preference, shown as the list item secondary text (max 2 lines). Defaults to
 *  [valueLabel] / [valueLabel].
 * @param min The minimum value allowed on the slider. Must be smaller than [max].
 * @param max The maximum value allowed on the slider. Must be greater than [min].
 * @param stepIncrement The step increment for the slider. Must be greater than 0.
 * @param onPreviewSelectedPrimaryValue Optional callback which gets invoked when the primary slider drag movement is
 *  finished. This allows to preview the effect of the selected primary value. This value should not be stored, the
 *  actual selected new primary value will be written to the preference once the user confirms it.
 * @param onPreviewSelectedSecondaryValue Optional callback which gets invoked when the secondary slider drag movement
 *  is finished. This allows to preview the effect of the selected secondary value. This value should not be stored, the
 *  actual selected new secondary value will be written to the preference once the user confirms it.
 * @param dialogStrings The dialog strings to use for this dialog. Defaults to the current dialog prefs set.
 * @param enabledIf Evaluator scope which allows to dynamically decide if this preference should be
 *  enabled (true) or disabled (false).
 * @param visibleIf Evaluator scope which allows to dynamically decide if this preference should be
 *  visible (true) or hidden (false).
 *
 * @since 0.1.0
 */
@ExperimentalJetPrefDatastoreUi
@Composable
fun <T : PreferenceModel> PreferenceUiScope<T>.DialogSliderPreference(
    primaryPref: PreferenceData<Double>,
    secondaryPref: PreferenceData<Double>,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    iconSpaceReserved: Boolean = this.iconSpaceReserved,
    title: String,
    primaryLabel: String,
    secondaryLabel: String,
    valueLabel: @Composable (Double) -> String = { it.toString() },
    summary: @Composable (Double, Double) -> String = { p, s -> "${valueLabel(p)} / ${valueLabel(s)}" },
    min: Double,
    max: Double,
    stepIncrement: Double,
    onPreviewSelectedPrimaryValue: (Double) -> Unit = { },
    onPreviewSelectedSecondaryValue: (Double) -> Unit = { },
    dialogStrings: DialogPrefStrings = LocalDefaultDialogPrefStrings.current,
    enabledIf: PreferenceDataEvaluator = { true },
    visibleIf: PreferenceDataEvaluator = { true },
) {
    DialogSliderPreference(
        primaryPref, secondaryPref, modifier, icon, iconSpaceReserved, title, primaryLabel,
        secondaryLabel, valueLabel, summary, min, max, stepIncrement, onPreviewSelectedPrimaryValue,
        onPreviewSelectedSecondaryValue, dialogStrings, enabledIf, visibleIf,
    ) { it.toDouble() }
}

/**
 * Material preference which provides a dialog with a slider for choosing a value.
 *
 * @param pref The float preference data entry from the datastore.
 * @param modifier Modifier to be applied to the underlying list item.
 * @param icon The [ImageVector] of the list entry.
 * @param iconSpaceReserved If the space at the start of the list item should be reserved (blank
 *  space) if no icon ID is provided.
 * @param title The title of this preference, shown as the list item primary text (max 1 line).
 * @param valueLabel The label of the value, used to add a unit to a value or to display a different text for special
 *  value (e.g. -1 -> System default).
 * @param summary The summary of this preference, shown as the list item secondary text (max 2 lines). Defaults to
 *  [valueLabel].
 * @param min The minimum value allowed on the slider. Must be smaller than [max].
 * @param max The maximum value allowed on the slider. Must be greater than [min].
 * @param stepIncrement The step increment for the slider. Must be greater than 0.
 * @param onPreviewSelectedValue Optional callback which gets invoked when the slider drag movement is finished. This
 *  allows to preview the effect of the selected value. This value should not be stored, the actual selected new value
 *  will be written to the preference once the user confirms it.
 * @param dialogStrings The dialog strings to use for this dialog. Defaults to the current dialog prefs set.
 * @param enabledIf Evaluator scope which allows to dynamically decide if this preference should be
 *  enabled (true) or disabled (false).
 * @param visibleIf Evaluator scope which allows to dynamically decide if this preference should be
 *  visible (true) or hidden (false).
 *
 * @since 0.1.0
 */
@ExperimentalJetPrefDatastoreUi
@Composable
fun <T : PreferenceModel> PreferenceUiScope<T>.DialogSliderPreference(
    pref: PreferenceData<Float>,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    iconSpaceReserved: Boolean = this.iconSpaceReserved,
    title: String,
    valueLabel: @Composable (Float) -> String = { it.toString() },
    summary: @Composable (Float) -> String = valueLabel,
    min: Float,
    max: Float,
    stepIncrement: Float,
    onPreviewSelectedValue: (Float) -> Unit = { },
    dialogStrings: DialogPrefStrings = LocalDefaultDialogPrefStrings.current,
    enabledIf: PreferenceDataEvaluator = { true },
    visibleIf: PreferenceDataEvaluator = { true },
) {
    DialogSliderPreference(
        pref, modifier, icon, iconSpaceReserved, title, valueLabel, summary, min, max,
        stepIncrement, onPreviewSelectedValue, dialogStrings, enabledIf, visibleIf,
    ) { it }
}

/**
 * Material preference which provides a dialog with two sliders for choosing two values at once.
 *
 * @param primaryPref The primary float preference data entry from the datastore.
 * @param secondaryPref The secondary float preference data entry from the datastore.
 * @param modifier Modifier to be applied to the underlying list item.
 * @param icon The [ImageVector] of the list entry.
 * @param iconSpaceReserved If the space at the start of the list item should be reserved (blank
 *  space) if no icon ID is provided.
 * @param title The title of this preference, shown as the list item primary text (max 1 line).
 * @param primaryLabel The label to display above the primary slider in the dialog.
 * @param secondaryLabel The label to display above the secondary slider in the dialog.
 * @param valueLabel The label of the value, used to add a unit to a value or to display a different text for special
 *  value (e.g. -1 -> System default).
 * @param summary The summary of this preference, shown as the list item secondary text (max 2 lines). Defaults to
 *  [valueLabel] / [valueLabel].
 * @param min The minimum value allowed on the slider. Must be smaller than [max].
 * @param max The maximum value allowed on the slider. Must be greater than [min].
 * @param stepIncrement The step increment for the slider. Must be greater than 0.
 * @param onPreviewSelectedPrimaryValue Optional callback which gets invoked when the primary slider drag movement is
 *  finished. This allows to preview the effect of the selected primary value. This value should not be stored, the
 *  actual selected new primary value will be written to the preference once the user confirms it.
 * @param onPreviewSelectedSecondaryValue Optional callback which gets invoked when the secondary slider drag movement
 *  is finished. This allows to preview the effect of the selected secondary value. This value should not be stored, the
 *  actual selected new secondary value will be written to the preference once the user confirms it.
 * @param dialogStrings The dialog strings to use for this dialog. Defaults to the current dialog prefs set.
 * @param enabledIf Evaluator scope which allows to dynamically decide if this preference should be
 *  enabled (true) or disabled (false).
 * @param visibleIf Evaluator scope which allows to dynamically decide if this preference should be
 *  visible (true) or hidden (false).
 *
 * @since 0.1.0
 */
@ExperimentalJetPrefDatastoreUi
@Composable
fun <T : PreferenceModel> PreferenceUiScope<T>.DialogSliderPreference(
    primaryPref: PreferenceData<Float>,
    secondaryPref: PreferenceData<Float>,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    iconSpaceReserved: Boolean = this.iconSpaceReserved,
    title: String,
    primaryLabel: String,
    secondaryLabel: String,
    valueLabel: @Composable (Float) -> String = { it.toString() },
    summary: @Composable (Float, Float) -> String = { p, s -> "${valueLabel(p)} / ${valueLabel(s)}" },
    min: Float,
    max: Float,
    stepIncrement: Float,
    onPreviewSelectedPrimaryValue: (Float) -> Unit = { },
    onPreviewSelectedSecondaryValue: (Float) -> Unit = { },
    dialogStrings: DialogPrefStrings = LocalDefaultDialogPrefStrings.current,
    enabledIf: PreferenceDataEvaluator = { true },
    visibleIf: PreferenceDataEvaluator = { true },
) {
    DialogSliderPreference(
        primaryPref, secondaryPref, modifier, icon, iconSpaceReserved, title, primaryLabel,
        secondaryLabel, valueLabel, summary, min, max, stepIncrement, onPreviewSelectedPrimaryValue,
        onPreviewSelectedSecondaryValue, dialogStrings, enabledIf, visibleIf,
    ) { it }
}
