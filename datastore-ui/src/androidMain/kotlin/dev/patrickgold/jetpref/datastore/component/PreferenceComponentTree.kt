/*
 * Copyright 2026 Patrick Goldinger
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

package dev.patrickgold.jetpref.datastore.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import dev.patrickgold.jetpref.datastore.model.LocalTime
import dev.patrickgold.jetpref.datastore.model.PreferenceData
import dev.patrickgold.jetpref.datastore.model.PreferenceDataEvaluator
import dev.patrickgold.jetpref.datastore.ui.ListPreferenceEntry

abstract class PreferenceComponentTree {
    protected typealias Screen = PreferenceComponent.Screen

    protected fun switch(
        pref: PreferenceData<Boolean>,
        icon: (@Composable () -> ImageVector)?,
        title: @Composable () -> String,
        summary: (@Composable () -> String)? = null,
        summaryOn: (@Composable () -> String)? = null,
        summaryOff: (@Composable () -> String)? = null,
        enabledIf: PreferenceDataEvaluator = { true },
        visibleIf: PreferenceDataEvaluator = { true },
    ) = object : PreferenceComponent.Switch {
        override val pref = pref
        override val icon = icon
        override val title = title
        override val summary = summary
        override val summaryOn = summaryOn
        override val summaryOff = summaryOff
        override val enabledIf = enabledIf
        override val visibleIf = visibleIf
    }

    protected fun textField(
        pref: PreferenceData<String>,
        icon: (@Composable () -> ImageVector)?,
        title: @Composable () -> String,
        summaryIfBlank: (@Composable () -> String)? = null,
        summaryIfEmpty: (@Composable () -> String)? = null,
        summary: @Composable (String) -> String? = { v ->
            when {
                v.isEmpty() -> summaryIfEmpty?.invoke() ?: v
                v.isBlank() -> summaryIfBlank?.invoke() ?: v
                else -> v
            }
        },
        transformValue: (String) -> String = { it },
        validateValue: (String) -> Unit = { },
        enabledIf: PreferenceDataEvaluator,
        visibleIf: PreferenceDataEvaluator,
    ) = object : PreferenceComponent.TextField {
        override val pref = pref
        override val icon = icon
        override val title = title
        override val summaryIfBlank = summaryIfBlank
        override val summaryIfEmpty = summaryIfEmpty
        override val summary = summary
        override val transformValue = transformValue
        override val validateValue = validateValue
        override val enabledIf = enabledIf
        override val visibleIf = visibleIf
    }

    protected fun <V : Any> list(
        listPref: PreferenceData<V>,
        icon: (@Composable () -> ImageVector)?,
        title: @Composable () -> String,
        enabledIf: PreferenceDataEvaluator,
        visibleIf: PreferenceDataEvaluator,
        entries: List<ListPreferenceEntry<V>>,
    ) = object : PreferenceComponent.ListPref<V> {
        override val listPref = listPref
        override val icon = icon
        override val title = title
        override val enabledIf = enabledIf
        override val visibleIf = visibleIf
        override val entries = entries
    }

    protected fun <V : Any> list(
        listPref: PreferenceData<V>,
        switchPref: PreferenceData<Boolean>,
        icon: (@Composable () -> ImageVector)?,
        title: @Composable () -> String,
        summarySwitchDisabled: @Composable () -> String,
        enabledIf: PreferenceDataEvaluator,
        visibleIf: PreferenceDataEvaluator,
        entries: List<ListPreferenceEntry<V>>,
    ) = object : PreferenceComponent.TogglableListPref<V> {
        override val listPref = listPref
        override val switchPref = switchPref
        override val icon = icon
        override val title = title
        override val summarySwitchDisabled = summarySwitchDisabled
        override val enabledIf = enabledIf
        override val visibleIf = visibleIf
        override val entries = entries
    }

    protected fun slider(
        pref: PreferenceData<Int>,
        icon: (@Composable () -> ImageVector)?,
        title: @Composable () -> String,
        valueLabel: @Composable (Int) -> String = { v -> v.toString() },
        summary: @Composable (Int) -> String = valueLabel,
        min: Int,
        max: Int,
        stepIncrement: Int,
        enabledIf: PreferenceDataEvaluator = { true },
        visibleIf: PreferenceDataEvaluator = { true },
    ) = object : PreferenceComponent.SingleSlider<Int> {
        override val pref = pref
        override val title = title
        override val icon = icon
        override val valueLabel = valueLabel
        override val summary = summary
        override val min = min
        override val max = max
        override val stepIncrement = stepIncrement
        override val enabledIf = enabledIf
        override val visibleIf = visibleIf
        override val convertToV = { v: Float -> v.toInt() }
    }

    protected fun slider(
        pref: PreferenceData<Long>,
        icon: (@Composable () -> ImageVector)?,
        title: @Composable () -> String,
        valueLabel: @Composable (Long) -> String = { v -> v.toString() },
        summary: @Composable (Long) -> String = valueLabel,
        min: Long,
        max: Long,
        stepIncrement: Long,
        enabledIf: PreferenceDataEvaluator = { true },
        visibleIf: PreferenceDataEvaluator = { true },
    ) = object : PreferenceComponent.SingleSlider<Long> {
        override val pref = pref
        override val title = title
        override val icon = icon
        override val valueLabel = valueLabel
        override val summary = summary
        override val min = min
        override val max = max
        override val stepIncrement = stepIncrement
        override val enabledIf = enabledIf
        override val visibleIf = visibleIf
        override val convertToV = { v: Float -> v.toLong() }
    }

    protected fun slider(
        pref: PreferenceData<Float>,
        icon: (@Composable () -> ImageVector)?,
        title: @Composable () -> String,
        valueLabel: @Composable (Float) -> String = { v -> v.toString() },
        summary: @Composable (Float) -> String = valueLabel,
        min: Float,
        max: Float,
        stepIncrement: Float,
        enabledIf: PreferenceDataEvaluator = { true },
        visibleIf: PreferenceDataEvaluator = { true },
    ) = object : PreferenceComponent.SingleSlider<Float> {
        override val pref = pref
        override val title = title
        override val icon = icon
        override val valueLabel = valueLabel
        override val summary = summary
        override val min = min
        override val max = max
        override val stepIncrement = stepIncrement
        override val enabledIf = enabledIf
        override val visibleIf = visibleIf
        override val convertToV = { v: Float -> v }
    }

    protected fun slider(
        pref: PreferenceData<Double>,
        icon: (@Composable () -> ImageVector)?,
        title: @Composable () -> String,
        valueLabel: @Composable (Double) -> String = { v -> v.toString() },
        summary: @Composable (Double) -> String = valueLabel,
        min: Double,
        max: Double,
        stepIncrement: Double,
        enabledIf: PreferenceDataEvaluator = { true },
        visibleIf: PreferenceDataEvaluator = { true },
    ) = object : PreferenceComponent.SingleSlider<Double> {
        override val pref = pref
        override val icon = icon
        override val title = title
        override val valueLabel = valueLabel
        override val summary = summary
        override val min = min
        override val max = max
        override val stepIncrement = stepIncrement
        override val enabledIf = enabledIf
        override val visibleIf = visibleIf
        override val convertToV = { v: Float -> v.toDouble() }
    }

    protected fun dualSlider(
        pref1: PreferenceData<Int>,
        pref2: PreferenceData<Int>,
        icon: (@Composable () -> ImageVector)?,
        title: @Composable () -> String,
        pref1Label: @Composable () -> String,
        pref2Label: @Composable () -> String,
        valueLabel: @Composable (Int) -> String = { v -> v.toString() },
        summary: @Composable (Int, Int) -> String = { v1, v2 ->
            "${valueLabel.invoke(v1)} / ${valueLabel.invoke(v2)}"
        },
        min: Int,
        max: Int,
        stepIncrement: Int,
        enabledIf: PreferenceDataEvaluator = { true },
        visibleIf: PreferenceDataEvaluator = { true },
    ) = object : PreferenceComponent.DualSlider<Int> {
        override val pref1 = pref1
        override val pref2 = pref2
        override val title = title
        override val icon = icon
        override val pref1Label = pref1Label
        override val pref2Label = pref2Label
        override val valueLabel = valueLabel
        override val summary = summary
        override val min = min
        override val max = max
        override val stepIncrement = stepIncrement
        override val enabledIf = enabledIf
        override val visibleIf = visibleIf
        override val convertToV = { v: Float -> v.toInt() }
    }

    protected fun dualSlider(
        pref1: PreferenceData<Long>,
        pref2: PreferenceData<Long>,
        icon: (@Composable () -> ImageVector)?,
        title: @Composable () -> String,
        pref1Label: @Composable () -> String,
        pref2Label: @Composable () -> String,
        valueLabel: @Composable (Long) -> String = { v -> v.toString() },
        summary: @Composable (Long, Long) -> String = { v1, v2 ->
            "${valueLabel.invoke(v1)} / ${valueLabel.invoke(v2)}"
        },
        min: Long,
        max: Long,
        stepIncrement: Long,
        enabledIf: PreferenceDataEvaluator = { true },
        visibleIf: PreferenceDataEvaluator = { true },
    ) = object : PreferenceComponent.DualSlider<Long> {
        override val pref1 = pref1
        override val pref2 = pref2
        override val title = title
        override val icon = icon
        override val pref1Label = pref1Label
        override val pref2Label = pref2Label
        override val valueLabel = valueLabel
        override val summary = summary
        override val min = min
        override val max = max
        override val stepIncrement = stepIncrement
        override val enabledIf = enabledIf
        override val visibleIf = visibleIf
        override val convertToV = { v: Float -> v.toLong() }
    }

    protected fun dualSlider(
        pref1: PreferenceData<Float>,
        pref2: PreferenceData<Float>,
        icon: (@Composable () -> ImageVector)?,
        title: @Composable () -> String,
        pref1Label: @Composable () -> String,
        pref2Label: @Composable () -> String,
        valueLabel: @Composable (Float) -> String = { v -> v.toString() },
        summary: @Composable (Float, Float) -> String = { v1, v2 ->
            "${valueLabel.invoke(v1)} / ${valueLabel.invoke(v2)}"
        },
        min: Float,
        max: Float,
        stepIncrement: Float,
        enabledIf: PreferenceDataEvaluator = { true },
        visibleIf: PreferenceDataEvaluator = { true },
    ) = object : PreferenceComponent.DualSlider<Float>{
        override val pref1 = pref1
        override val pref2 = pref2
        override val title = title
        override val icon = icon
        override val pref1Label = pref1Label
        override val pref2Label = pref2Label
        override val valueLabel = valueLabel
        override val summary = summary
        override val min = min
        override val max = max
        override val stepIncrement = stepIncrement
        override val enabledIf = enabledIf
        override val visibleIf = visibleIf
        override val convertToV = { v: Float -> v }
    }

    protected fun dualSlider(
        pref1: PreferenceData<Double>,
        pref2: PreferenceData<Double>,
        icon: (@Composable () -> ImageVector)?,
        title: @Composable () -> String,
        pref1Label: @Composable () -> String,
        pref2Label: @Composable () -> String,
        valueLabel: @Composable (Double) -> String = { v -> v.toString() },
        summary: @Composable (Double, Double) -> String = { v1, v2 ->
            "${valueLabel.invoke(v1)} / ${valueLabel.invoke(v2)}"
        },
        min: Double,
        max: Double,
        stepIncrement: Double,
        enabledIf: PreferenceDataEvaluator = { true },
        visibleIf: PreferenceDataEvaluator = { true },
    ) = object : PreferenceComponent.DualSlider<Double>{
        override val pref1 = pref1
        override val pref2 = pref2
        override val title = title
        override val icon = icon
        override val pref1Label = pref1Label
        override val pref2Label = pref2Label
        override val valueLabel = valueLabel
        override val summary = summary
        override val min = min
        override val max = max
        override val stepIncrement = stepIncrement
        override val enabledIf = enabledIf
        override val visibleIf = visibleIf
        override val convertToV = { v: Float -> v.toDouble() }
    }

    protected fun colorPicker(
        pref: PreferenceData<Color>,
        icon: (@Composable () -> ImageVector)?,
        title: @Composable () -> String,
        summary: (@Composable () -> String)? = null,
        defaultValueLabel: (@Composable () -> String)? = null,
        showAlphaSlider: Boolean = false,
        enableAdvancedLayout: Boolean = false,
        defaultColors: List<Color>,
        enabledIf: PreferenceDataEvaluator = { true },
        visibleIf: PreferenceDataEvaluator = { true },
    ) = object : PreferenceComponent.ColorPicker {
        override val pref = pref
        override val icon = icon
        override val title = title
        override val summary = summary
        override val defaultValueLabel = defaultValueLabel
        override val showAlphaSlider = showAlphaSlider
        override val enableAdvancedLayout = enableAdvancedLayout
        override val defaultColors = defaultColors
        override val enabledIf = enabledIf
        override val visibleIf = visibleIf
    }

    protected fun localTimePicker(
        pref: PreferenceData<LocalTime>,
        icon: (@Composable () -> ImageVector)?,
        title: @Composable () -> String,
        enabledIf: PreferenceDataEvaluator = { true },
        visibleIf: PreferenceDataEvaluator = { true },
    ) = object : PreferenceComponent.LocalTimePicker {
        override val pref = pref
        override val icon = icon
        override val title = title
        override val enabledIf = enabledIf
        override val visibleIf = visibleIf
    }
}
