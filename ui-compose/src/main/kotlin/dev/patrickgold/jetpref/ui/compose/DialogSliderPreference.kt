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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Slider
import androidx.compose.material.SliderDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import dev.patrickgold.jetpref.datastore.model.PreferenceData
import dev.patrickgold.jetpref.datastore.model.PreferenceDataEvaluator
import dev.patrickgold.jetpref.datastore.model.PreferenceDataEvaluatorScope
import dev.patrickgold.jetpref.datastore.model.PreferenceModel
import dev.patrickgold.jetpref.datastore.model.observeAsState
import dev.patrickgold.jetpref.ui.compose.annotations.ExperimentalJetPrefUi

@ExperimentalJetPrefUi
@Composable
internal fun <T : PreferenceModel, V : Number> PreferenceUiScope<T>.DialogSliderPreference(
    pref: PreferenceData<V>,
    @DrawableRes iconId: Int?,
    iconSpaceReserved: Boolean,
    title: String,
    summary: String,
    unit: String,
    min: V,
    max: V,
    stepIncrement: V,
    enabledIf: PreferenceDataEvaluator,
    visibleIf: PreferenceDataEvaluator,
    convertToV: (Float) -> V,
) {
    val prefValue by pref.observeAsState()
    val (sliderValue, setSliderValue) = remember { mutableStateOf(0.0f) }
    val isDialogOpen = remember { mutableStateOf(false) }

    val evalScope = PreferenceDataEvaluatorScope.instance()
    if (this.visibleIf(evalScope) && visibleIf(evalScope)) {
        val isEnabled = this.enabledIf(evalScope) && enabledIf(evalScope)
        JetPrefListItem(
            icon = maybeJetIcon(iconId, iconSpaceReserved),
            text = title,
            secondaryText = summary.formatValue(prefValue),
            modifier = Modifier
                .clickable(
                    enabled = isEnabled,
                    role = Role.Button,
                    onClick = {
                        setSliderValue(prefValue.toFloat())
                        isDialogOpen.value = true
                    }
                ),
            enabled = isEnabled,
        )
        if (isDialogOpen.value) {
            JetPrefAlertDialog(
                title = title,
                confirmLabel = stringResource(android.R.string.ok),
                onConfirm = {
                    pref.set(convertToV(sliderValue))
                    isDialogOpen.value = false
                },
                dismissLabel = stringResource(android.R.string.cancel),
                onDismiss = { isDialogOpen.value = false },
                neutralLabel = "Default",
                onNeutral = {
                    pref.reset()
                    isDialogOpen.value = false
                }
            ) {
                Column {
                    Text(
                        text = unit.formatValue(convertToV(sliderValue)),
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                    )
                    Slider(
                        value = sliderValue,
                        valueRange = min.toFloat()..max.toFloat(),
                        steps = ((max.toFloat() - min.toFloat()) / stepIncrement.toFloat()).toInt() - 1,
                        onValueChange = {
                            setSliderValue(it)
                        },
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colors.primary,
                            activeTrackColor = MaterialTheme.colors.primary,
                            activeTickColor = Color.Transparent,
                            inactiveTrackColor = MaterialTheme.colors.onSurface.copy(
                                alpha = SliderDefaults.InactiveTrackAlpha,
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

@ExperimentalJetPrefUi
@Composable
internal fun <T : PreferenceModel, V : Number> PreferenceUiScope<T>.DialogSliderPreference(
    primaryPref: PreferenceData<V>,
    secondaryPref: PreferenceData<V>,
    @DrawableRes iconId: Int?,
    iconSpaceReserved: Boolean,
    title: String,
    primaryLabel: String,
    secondaryLabel: String,
    summary: String,
    unit: String,
    min: V,
    max: V,
    stepIncrement: V,
    enabledIf: PreferenceDataEvaluator,
    visibleIf: PreferenceDataEvaluator,
    convertToV: (Float) -> V,
) {
    val primaryPrefValue by primaryPref.observeAsState()
    val secondaryPrefValue by secondaryPref.observeAsState()
    val (primarySliderValue, setPrimarySliderValue) = remember { mutableStateOf(convertToV(0.0f)) }
    val (secondarySliderValue, setSecondarySliderValue) = remember { mutableStateOf(convertToV(0.0f)) }
    val isDialogOpen = remember { mutableStateOf(false) }

    val evalScope = PreferenceDataEvaluatorScope.instance()
    if (this.visibleIf(evalScope) && visibleIf(evalScope)) {
        val isEnabled = this.enabledIf(evalScope) && enabledIf(evalScope)
        JetPrefListItem(
            icon = maybeJetIcon(iconId, iconSpaceReserved),
            text = title,
            secondaryText = summary.formatValue(primaryPrefValue, secondaryPrefValue),
            modifier = Modifier
                .clickable(
                    enabled = isEnabled,
                    role = Role.Button,
                    onClick = {
                        setPrimarySliderValue(primaryPrefValue)
                        setSecondarySliderValue(secondaryPrefValue)
                        isDialogOpen.value = true
                    }
                ),
            enabled = isEnabled,
        )
        if (isDialogOpen.value) {
            JetPrefAlertDialog(
                title = title,
                confirmLabel = stringResource(android.R.string.ok),
                onConfirm = {
                    primaryPref.set(primarySliderValue)
                    secondaryPref.set(secondarySliderValue)
                    isDialogOpen.value = false
                },
                dismissLabel = stringResource(android.R.string.cancel),
                onDismiss = { isDialogOpen.value = false },
                neutralLabel = "Default",
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
                        Text(unit.formatValue(primarySliderValue))
                    }
                    Slider(
                        value = primarySliderValue.toFloat(),
                        valueRange = min.toFloat()..max.toFloat(),
                        steps = ((max.toFloat() - min.toFloat()) / stepIncrement.toFloat()).toInt() - 1,
                        onValueChange = {
                            setPrimarySliderValue(convertToV(it))
                        },
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colors.primary,
                            activeTrackColor = MaterialTheme.colors.primary,
                            activeTickColor = Color.Transparent,
                            inactiveTrackColor = MaterialTheme.colors.onSurface.copy(
                                alpha = SliderDefaults.InactiveTrackAlpha,
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
                        Text(unit.formatValue(secondarySliderValue))
                    }
                    Slider(
                        value = secondarySliderValue.toFloat(),
                        valueRange = min.toFloat()..max.toFloat(),
                        steps = ((max.toFloat() - min.toFloat()) / stepIncrement.toFloat()).toInt() - 1,
                        onValueChange = {
                            setSecondarySliderValue(convertToV(it))
                        },
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colors.primary,
                            activeTrackColor = MaterialTheme.colors.primary,
                            activeTickColor = Color.Transparent,
                            inactiveTrackColor = MaterialTheme.colors.onSurface.copy(
                                alpha = SliderDefaults.InactiveTrackAlpha,
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

@ExperimentalJetPrefUi
@Composable
fun <T : PreferenceModel> PreferenceUiScope<T>.DialogSliderPreference(
    ref: PreferenceData<Int>,
    @DrawableRes iconId: Int? = null,
    iconSpaceReserved: Boolean = this.iconSpaceReserved,
    title: String,
    unit: String = "{v}",
    summary: String = unit,
    min: Int,
    max: Int,
    stepIncrement: Int,
    enabledIf: PreferenceDataEvaluator = { true },
    visibleIf: PreferenceDataEvaluator = { true },
) {
    DialogSliderPreference(
        ref, iconId, iconSpaceReserved, title, summary, unit, min, max,
        stepIncrement, enabledIf, visibleIf
    ) { it.toInt() }
}

@ExperimentalJetPrefUi
@Composable
fun <T : PreferenceModel> PreferenceUiScope<T>.DialogSliderPreference(
    primaryPref: PreferenceData<Int>,
    secondaryPref: PreferenceData<Int>,
    @DrawableRes iconId: Int? = null,
    iconSpaceReserved: Boolean = this.iconSpaceReserved,
    title: String,
    primaryLabel: String,
    secondaryLabel: String,
    unit: String = "{v}",
    summary: String = "$unit / $unit",
    min: Int,
    max: Int,
    stepIncrement: Int,
    enabledIf: PreferenceDataEvaluator = { true },
    visibleIf: PreferenceDataEvaluator = { true },
) {
    DialogSliderPreference(
        primaryPref, secondaryPref, iconId, iconSpaceReserved, title,
        primaryLabel, secondaryLabel, summary, unit, min, max,
        stepIncrement, enabledIf, visibleIf
    ) { it.toInt() }
}

@ExperimentalJetPrefUi
@Composable
fun <T : PreferenceModel> PreferenceUiScope<T>.DialogSliderPreference(
    ref: PreferenceData<Long>,
    @DrawableRes iconId: Int? = null,
    iconSpaceReserved: Boolean = this.iconSpaceReserved,
    title: String,
    unit: String = "{v}",
    summary: String = unit,
    min: Long,
    max: Long,
    stepIncrement: Long,
    enabledIf: PreferenceDataEvaluator = { true },
    visibleIf: PreferenceDataEvaluator = { true },
) {
    DialogSliderPreference(
        ref, iconId, iconSpaceReserved, title, summary, unit, min, max,
        stepIncrement, enabledIf, visibleIf
    ) { it.toLong() }
}

@ExperimentalJetPrefUi
@Composable
fun <T : PreferenceModel> PreferenceUiScope<T>.DialogSliderPreference(
    primaryPref: PreferenceData<Long>,
    secondaryPref: PreferenceData<Long>,
    @DrawableRes iconId: Int? = null,
    iconSpaceReserved: Boolean = this.iconSpaceReserved,
    title: String,
    primaryLabel: String,
    secondaryLabel: String,
    unit: String = "{v}",
    summary: String = "$unit / $unit",
    min: Long,
    max: Long,
    stepIncrement: Long,
    enabledIf: PreferenceDataEvaluator = { true },
    visibleIf: PreferenceDataEvaluator = { true },
) {
    DialogSliderPreference(
        primaryPref, secondaryPref, iconId, iconSpaceReserved, title,
        primaryLabel, secondaryLabel, summary, unit, min, max,
        stepIncrement, enabledIf, visibleIf
    ) { it.toLong() }
}

@ExperimentalJetPrefUi
@Composable
fun <T : PreferenceModel> PreferenceUiScope<T>.DialogSliderPreference(
    ref: PreferenceData<Double>,
    @DrawableRes iconId: Int? = null,
    iconSpaceReserved: Boolean = this.iconSpaceReserved,
    title: String,
    unit: String = "{v}",
    summary: String = unit,
    min: Double,
    max: Double,
    stepIncrement: Double,
    enabledIf: PreferenceDataEvaluator = { true },
    visibleIf: PreferenceDataEvaluator = { true },
) {
    DialogSliderPreference(
        ref, iconId, iconSpaceReserved, title, summary, unit, min, max,
        stepIncrement, enabledIf, visibleIf
    ) { it.toDouble() }
}

@ExperimentalJetPrefUi
@Composable
fun <T : PreferenceModel> PreferenceUiScope<T>.DialogSliderPreference(
    primaryPref: PreferenceData<Double>,
    secondaryPref: PreferenceData<Double>,
    @DrawableRes iconId: Int? = null,
    iconSpaceReserved: Boolean = this.iconSpaceReserved,
    title: String,
    primaryLabel: String,
    secondaryLabel: String,
    unit: String = "{v}",
    summary: String = "$unit / $unit",
    min: Double,
    max: Double,
    stepIncrement: Double,
    enabledIf: PreferenceDataEvaluator = { true },
    visibleIf: PreferenceDataEvaluator = { true },
) {
    DialogSliderPreference(
        primaryPref, secondaryPref, iconId, iconSpaceReserved, title,
        primaryLabel, secondaryLabel, summary, unit, min, max,
        stepIncrement, enabledIf, visibleIf
    ) { it.toDouble() }
}

@ExperimentalJetPrefUi
@Composable
fun <T : PreferenceModel> PreferenceUiScope<T>.DialogSliderPreference(
    ref: PreferenceData<Float>,
    @DrawableRes iconId: Int? = null,
    iconSpaceReserved: Boolean = this.iconSpaceReserved,
    title: String,
    unit: String = "{v}",
    summary: String = unit,
    min: Float,
    max: Float,
    stepIncrement: Float,
    enabledIf: PreferenceDataEvaluator = { true },
    visibleIf: PreferenceDataEvaluator = { true },
) {
    DialogSliderPreference(
        ref, iconId, iconSpaceReserved, title, summary, unit, min, max,
        stepIncrement, enabledIf, visibleIf
    ) { it }
}

@ExperimentalJetPrefUi
@Composable
fun <T : PreferenceModel> PreferenceUiScope<T>.DialogSliderPreference(
    primaryPref: PreferenceData<Float>,
    secondaryPref: PreferenceData<Float>,
    @DrawableRes iconId: Int? = null,
    iconSpaceReserved: Boolean = this.iconSpaceReserved,
    title: String,
    primaryLabel: String,
    secondaryLabel: String,
    unit: String = "{v}",
    summary: String = "$unit / $unit",
    min: Float,
    max: Float,
    stepIncrement: Float,
    enabledIf: PreferenceDataEvaluator = { true },
    visibleIf: PreferenceDataEvaluator = { true },
) {
    DialogSliderPreference(
        primaryPref, secondaryPref, iconId, iconSpaceReserved, title,
        primaryLabel, secondaryLabel, summary, unit, min, max,
        stepIncrement, enabledIf, visibleIf
    ) { it }
}
