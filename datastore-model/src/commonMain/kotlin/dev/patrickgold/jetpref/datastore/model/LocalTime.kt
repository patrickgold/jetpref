package dev.patrickgold.jetpref.datastore.model

import java.util.*
import kotlinx.serialization.Serializable

@Serializable
data class LocalTime(
    val hour: Int,
    val minute: Int,
) {
    init {
        require(hour in 0..23) { "hour must in [0..23] range" }
        require(minute in 0..59) { "minute must be in [0..59] range" }
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
        val minute = String.format(Locale.ROOT, "%02d", minute)
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
}
