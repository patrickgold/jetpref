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
import dev.patrickgold.jetpref.datastore.ui.ListPreferenceEntry

sealed interface PreferenceComponentItem : PreferenceComponent {
    @Composable
    fun Render()

    interface GroupTitle : PreferenceComponentItem

    interface ComposableContent : PreferenceComponentItem {
        val content: @Composable () -> Unit
    }

    interface NavigationEntry : PreferenceComponentItem {
        val targetScreen: PreferenceComponentScreen
        val summary: (@Composable () -> String)?
    }

    interface Switch : PreferenceComponentItem {
        val pref: PreferenceData<Boolean>
        val summary: (@Composable () -> String)?
        val summaryOn: (@Composable () -> String)?
        val summaryOff: (@Composable () -> String)?
    }

    interface ListPicker<V : Any> : PreferenceComponentItem {
        val listPref: PreferenceData<V>
        val entries: @Composable () -> List<ListPreferenceEntry<V>>
    }

    interface ListPickerWithSwitch<V : Any> : ListPicker<V> {
        val switchPref: PreferenceData<Boolean>
        val summarySwitchDisabled: (@Composable () -> String)?
    }

    interface ColorPicker : PreferenceComponentItem {
        val pref: PreferenceData<Color>
        val summary: (@Composable () -> String)?
        val defaultValueLabel: (@Composable () -> String)?
        val showAlphaSlider: Boolean
        val enableAdvancedLayout: Boolean
        val defaultColors: List<Color>
    }

    interface LocalTimePicker : PreferenceComponentItem {
        val pref: PreferenceData<LocalTime>
    }

    interface TextField : PreferenceComponentItem {
        val pref: PreferenceData<String>
        val summaryIfBlank: (@Composable () -> String)?
        val summaryIfEmpty: (@Composable () -> String)?
        val summary: @Composable (String) -> String?
        val transformValue: (String) -> String
        val validateValue: (String) -> Unit
    }

    interface SingleSlider<V> : PreferenceComponentItem where V : Number, V : Comparable<V> {
        val pref: PreferenceData<V>
        val valueLabel: @Composable (V) -> String
        val summary: @Composable (V) -> String
        val min: V
        val max: V
        val stepIncrement: V
        val convertToV: (Float) -> V
    }

    interface DualSlider<V> : PreferenceComponentItem where V : Number, V : Comparable<V> {
        val pref1: PreferenceData<V>
        val pref2: PreferenceData<V>
        val pref1Label: @Composable () -> String
        val pref2Label: @Composable () -> String
        val valueLabel: @Composable (V) -> String
        val summary: @Composable (V, V) -> String
        val min: V
        val max: V
        val stepIncrement: V
        val convertToV: (Float) -> V
    }
}
