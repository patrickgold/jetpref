/*
 * Copyright 2017 The Android Open Source Project
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

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import dev.patrickgold.jetpref.datastore.annotations.PreferenceKey
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.*

/**
 * Interface for an implementation of a preference data accessor for
 * type [V]. A preference data is the heart of all the jetpref logic
 * and allows to observe value changes. The default implementation and
 * behavior internally is very similar to Android's LiveData, but this
 * interface allows for any kind of custom implementation.
 *
 * @since 0.1.0
 */
interface PreferenceData<V : Any> {
    /**
     * The type of this preference, useful especially in the serialization
     * process. If the preference type attribute is invalid, this preference
     * data will not work correctly.
     *
     * @since 0.1.0
     */
    val type: PreferenceType

    /**
     * The serializer for this preference data, used by the serialization
     * process to persist the cached preferences onto the device storage.
     *
     * @since 0.1.0
     */
    val serializer: PreferenceSerializer<V>

    /**
     * The key of this value. Generally a key must only contain the following
     * characters and symbols:
     *  - `a-z`
     *  - `A-Z`
     *  - `0-9` (not at the start though)
     *  - `_`
     *  - `-` (not at the start or end though)
     *
     * @since 0.1.0
     */
    @PreferenceKey val key: String

    /**
     * The default value for this preference data. Is used if no valid persisted
     * value is existent or when resetting this preference data.
     *
     * @since 0.1.0
     */
    val default: V

    /**
     * Gets the cached value of this preference data.
     *
     * @return The value of this preference data or [default].
     *
     * @since 0.1.0
     */
    fun get(): V

    /**
     * Gets the cached value of this preference data.
     *
     * @return The value of this preference data or null.
     *
     * @since 0.1.0
     */
    fun getOrNull(): V?

    /**
     * Sets the value of this preference data.
     *
     * @param value The new value to set for this preference data.
     * @param requestSync If true the [PreferenceModel] is requested to
     *  persist the cached values to storage.
     *
     * @since 0.1.0
     */
    fun set(value: V, requestSync: Boolean = true)

    /**
     * Resets this preference data to [default].
     *
     * @param requestSync If true the [PreferenceModel] is requested to
     *  persist the cached values to storage.
     *
     * @since 0.1.0
     */
    fun reset(requestSync: Boolean = true)

    /**
     * Check if this preference data has any observers attached, regardless
     * of their active state.
     *
     * @return True if at least one observer is attached, false otherwise.
     *
     * @since 0.1.0
     */
    fun hasObservers(): Boolean

    /**
     * Observes this preference value for given [owner] lifecycle. The
     * observer will only be called if [owner] is active. If the owner
     * is destroyed, the observer will automatically be removed.
     *
     * @param owner The owner lifecycle of the observer to be added.
     * @param observer The observer which will receive new values.
     *
     * @since 0.1.0
     */
    fun observe(owner: LifecycleOwner, observer: PreferenceObserver<V>)

    /**
     * Observes this preference data forever. The observer will always
     * be called. To remove the observer, call [removeObserver].
     *
     * @param observer The observer which will receive new values.
     *
     * @since 0.1.0
     */
    fun observeForever(observer: PreferenceObserver<V>)

    /**
     * Removes a given [observer] from the observer list. Does nothing
     * if the observer is not attached to this preference data.
     *
     * @param observer The observer to remove.
     *
     * @since 0.1.0
     */
    fun removeObserver(observer: PreferenceObserver<V>)

    /**
     * Removes all observers from this preference data which belong to [owner].
     *
     * @since 0.1.0
     */
    fun removeObservers(owner: LifecycleOwner)
}

/**
 * Implementation partially based on:
 *  https://android.googlesource.com/platform/frameworks/support/+/refs/heads/androidx-main/lifecycle/lifecycle-livedata-core/src/main/java/androidx/lifecycle/LiveData.java
 */
internal abstract class AbstractPreferenceData<V : Any>(private val model: PreferenceModel) : PreferenceData<V> {
    private val cacheGuard = Mutex()
    private var cachedValue: V? = null
    private var cachedValueVersion: Int = 0

