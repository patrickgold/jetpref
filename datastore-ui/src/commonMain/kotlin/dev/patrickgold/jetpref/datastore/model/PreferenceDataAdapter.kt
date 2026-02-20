/*
 * Copyright 2025 Patrick Goldinger
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
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState

@Composable
@Deprecated(message = "Use collectAsState for flow like constructs", ReplaceWith("collectAsState()"))
fun <V : Any> PreferenceData<V>.observeAsState(): State<V> {
    return asFlow().collectAsState()
}

@Composable
fun <V : Any> PreferenceData<V>.collectAsState(): State<V> {
    return asFlow().collectAsState()
}

@Composable
@Deprecated(message = "Use collectAsState for flow like constructs", ReplaceWith("collectAsState(initial)"))
fun <V : Any> PreferenceData<V>.observeAsState(initial: V): State<V> {
    return asFlow().collectAsState(initial)
}

@Composable
fun <V : Any> PreferenceData<V>.collectAsState(initial: V): State<V> {
    return asFlow().collectAsState(initial)
}
