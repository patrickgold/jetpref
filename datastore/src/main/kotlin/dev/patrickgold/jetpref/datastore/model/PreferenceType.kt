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

typealias PreferenceType = String

internal object PreferenceTypes {
    const val BOOLEAN: PreferenceType =     "b"
    const val DOUBLE: PreferenceType =      "d"
    const val FLOAT: PreferenceType =       "f"
    const val INTEGER: PreferenceType =     "i"
    const val LONG: PreferenceType =        "l"
    const val STRING: PreferenceType =      "s"
}

fun PreferenceType.isPrimitive() = when (this) {
    PreferenceTypes.BOOLEAN,
    PreferenceTypes.DOUBLE,
    PreferenceTypes.FLOAT,
    PreferenceTypes.INTEGER,
    PreferenceTypes.LONG,
    PreferenceTypes.STRING -> true
    else -> false
}
