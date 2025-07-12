package dev.patrickgold.jetpref.datastore.model

import android.text.format.DateFormat.is24HourFormat
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
}
