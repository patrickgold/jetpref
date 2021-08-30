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
