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

import dev.patrickgold.jetpref.datastore.model.PreferenceData
import dev.patrickgold.jetpref.datastore.model.PreferenceDataEvaluator

abstract class PreferenceComponentTree {
    protected typealias Screen = PreferenceComponent.Screen

    protected fun switch(
        pref: PreferenceData<Boolean>,
        title: StringDescriptor.ZeroArg,
        icon: IconDescriptor? = null,
        summary: StringDescriptor.ZeroArg? = null,
        summaryOn: StringDescriptor.ZeroArg? = null,
        summaryOff: StringDescriptor.ZeroArg? = null,
        enabledIf: PreferenceDataEvaluator = { true },
        visibleIf: PreferenceDataEvaluator = { true },
    ) = object : PreferenceComponent.Switch(
        pref = pref,
        title = title,
        icon = icon,
        summary = summary,
        summaryOn = summaryOn,
        summaryOff = summaryOff,
        enabledIf = enabledIf,
        visibleIf = visibleIf,
    ) { }

    protected fun slider(
        pref: PreferenceData<Int>,
        title: StringDescriptor.ZeroArg,
        icon: IconDescriptor? = null,
        valueLabel: StringDescriptor.OneArg<Int> = describedBy { v -> v.toString() },
        summary: StringDescriptor.OneArg<Int> = valueLabel,
        min: Int,
        max: Int,
        stepIncrement: Int,
        enabledIf: PreferenceDataEvaluator = { true },
        visibleIf: PreferenceDataEvaluator = { true },
    ) = object : PreferenceComponent.SingleSlider<Int>(
        pref = pref,
        title = title,
        icon = icon,
        valueLabel = valueLabel,
        summary = summary,
        min = min,
        max = max,
        stepIncrement = stepIncrement,
        enabledIf = enabledIf,
        visibleIf = visibleIf,
        convertToV = { it.toInt() },
    ) { }

    protected fun slider(
        pref: PreferenceData<Long>,
        title: StringDescriptor.ZeroArg,
        icon: IconDescriptor? = null,
        valueLabel: StringDescriptor.OneArg<Long> = describedBy { v -> v.toString() },
        summary: StringDescriptor.OneArg<Long> = valueLabel,
        min: Long,
        max: Long,
        stepIncrement: Long,
        enabledIf: PreferenceDataEvaluator = { true },
        visibleIf: PreferenceDataEvaluator = { true },
    ) = object : PreferenceComponent.SingleSlider<Long>(
        pref = pref,
        title = title,
        icon = icon,
        valueLabel = valueLabel,
        summary = summary,
        min = min,
        max = max,
        stepIncrement = stepIncrement,
        enabledIf = enabledIf,
        visibleIf = visibleIf,
        convertToV = { it.toLong() },
    ) { }

    protected fun slider(
        pref: PreferenceData<Float>,
        title: StringDescriptor.ZeroArg,
        icon: IconDescriptor? = null,
        valueLabel: StringDescriptor.OneArg<Float> = describedBy { v -> v.toString() },
        summary: StringDescriptor.OneArg<Float> = valueLabel,
        min: Float,
        max: Float,
        stepIncrement: Float,
        enabledIf: PreferenceDataEvaluator = { true },
        visibleIf: PreferenceDataEvaluator = { true },
    ) = object : PreferenceComponent.SingleSlider<Float>(
        pref = pref,
        title = title,
        icon = icon,
        valueLabel = valueLabel,
        summary = summary,
        min = min,
        max = max,
        stepIncrement = stepIncrement,
        enabledIf = enabledIf,
        visibleIf = visibleIf,
        convertToV = { it },
    ) { }

    protected fun slider(
        pref: PreferenceData<Double>,
        title: StringDescriptor.ZeroArg,
        icon: IconDescriptor? = null,
        valueLabel: StringDescriptor.OneArg<Double> = describedBy { v -> v.toString() },
        summary: StringDescriptor.OneArg<Double> = valueLabel,
        min: Double,
        max: Double,
        stepIncrement: Double,
        enabledIf: PreferenceDataEvaluator = { true },
        visibleIf: PreferenceDataEvaluator = { true },
    ) = object : PreferenceComponent.SingleSlider<Double>(
        pref = pref,
        title = title,
        icon = icon,
        valueLabel = valueLabel,
        summary = summary,
        min = min,
        max = max,
        stepIncrement = stepIncrement,
        enabledIf = enabledIf,
        visibleIf = visibleIf,
        convertToV = { it.toDouble() },
    ) { }

