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

@JvmInline
value class PreferenceType private constructor(val id: String) {
    companion object {
        private const val BOOLEAN: String =     "b"
        private const val DOUBLE: String =      "d"
        private const val FLOAT: String =       "f"
        private const val INTEGER: String =     "i"
        private const val LONG: String =        "l"
        private const val STRING: String =      "s"

        fun boolean() = PreferenceType(BOOLEAN)

        fun double() = PreferenceType(DOUBLE)

        fun float() = PreferenceType(FLOAT)

        fun integer() = PreferenceType(INTEGER)

        fun long() = PreferenceType(LONG)

        fun string() = PreferenceType(STRING)

        fun from(id: String) = PreferenceType(id)
    }

    fun isValid() = id.isNotBlank()

    fun isInvalid() = !isValid()

    fun isPrimitive() = id.first().isLowerCase()

    fun isBoolean() = id == BOOLEAN

    fun isDouble() = id == DOUBLE

    fun isFloat() = id == FLOAT

    fun isInteger() = id == INTEGER

    fun isLong() = id == LONG

    fun isString() = id == STRING
}