    private val observerGuard = Mutex()
    private val observers: WeakHashMap<PreferenceObserver<V>, ObserverWrapper> = WeakHashMap()

    private val dispatchChannel: Channel<ObserverWrapper?> = Channel(Channel.UNLIMITED)
    private val dispatcher = model.mainScope.launch {
        for (wrapper in dispatchChannel) {
            if (wrapper != null) {
                considerNotify(wrapper)
            } else {
                val observerIterator = observers.iterator()
                while (observerIterator.hasNext()) {
                    considerNotify(observerIterator.next().value)
                }
            }
        }
    }

    final override fun get(): V = cachedValue ?: default

    final override fun getOrNull(): V? = cachedValue

    final override fun set(value: V, requestSync: Boolean) {
        model.mainScope.launch {
            cacheGuard.withLock {
                if (cachedValue != value) {
                    cachedValue = value
                    cachedValueVersion++
                    if (requestSync) {
                        model.notifyValueChanged()
                    }
                    dispatchValue(null)
                }
            }
        }
    }

    final override fun reset(requestSync: Boolean) {
        model.mainScope.launch {
            cacheGuard.withLock {
                if (cachedValue != null) {
                    cachedValue = null
                    cachedValueVersion++
                    if (requestSync) {
                        model.notifyValueChanged()
                    }
                    dispatchValue(null)
                }
            }
        }
    }

    final override fun hasObservers(): Boolean = observers.isNotEmpty()

    final override fun observe(owner: LifecycleOwner, observer: PreferenceObserver<V>) {
        if (owner.lifecycle.currentState == Lifecycle.State.DESTROYED) {
            return // ignore
        }
        runBlocking {
            observerGuard.withLock {
                val wrapper = LifecycleBoundObserverWrapper(owner, observer)
                val existing = observers.putIfAbsentAnyApi(observer, wrapper)
                when {
                    existing != null && !existing.isAttachedTo(owner) -> {
                        throw IllegalArgumentException("Cannot add the same observer"
                            + " with different lifecycles")
                    }
                    existing != null -> { }
                    else -> { owner.lifecycle.addObserver(wrapper) }
                }
            }
        }
    }

    final override fun observeForever(observer: PreferenceObserver<V>) {
        runBlocking {
            observerGuard.withLock {
                val wrapper = ObserverWrapper(observer)
                val existing = observers.putIfAbsentAnyApi(observer, wrapper)
                when {
                    existing is LifecycleBoundObserverWrapper -> {
                        throw IllegalArgumentException("Cannot add the same observer"
                            + " with different lifecycles")
                    }
                    existing != null -> { }
                    else -> { wrapper.activeStateChanged(true) }
                }
            }
        }
    }

    final override fun removeObserver(observer: PreferenceObserver<V>) {
        runBlocking {
            observerGuard.withLock {
                val removed = observers.remove(observer)
                if (removed != null) {
                    removed.detachObserver()
                    removed.activeStateChanged(false)
                }
            }
        }
    }

    override fun removeObservers(owner: LifecycleOwner) {
        runBlocking {
            observerGuard.withLock {
                for ((observer, wrapper) in observers) {
                    if (wrapper.isAttachedTo(owner)) {
                        removeObserver(observer)
                    }
                }
            }
        }
    }

    private fun considerNotify(observer: ObserverWrapper) {
        if (!observer.active) return
        if (!observer.shouldBeActive()) {
            observer.activeStateChanged(false)
            return
        }
        if (observer.lastVersion >= cachedValueVersion) {
            return
        }
        observer.lastVersion = cachedValueVersion
        observer.observer.onChanged(cachedValue ?: default)
    }

    private fun dispatchValue(initiator: ObserverWrapper?) {
        dispatchChannel.trySend(initiator)
    }

    private fun <K, V> WeakHashMap<K, V>.putIfAbsentAnyApi(key: K, value: V): V? {
        var v = get(key)
        if (v == null) {
            v = put(key, value)
        }
        return v
    }

    private open inner class ObserverWrapper(val observer: PreferenceObserver<V>) {
        var active: Boolean = false
        var lastVersion: Int = 0

        open fun shouldBeActive(): Boolean = true

        open fun isAttachedTo(owner: LifecycleOwner): Boolean = true

        open fun detachObserver() { }

        fun activeStateChanged(newActive: Boolean) {
            if (newActive == active) {
                return
            }
            active = newActive
            if (active) {
                dispatchValue(this)
            }
        }
    }

