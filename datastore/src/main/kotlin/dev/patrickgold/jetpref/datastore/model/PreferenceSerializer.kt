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

interface PreferenceSerializer<V : Any> {
    fun serialize(value: V): String?

    fun deserialize(value: String): V?
}

internal object BooleanPreferenceSerializer : PreferenceSerializer<Boolean> {
    override fun serialize(value: Boolean): String = value.toString()

    override fun deserialize(value: String): Boolean? = value.toBooleanStrictOrNull()
}

internal object DoublePreferenceSerializer : PreferenceSerializer<Double> {
    override fun serialize(value: Double): String = value.toString()

    override fun deserialize(value: String): Double? = value.toDoubleOrNull()
}

internal object FloatPreferenceSerializer : PreferenceSerializer<Float> {
    override fun serialize(value: Float): String = value.toString()

    override fun deserialize(value: String): Float? = value.toFloatOrNull()
}

internal object IntPreferenceSerializer : PreferenceSerializer<Int> {
    override fun serialize(value: Int): String = value.toString(10)

    override fun deserialize(value: String): Int? = value.toIntOrNull(10)
}

internal object LongPreferenceSerializer : PreferenceSerializer<Long> {
    override fun serialize(value: Long): String = value.toString(10)

    override fun deserialize(value: String): Long? = value.toLongOrNull(10)
}

internal object StringPreferenceSerializer : PreferenceSerializer<String> {
    override fun serialize(value: String): String = value

    override fun deserialize(value: String): String = value
}
