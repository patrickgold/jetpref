package dev.patrickgold.jetpref.datastore.model

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

interface PreferenceData<V : Any> {
    val type: PreferenceType

    val serializer: PreferenceSerializer<V>

    val key: String

    val default: V

    suspend fun get(): V

    suspend fun set(value: V)

    suspend fun observe(observer: PreferenceObserver<V>)

    suspend fun stopObserving(observer: PreferenceObserver<V>)
}

internal abstract class AbstractPreferenceData<V : Any>(private val model: PreferenceModel) : PreferenceData<V> {
    private val cacheGuard = Mutex()
    private var cachedValue: V? = null

    private val observerGuard = Mutex()
    private val observers: MutableList<PreferenceObserver<V>> = mutableListOf()

    final override suspend fun get(): V = cacheGuard.withLock { cachedValue ?: default }

    final override suspend fun set(value: V) = cacheGuard.withLock {
        if (cachedValue != value) {
            cachedValue = value
            model.notifyValueChanged()
        }
    }

    internal suspend fun setSilent(value: V) = cacheGuard.withLock {
        cachedValue = value
    }

    final override suspend fun observe(observer: PreferenceObserver<V>) = observerGuard.withLock {
        //
    }

    final override suspend fun stopObserving(observer: PreferenceObserver<V>) {
        //
    }
}

internal class BooleanPreferenceData(
    model: PreferenceModel,
    override val key: String,
    override val default: Boolean,
) : AbstractPreferenceData<Boolean>(model) {

    override val type: PreferenceType = PreferenceTypes.BOOLEAN

    override val serializer: PreferenceSerializer<Boolean> = BooleanPreferenceSerializer
}

internal class CustomPreferenceData<V : Any>(
    model: PreferenceModel,
    override val key: String,
    override val default: V,
    override val serializer: PreferenceSerializer<V>,
) : AbstractPreferenceData<V>(model) {

    override val type: PreferenceType = PreferenceTypes.STRING
}
