package dev.patrickgold.jetpref.datastore.model

interface PreferenceSerializer<V : Any> {
    fun serialize(v: V): String?

    fun deserialize(v: String): V?
}

internal object BooleanPreferenceSerializer : PreferenceSerializer<Boolean> {
    override fun serialize(v: Boolean): String = v.toString()

    override fun deserialize(v: String): Boolean? = v.toBooleanStrictOrNull()
}

internal object DoublePreferenceSerializer : PreferenceSerializer<Double> {
    override fun serialize(v: Double): String = v.toString()

    override fun deserialize(v: String): Double? = v.toDoubleOrNull()
}

internal object FloatPreferenceSerializer : PreferenceSerializer<Float> {
    override fun serialize(v: Float): String = v.toString()

    override fun deserialize(v: String): Float? = v.toFloatOrNull()
}

internal object IntPreferenceSerializer : PreferenceSerializer<Int> {
    override fun serialize(v: Int): String = v.toString(10)

    override fun deserialize(v: String): Int? = v.toIntOrNull(10)
}

internal object LongPreferenceSerializer : PreferenceSerializer<Long> {
    override fun serialize(v: Long): String = v.toString(10)

    override fun deserialize(v: String): Long? = v.toLongOrNull(10)
}

internal object StringPreferenceSerializer : PreferenceSerializer<String> {
    override fun serialize(v: String): String = v

    override fun deserialize(v: String): String = v
}
