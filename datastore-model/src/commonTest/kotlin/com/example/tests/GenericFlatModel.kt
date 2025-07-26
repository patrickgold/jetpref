package com.example.tests

import dev.patrickgold.jetpref.datastore.annotations.Preferences
import dev.patrickgold.jetpref.datastore.model.LocalTime
import dev.patrickgold.jetpref.datastore.model.PreferenceModel

/**
 * Re-usable simple flat model (no groups)
 */
@Preferences
abstract class GenericFlatModel : PreferenceModel() {
    enum class Theme {
        SYSTEM_DEFAULT,
        EYE_BURNER,
        NIGHT_OWL,
        PITCH_BLACK,
    }

    companion object {
        const val KEY_BOOLEAN = "boolean"
        const val KEY_DOUBLE = "double"
        const val KEY_FLOAT = "float"
        const val KEY_INT = "int"
        const val KEY_LONG = "long"
        const val KEY_STRING = "string"
        const val KEY_ENUM = "enum"
        const val KEY_LOCAL_TIME = "local_time"

        const val DEFAULT_BOOLEAN: Boolean = true
        const val DEFAULT_DOUBLE: Double = 42.8
        const val DEFAULT_FLOAT: Float = 31.7f
        const val DEFAULT_INT: Int = 13
        const val DEFAULT_LONG: Long = 17L
        const val DEFAULT_STRING: String = "hello_world_123"
        val DEFAULT_ENUM: Theme = Theme.NIGHT_OWL
        val DEFAULT_LOCAL_TIME: LocalTime = LocalTime(20, 0)
    }

    val prefBoolean = boolean(
        key = KEY_BOOLEAN,
        default = DEFAULT_BOOLEAN,
    )
    val prefDouble = double(
        key = KEY_DOUBLE,
        default = DEFAULT_DOUBLE,
    )
    val prefFloat = float(
        key = KEY_FLOAT,
        default = DEFAULT_FLOAT,
    )
    val prefInt = int(
        key = KEY_INT,
        default = DEFAULT_INT,
    )
    val prefLong = long(
        key = KEY_LONG,
        default = DEFAULT_LONG,
    )
    val prefString = string(
        key = KEY_STRING,
        default = DEFAULT_STRING,
    )
    val prefEnum = enum(
        key = KEY_ENUM,
        default = DEFAULT_ENUM,
    )
    val prefLocalTime = localTime(
        key = KEY_LOCAL_TIME,
        default = DEFAULT_LOCAL_TIME,
    )
}
