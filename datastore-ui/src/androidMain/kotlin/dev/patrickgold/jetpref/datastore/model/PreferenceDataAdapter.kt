/*
 * Copyright 2020 The Android Open Source Project
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

/**
 * Original source:
 *  https://android.googlesource.com/platform/frameworks/support/+/refs/heads/androidx-main/compose/runtime/runtime-livedata/src/main/java/androidx/compose/runtime/livedata/LiveDataAdapter.kt
 *
 * Adapted so it works for the custom PreferenceData instead of LiveData.
 */

package dev.patrickgold.jetpref.datastore.model

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.LocalLifecycleOwner

@Composable
fun <V : Any> PreferenceData<V>.observeAsState(): State<V> = observeAsState(get())

@Composable
fun <V : Any> PreferenceData<V>.observeAsState(initial: V): State<V> {
    val lifecycleOwner = LocalLifecycleOwner.current
    val state = remember(key) { mutableStateOf(initial) }
    // TODO fix state again!!!!!!!!!!!!!!!!
//    DisposableEffect(this, lifecycleOwner) {
//        val observer = PreferenceObserver<V> { newValue -> state.value = newValue }
//        observe(lifecycleOwner, observer)
//        onDispose { removeObserver(observer) }
//    }
    return state
}
