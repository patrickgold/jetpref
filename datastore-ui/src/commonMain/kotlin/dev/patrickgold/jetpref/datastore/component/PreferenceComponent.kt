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
import dev.patrickgold.jetpref.datastore.model.LocalTime
import dev.patrickgold.jetpref.datastore.model.PreferenceData
import dev.patrickgold.jetpref.datastore.model.PreferenceDataEvaluator
import dev.patrickgold.jetpref.datastore.ui.ListPreferenceEntry

sealed interface PreferenceComponent : Presentable {
    val id: Int

    val enabledIf: PreferenceDataEvaluator

    val visibleIf: PreferenceDataEvaluator

    val associatedGroup: GroupHeader?

    val level: Int

    @Composable
    fun Render()

    interface GroupHeader : PreferenceComponent

    interface ComposableContent : PreferenceComponent {
        val content: @Composable () -> Unit
    }

    interface NavigationEntry : PreferenceComponent {
        val targetScreen: PreferenceScreen
    }

    interface Switch : PreferenceComponent {
        val pref: PreferenceData<Boolean>
    }

    interface ListPicker<V : Any> : PreferenceComponent {
        val listPref: PreferenceData<V>
        val entries: List<ListPreferenceEntry<V>>
    }

    interface ListPickerWithSwitch<V : Any> : ListPicker<V> {
        val switchPref: PreferenceData<Boolean>
    }

    interface ColorPicker : PreferenceComponent {
        val pref: PreferenceData<Color>
        val defaultValueLabel: @Composable () -> String?
        val showAlphaSlider: Boolean
        val enableAdvancedLayout: Boolean
        val defaultColors: List<Color>
    }

    interface LocalTimePicker : PreferenceComponent {
        val pref: PreferenceData<LocalTime>
    }

    interface TextField : PreferenceComponent {
        val pref: PreferenceData<String>
        val transformValue: (String) -> String
        val validateValue: (String) -> Unit
    }

    interface SingleSlider<V> : PreferenceComponent where V : Number, V : Comparable<V> {
        val pref: PreferenceData<V>
        val valueLabel: @Composable (V) -> String
        val min: V
        val max: V
        val stepIncrement: V
        val convertToV: (Float) -> V
    }

    interface DualSlider<V> : PreferenceComponent where V : Number, V : Comparable<V> {
        val pref1: PreferenceData<V>
        val pref2: PreferenceData<V>
        val pref1Label: @Composable () -> String
        val pref2Label: @Composable () -> String
        val valueLabel: @Composable (V) -> String
        val min: V
        val max: V
        val stepIncrement: V
        val convertToV: (Float) -> V
    }
}
