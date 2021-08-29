package dev.patrickgold.jetpref.datastore

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

interface JetPrefData<V : Any> : ReadWriteProperty<Any?, V> {
    val key: String

    var value: V

    val defaultValue: V

    override fun getValue(thisRef: Any?, property: KProperty<*>): V = this.value

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: V) {
        this.value = value
    }
}

fun <T : Any> singleton(generator: () -> T) = SingletonImpl(generator)

interface Singleton<T : Any> {
    fun default(): T
}

class SingletonImpl<T : Any>(private val generator: () -> T) : Singleton<T> {
    private var instance: T? = null

    @Synchronized
    override fun default(): T {
        val cachedInstance = instance
        return if (cachedInstance != null) {
            cachedInstance
        } else {
            val newInstance = generator()
            instance = newInstance
            newInstance
        }
    }
}
