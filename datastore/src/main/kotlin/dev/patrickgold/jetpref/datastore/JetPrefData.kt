package dev.patrickgold.jetpref.datastore

interface JetPrefData<V : Any> {
    val key: String

    var value: V

    val defaultValue: V
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
