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

package dev.patrickgold.jetpref.datastore.model

import androidx.compose.runtime.Composable

class PreferenceDataEvaluator {
    companion object {
        private val staticInstance = PreferenceDataEvaluator()

        fun instance() = staticInstance
    }

    @Composable
    infix fun <V : Any> PreferenceData<V>.isEqualTo(other: PreferenceData<V>): Boolean {
        val pref1 = this.observeAsState()
        val pref2 = other.observeAsState()
        return pref1.value == pref2.value
    }

    @Composable
    infix fun <V : Any> PreferenceData<V>.isEqualTo(other: V): Boolean {
        val pref = this.observeAsState()
        return pref.value == other
    }

    @Composable
    infix fun <V : Any> V.isEqualTo(other: PreferenceData<V>): Boolean {
        val pref = other.observeAsState()
        return this == pref.value
    }

    @Composable
    infix fun <V : Any> PreferenceData<V>.isNotEqualTo(other: PreferenceData<V>): Boolean {
        return !(this isEqualTo other)
    }

    @Composable
    infix fun <V : Any> PreferenceData<V>.isNotEqualTo(other: V): Boolean {
        return !(this isEqualTo other)
    }

    @Composable
    infix fun <V : Any> V.isNotEqualTo(other: PreferenceData<V>): Boolean {
        return !(this isEqualTo other)
    }
}
