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

sealed class PreferenceComponent {
    abstract val title: StringDescriptor.ZeroArg

    sealed class LeafNode : PreferenceComponent() {
        abstract val icon: IconDescriptor?

        abstract val enabledIf: PreferenceDataEvaluator

        abstract val visibleIf: PreferenceDataEvaluator
    }

    sealed class SinglePrefLeafNode<V1 : Any> : LeafNode() {
        abstract val pref: PreferenceData<V1>
    }

    sealed class DualPrefLeafNode<V1 : Any, V2 : Any> : LeafNode() {
        abstract val pref1: PreferenceData<V1>

        abstract val pref2: PreferenceData<V2>
    }

    abstract class Switch(
        override val pref: PreferenceData<Boolean>,
        override val title: StringDescriptor.ZeroArg,
        override val icon: IconDescriptor?,
        val summary: StringDescriptor.ZeroArg?,
        val summaryOn: StringDescriptor.ZeroArg?,
        val summaryOff: StringDescriptor.ZeroArg?,
        override val enabledIf: PreferenceDataEvaluator,
        override val visibleIf: PreferenceDataEvaluator,
    ) : SinglePrefLeafNode<Boolean>()

    abstract class SingleSlider<V>(
        override val pref: PreferenceData<V>,
        override val title: StringDescriptor.ZeroArg,
        override val icon: IconDescriptor?,
        open val valueLabel: StringDescriptor.OneArg<V>,
        open val summary: StringDescriptor.OneArg<V>,
        open val min: V,
        open val max: V,
        open val stepIncrement: V,
        override val enabledIf: PreferenceDataEvaluator,
        override val visibleIf: PreferenceDataEvaluator,
        open val convertToV: (Float) -> V,
    ) : SinglePrefLeafNode<V>() where V : Number, V : Comparable<V> {
        init {
            require(stepIncrement > convertToV(0f)) { "Step increment must be greater than 0!" }
            require(max > min) { "Maximum value ($max) must be greater than minimum value ($min)!" }
        }
    }

    abstract class DualSlider<V>(
        override val pref1: PreferenceData<V>,
        override val pref2: PreferenceData<V>,
        override val title: StringDescriptor.ZeroArg,
        override val icon: IconDescriptor?,
        val pref1Label: StringDescriptor.ZeroArg,
        val pref2Label: StringDescriptor.ZeroArg,
        open val valueLabel: StringDescriptor.OneArg<V>,
        open val summary: StringDescriptor.TwoArg<V, V>,
        open val min: V,
        open val max: V,
        open val stepIncrement: V,
        override val enabledIf: PreferenceDataEvaluator,
        override val visibleIf: PreferenceDataEvaluator,
        open val convertToV: (Float) -> V,
    ) : DualPrefLeafNode<V, V>() where V : Number, V : Comparable<V> {
        init {
            require(stepIncrement > convertToV(0f)) { "Step increment must be greater than 0!" }
            require(max > min) { "Maximum value ($max) must be greater than minimum value ($min)!" }
        }
    }

    sealed class BranchNode : PreferenceComponent()

    abstract class Group(
        override val title: StringDescriptor.ZeroArg,
    ) : BranchNode()

    abstract class Screen(
        override val title: StringDescriptor.ZeroArg,
    ) : BranchNode()
}
