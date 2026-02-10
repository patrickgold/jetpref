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

sealed interface StringDescriptor {
    fun interface ZeroArg : StringDescriptor {
        @Composable
        fun resolve(): String
    }

    fun interface OneArg<V1> : StringDescriptor {
        @Composable
        fun resolve(arg1: V1): String
    }

    fun interface TwoArg<V1, V2> : StringDescriptor {
        @Composable
        fun resolve(arg1: V1, arg2: V2): String
    }
}

fun describedBy(descriptor: StringDescriptor.ZeroArg) = descriptor

fun <V1> describedBy(descriptor: StringDescriptor.OneArg<V1>) = descriptor

fun <V1, V2> describedBy(descriptor: StringDescriptor.TwoArg<V1, V2>) = descriptor
