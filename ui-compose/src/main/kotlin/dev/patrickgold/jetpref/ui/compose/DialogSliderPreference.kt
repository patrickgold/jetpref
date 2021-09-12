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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Slider
import androidx.compose.material.SliderDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import dev.patrickgold.jetpref.datastore.model.PreferenceData
import dev.patrickgold.jetpref.datastore.model.PreferenceDataEvaluator
import dev.patrickgold.jetpref.datastore.model.PreferenceDataEvaluatorScope
import dev.patrickgold.jetpref.datastore.model.PreferenceModel
import dev.patrickgold.jetpref.datastore.model.observeAsState

@Composable
internal fun <V : Number> DialogSliderPreference(
    ref: PreferenceData<V>,
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
    val pref = ref.observeAsState()
    val (sliderValue, setSliderValue) = remember { mutableStateOf(convertToV(0.0f)) }
    val isDialogOpen = remember { mutableStateOf(false) }

    if (visibleIf(PreferenceDataEvaluatorScope.instance())) {
        val isEnabled = enabledIf(PreferenceDataEvaluatorScope.instance())
        JetPrefListItem(
            icon = maybeJetIcon(iconId, iconSpaceReserved),
            text = title,
            secondaryText = summary.formatValue(pref.value),
            modifier = Modifier
                .clickable(
                    enabled = isEnabled,
                    role = Role.Button,
                    onClick = {
                        setSliderValue(pref.value)
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
                    ref.set(sliderValue)
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
                    Text(
                        text = unit.formatValue(sliderValue),
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                    )
                    Slider(
                        value = sliderValue.toFloat(),
                        valueRange = min.toFloat()..max.toFloat(),
                        steps = ((max.toFloat() - min.toFloat()) / stepIncrement.toFloat()).toInt() - 1,
                        onValueChange = {
                            setSliderValue(convertToV(it))
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
                        modifier = Modifier.padding(horizontal = 8.dp).fillMaxWidth(),
                    )
                }
            }
        }
    }
}

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
    enabledIf: PreferenceDataEvaluator = this.enabledIf,
    visibleIf: PreferenceDataEvaluator = this.visibleIf,
) {
    DialogSliderPreference(
        ref, iconId, iconSpaceReserved, title, summary, unit, min, max,
        stepIncrement, enabledIf, visibleIf
    ) { it.toInt() }
}

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
    enabledIf: PreferenceDataEvaluator = this.enabledIf,
    visibleIf: PreferenceDataEvaluator = this.visibleIf,
) {
    DialogSliderPreference(
        ref, iconId, iconSpaceReserved, title, summary, unit, min, max,
        stepIncrement, enabledIf, visibleIf
    ) { it.toLong() }
}

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
    enabledIf: PreferenceDataEvaluator = this.enabledIf,
    visibleIf: PreferenceDataEvaluator = this.visibleIf,
) {
    DialogSliderPreference(
        ref, iconId, iconSpaceReserved, title, summary, unit, min, max,
        stepIncrement, enabledIf, visibleIf
    ) { it.toDouble() }
}

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
    enabledIf: PreferenceDataEvaluator = this.enabledIf,
    visibleIf: PreferenceDataEvaluator = this.visibleIf,
) {
    DialogSliderPreference(
        ref, iconId, iconSpaceReserved, title, summary, unit, min, max,
        stepIncrement, enabledIf, visibleIf
    ) { it }
}
