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

/**
 * Data class holding a local unqualified time.
 *
 * @property hour The hour component. Must be in interval [0,23].
 * @property minute The minute component. Must be in interval [0,59].
 * @property second The second component. Must be in interval [0,59].
 * @property millisecond The millisecond component. Must be in interval [0,999].
 * @since 0.2.0
 */
data class LocalTime(
    val hour: Int,
    val minute: Int,
    val second: Int = 0,
    val millisecond: Int = 0,
) {
    init {
        require(hour in 0..23) { "hour must in [0..23] range" }
        require(minute in 0..59) { "minute must be in [0..59] range" }
        require(second in 0..59) { "second must be in [0..59] range" }
        require(millisecond in 0..999) { "millisecond must be in [0..999] range" }
    }

    val isAfternoon
        get() = hour >= 12

    private fun hourForDisplay(is24hour: Boolean): Int {
        return when {
            is24hour -> hour % 24
            hour % 12 == 0 -> 12
            isAfternoon -> hour - 12
            else -> hour
        }
    }

    fun stringRepresentation(is24hour: Boolean): String {
        val hour = hourForDisplay(is24hour)
        val minute = minute.toString().padStart(2, '0')
        return if (is24hour) {
            "$hour:$minute"
        } else {
            if (isAfternoon) {
                "$hour:$minute PM"
            } else {
                "$hour:$minute AM"
            }
        }
    }

    override fun toString(): String {
        val hh = hour.toString().padStart(2, '0')
        val mm = minute.toString().padStart(2, '0')
        val ss = second.toString().padStart(2, '0')
        val sss = millisecond.toString().padStart(3, '0')
        return "$hh:$mm:$ss.$sss"
    }

    companion object {
        private val PATTERN = "^(?<hh>2[0-3]|[0-1][0-9]):(?<mm>[0-5][0-9]):(?<ss>[0-5][0-9])[.](?<sss>[0-9]{3})$".toRegex()

        fun parse(str: String): Result<LocalTime> = runCatching {
            val match = PATTERN.matchEntire(str)!!
            LocalTime(
                hour = match.groups["hh"]!!.value.toInt(),
                minute = match.groups["mm"]!!.value.toInt(),
                second = match.groups["ss"]!!.value.toInt(),
                millisecond = match.groups["sss"]!!.value.toInt(),
            )
        }
    }
}

internal object LocalTimePreferenceSerializer : PreferenceSerializer<LocalTime> {
    override fun serialize(value: LocalTime): String? {
        return value.toString()
    }

    override fun deserialize(value: String): LocalTime? {
        return LocalTime.parse(value).getOrNull()
    }
}