    protected fun dualSlider(
        pref1: PreferenceData<Int>,
        pref2: PreferenceData<Int>,
        title: StringDescriptor.ZeroArg,
        icon: IconDescriptor? = null,
        pref1Label: StringDescriptor.ZeroArg,
        pref2Label: StringDescriptor.ZeroArg,
        valueLabel: StringDescriptor.OneArg<Int> = describedBy { v -> v.toString() },
        summary: StringDescriptor.TwoArg<Int, Int> = describedBy { v1, v2 ->
            "${valueLabel.resolve(v1)} / ${valueLabel.resolve(v2)}"
        },
        min: Int,
        max: Int,
        stepIncrement: Int,
        enabledIf: PreferenceDataEvaluator = { true },
        visibleIf: PreferenceDataEvaluator = { true },
    ) = object : PreferenceComponent.DualSlider<Int>(
        pref1 = pref1,
        pref2 = pref2,
        title = title,
        icon = icon,
        pref1Label = pref1Label,
        pref2Label = pref2Label,
        valueLabel = valueLabel,
        summary = summary,
        min = min,
        max = max,
        stepIncrement = stepIncrement,
        enabledIf = enabledIf,
        visibleIf = visibleIf,
        convertToV = { it.toInt() },
    ) { }

    protected fun dualSlider(
        pref1: PreferenceData<Long>,
        pref2: PreferenceData<Long>,
        title: StringDescriptor.ZeroArg,
        icon: IconDescriptor? = null,
        pref1Label: StringDescriptor.ZeroArg,
        pref2Label: StringDescriptor.ZeroArg,
        valueLabel: StringDescriptor.OneArg<Long> = describedBy { v -> v.toString() },
        summary: StringDescriptor.TwoArg<Long, Long> = describedBy { v1, v2 ->
            "${valueLabel.resolve(v1)} / ${valueLabel.resolve(v2)}"
        },
        min: Long,
        max: Long,
        stepIncrement: Long,
        enabledIf: PreferenceDataEvaluator = { true },
        visibleIf: PreferenceDataEvaluator = { true },
    ) = object : PreferenceComponent.DualSlider<Long>(
        pref1 = pref1,
        pref2 = pref2,
        title = title,
        icon = icon,
        pref1Label = pref1Label,
        pref2Label = pref2Label,
        valueLabel = valueLabel,
        summary = summary,
        min = min,
        max = max,
        stepIncrement = stepIncrement,
        enabledIf = enabledIf,
        visibleIf = visibleIf,
        convertToV = { it.toLong() },
    ) { }

    protected fun dualSlider(
        pref1: PreferenceData<Float>,
        pref2: PreferenceData<Float>,
        title: StringDescriptor.ZeroArg,
        icon: IconDescriptor? = null,
        pref1Label: StringDescriptor.ZeroArg,
        pref2Label: StringDescriptor.ZeroArg,
        valueLabel: StringDescriptor.OneArg<Float> = describedBy { v -> v.toString() },
        summary: StringDescriptor.TwoArg<Float, Float> = describedBy { v1, v2 ->
            "${valueLabel.resolve(v1)} / ${valueLabel.resolve(v2)}"
        },
        min: Float,
        max: Float,
        stepIncrement: Float,
        enabledIf: PreferenceDataEvaluator = { true },
        visibleIf: PreferenceDataEvaluator = { true },
    ) = object : PreferenceComponent.DualSlider<Float>(
        pref1 = pref1,
        pref2 = pref2,
        title = title,
        icon = icon,
        pref1Label = pref1Label,
        pref2Label = pref2Label,
        valueLabel = valueLabel,
        summary = summary,
        min = min,
        max = max,
        stepIncrement = stepIncrement,
        enabledIf = enabledIf,
        visibleIf = visibleIf,
        convertToV = { it },
    ) { }

    protected fun dualSlider(
        pref1: PreferenceData<Double>,
        pref2: PreferenceData<Double>,
        title: StringDescriptor.ZeroArg,
        icon: IconDescriptor? = null,
        pref1Label: StringDescriptor.ZeroArg,
        pref2Label: StringDescriptor.ZeroArg,
        valueLabel: StringDescriptor.OneArg<Double> = describedBy { v -> v.toString() },
        summary: StringDescriptor.TwoArg<Double, Double> = describedBy { v1, v2 ->
            "${valueLabel.resolve(v1)} / ${valueLabel.resolve(v2)}"
        },
        min: Double,
        max: Double,
        stepIncrement: Double,
        enabledIf: PreferenceDataEvaluator = { true },
        visibleIf: PreferenceDataEvaluator = { true },
    ) = object : PreferenceComponent.DualSlider<Double>(
        pref1 = pref1,
        pref2 = pref2,
        title = title,
        icon = icon,
        pref1Label = pref1Label,
        pref2Label = pref2Label,
        valueLabel = valueLabel,
        summary = summary,
        min = min,
        max = max,
        stepIncrement = stepIncrement,
        enabledIf = enabledIf,
        visibleIf = visibleIf,
        convertToV = { it.toDouble() },
    ) { }
}
