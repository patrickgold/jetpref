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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderColors
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import dev.patrickgold.jetpref.datastore.component.PreferenceComponent
import dev.patrickgold.jetpref.datastore.model.PreferenceData
import dev.patrickgold.jetpref.datastore.model.PreferenceDataEvaluator
import dev.patrickgold.jetpref.datastore.model.collectAsState
import dev.patrickgold.jetpref.material.ui.JetPrefAlertDialog
import kotlinx.coroutines.launch
import kotlin.math.round
import kotlin.math.roundToInt

/**
 * Material preference which provides a dialog with a slider for choosing a value.
 *
 * @param component Component describing what to display.
 * @param modifier Modifier to be applied to the underlying preference.
 *
 * @since 0.4.0
 */
@Composable
fun <V> DialogSliderPreference(
    component: PreferenceComponent.SingleSlider<V>,
    modifier: Modifier = Modifier,
) where V : Number, V : Comparable<V> {
    DialogSliderPreferenceImpl(
        pref = component.pref,
        modifier = modifier,
        icon = component.icon.invoke(),
        title = component.title.invoke(),
        valueLabel = component.valueLabel,
        summary = component.summary.invoke(),
        min = component.min,
        max = component.max,
        stepIncrement = component.stepIncrement,
        enabledIf = component.enabledIf,
        visibleIf = component.visibleIf,
        convertToV = component.convertToV,
    )
}

/**
 * Material preference which provides a dialog with two sliders for choosing two values at once.
 *
 * @param component Component describing what to display.
 * @param modifier Modifier to be applied to the underlying preference.
 *
 * @since 0.4.0
 */
@Composable
fun <V> DialogSliderPreference(
    component: PreferenceComponent.DualSlider<V>,
    modifier: Modifier = Modifier,
) where V : Number, V : Comparable<V> {
    DialogSliderPreferenceImpl(
        primaryPref = component.pref1,
        secondaryPref = component.pref2,
        modifier = modifier,
        icon = component.icon.invoke(),
        title = component.title.invoke(),
        primaryLabel = component.pref1Label.invoke(),
        secondaryLabel = component.pref2Label.invoke(),
        valueLabel = component.valueLabel,
        summary = component.summary.invoke(),
        min = component.min,
        max = component.max,
        stepIncrement = component.stepIncrement,
        enabledIf = component.enabledIf,
        visibleIf = component.visibleIf,
        convertToV = component.convertToV,
    )
}

@Composable
private fun <V> DialogSliderPreferenceImpl(
    pref: PreferenceData<V>,
    modifier: Modifier,
    icon: ImageVector? = null,
    title: String,
    valueLabel: @Composable (V) -> String,
    summary: String?,
    min: V,
    max: V,
    stepIncrement: V,
    enabledIf: PreferenceDataEvaluator,
    visibleIf: PreferenceDataEvaluator,
    convertToV: (Float) -> V,
) where V : Number, V : Comparable<V> {
    require(stepIncrement > convertToV(0f)) { "Step increment must be greater than 0!" }
    require(max > min) { "Maximum value ($max) must be greater than minimum value ($min)!" }

    val dialogStrings = LocalDialogPrefStrings.current
    val scope = rememberCoroutineScope()
    val prefValue by pref.collectAsState()
    var sliderValue by remember { mutableFloatStateOf(0.0f) }
    var isDialogOpen by remember { mutableStateOf(false) }

    Preference(
        modifier = modifier,
        icon = icon,
        title = title,
        summary = summary,
        enabledIf = enabledIf,
        visibleIf = visibleIf,
        onClick = {
            sliderValue = prefValue.toFloat()
            isDialogOpen = true
        },
    )

    if (isDialogOpen) {
        JetPrefAlertDialog(
            title = title,
            confirmLabel = dialogStrings.confirmLabel,
            onConfirm = {
                scope.launch {
                    pref.set(convertToV(sliderValue))
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
                    pref.reset()
                }
                isDialogOpen = false
            },
        ) {
            Column {
                Text(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 8.dp),
                    text = valueLabel(convertToV(sliderValue)),
                )
                Slider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    value = sliderValue,
                    valueRange = min.toFloat()..max.toFloat(),
                    steps = ((max.toFloat() - min.toFloat()) / stepIncrement.toFloat()).roundToInt() - 1,
                    onValueChange = { sliderValue = round(it) },
                    colors = customSliderDialogColors(),
                )
            }
        }
    }
}

@Composable
private fun <V> DialogSliderPreferenceImpl(
    primaryPref: PreferenceData<V>,
    secondaryPref: PreferenceData<V>,
    modifier: Modifier,
    icon: ImageVector? = null,
    title: String,
    primaryLabel: String,
    secondaryLabel: String,
    valueLabel: @Composable (V) -> String,
    summary: String?,
    min: V,
    max: V,
    stepIncrement: V,
    enabledIf: PreferenceDataEvaluator,
    visibleIf: PreferenceDataEvaluator,
    convertToV: (Float) -> V,
) where V : Number, V : Comparable<V> {
    require(stepIncrement > convertToV(0f)) { "Step increment must be greater than 0!" }
    require(max > min) { "Maximum value ($max) must be greater than minimum value ($min)!" }

    val dialogStrings = LocalDialogPrefStrings.current
    val scope = rememberCoroutineScope()
    val primaryPrefValue by primaryPref.collectAsState()
    val secondaryPrefValue by secondaryPref.collectAsState()
    var primarySliderValue by remember { mutableStateOf(convertToV(0.0f)) }
    var secondarySliderValue by remember { mutableStateOf(convertToV(0.0f)) }
    var isDialogOpen by remember { mutableStateOf(false) }

    Preference(
        modifier = modifier,
        icon = icon,
        title = title,
        summary = summary,
        enabledIf = enabledIf,
        visibleIf = visibleIf,
        onClick = {
            primarySliderValue = primaryPrefValue
            secondarySliderValue = secondaryPrefValue
            isDialogOpen = true
        },
    )

    if (isDialogOpen) {
        JetPrefAlertDialog(
            title = title,
            confirmLabel = dialogStrings.confirmLabel,
            onConfirm = {
                scope.launch {
                    primaryPref.set(primarySliderValue)
                    secondaryPref.set(secondarySliderValue)
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
                    primaryPref.reset()
                    secondaryPref.reset()
                }
                isDialogOpen = false
            },
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(primaryLabel)
                    Text(valueLabel(primarySliderValue))
                }
                Slider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    value = primarySliderValue.toFloat(),
                    valueRange = min.toFloat()..max.toFloat(),
                    steps = ((max.toFloat() - min.toFloat()) / stepIncrement.toFloat()).toInt() - 1,
                    onValueChange = { primarySliderValue = convertToV(it) },
                    colors = customSliderDialogColors(),
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(secondaryLabel)
                    Text(valueLabel(secondarySliderValue))
                }
                Slider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    value = secondarySliderValue.toFloat(),
                    valueRange = min.toFloat()..max.toFloat(),
                    steps = ((max.toFloat() - min.toFloat()) / stepIncrement.toFloat()).toInt() - 1,
                    onValueChange = { secondarySliderValue = convertToV(it) },
                    colors = customSliderDialogColors(),
                )
            }
        }
    }
}

@Composable
private fun customSliderDialogColors(): SliderColors {
    return SliderDefaults.colors(
        thumbColor = MaterialTheme.colorScheme.primary,
        activeTrackColor = MaterialTheme.colorScheme.primary,
        activeTickColor = Color.Transparent,
        inactiveTrackColor = MaterialTheme.colorScheme.primaryContainer,
        inactiveTickColor = Color.Transparent,
    )
}