    private inner class LifecycleBoundObserverWrapper(
        private val owner: LifecycleOwner,
        observer: PreferenceObserver<V>,
    ) : ObserverWrapper(observer), LifecycleEventObserver {

        override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
            var currentState = owner.lifecycle.currentState
            if (currentState == Lifecycle.State.DESTROYED) {
                removeObserver(observer)
                return
            }
            var prevState: Lifecycle.State? = null
            while (prevState != currentState) {
                prevState = currentState
                activeStateChanged(shouldBeActive())
                currentState = owner.lifecycle.currentState
            }
        }

        override fun shouldBeActive(): Boolean {
            return owner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)
        }

        override fun isAttachedTo(owner: LifecycleOwner): Boolean {
            return this.owner == owner
        }

        override fun detachObserver() {
            owner.lifecycle.removeObserver(this)
        }
    }
}

internal class BooleanPreferenceData(
    model: PreferenceModel,
    override val key: String,
    override val default: Boolean,
) : AbstractPreferenceData<Boolean>(model) {

    init {
        Validator.validateKey(key)
    }

    override val type: PreferenceType = PreferenceType.boolean()

    override val serializer: PreferenceSerializer<Boolean> = BooleanPreferenceSerializer
}

internal class DoublePreferenceData(
    model: PreferenceModel,
    override val key: String,
    override val default: Double,
) : AbstractPreferenceData<Double>(model) {

    init {
        Validator.validateKey(key)
    }

    override val type: PreferenceType = PreferenceType.double()

    override val serializer: PreferenceSerializer<Double> = DoublePreferenceSerializer
}

internal class FloatPreferenceData(
    model: PreferenceModel,
    override val key: String,
    override val default: Float,
) : AbstractPreferenceData<Float>(model) {

    init {
        Validator.validateKey(key)
    }

    override val type: PreferenceType = PreferenceType.float()

    override val serializer: PreferenceSerializer<Float> = FloatPreferenceSerializer
}

internal class IntPreferenceData(
    model: PreferenceModel,
    override val key: String,
    override val default: Int,
) : AbstractPreferenceData<Int>(model) {

    init {
        Validator.validateKey(key)
    }

    override val type: PreferenceType = PreferenceType.integer()

    override val serializer: PreferenceSerializer<Int> = IntPreferenceSerializer
}

internal class LongPreferenceData(
    model: PreferenceModel,
    override val key: String,
    override val default: Long,
) : AbstractPreferenceData<Long>(model) {

    init {
        Validator.validateKey(key)
    }

    override val type: PreferenceType = PreferenceType.long()

    override val serializer: PreferenceSerializer<Long> = LongPreferenceSerializer
}

internal class StringPreferenceData(
    model: PreferenceModel,
    override val key: String,
    override val default: String,
) : AbstractPreferenceData<String>(model) {

    init {
        Validator.validateKey(key)
    }

    override val type: PreferenceType = PreferenceType.string()

    override val serializer: PreferenceSerializer<String> = StringPreferenceSerializer
}

internal class EnumPreferenceData<V : Enum<V>>(
    model: PreferenceModel,
    override val key: String,
    override val default: V,
    private val stringToEnum: (String) -> V?,
) : AbstractPreferenceData<V>(model) {

    init {
        Validator.validateKey(key)
    }

    override val type: PreferenceType = PreferenceType.string()

    override val serializer: PreferenceSerializer<V> = object : PreferenceSerializer<V> {
        override fun serialize(value: V): String {
            return value.toString().lowercase()
        }

        override fun deserialize(value: String): V? {
            return stringToEnum(value.uppercase())
        }
    }
}

internal class CustomPreferenceData<V : Any>(
    model: PreferenceModel,
    override val key: String,
    override val default: V,
    override val serializer: PreferenceSerializer<V>,
) : AbstractPreferenceData<V>(model) {

    init {
        Validator.validateKey(key)
    }

    override val type: PreferenceType = PreferenceType.string()
}
