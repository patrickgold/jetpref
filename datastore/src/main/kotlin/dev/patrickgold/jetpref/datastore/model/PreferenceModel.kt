package dev.patrickgold.jetpref.datastore.model

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.atomic.AtomicInteger

abstract class PreferenceModel {
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private val registryGuard = Mutex()
    private val registry: MutableList<PreferenceData<*>> = mutableListOf()

    private val numChangedValueNotifies: AtomicInteger = AtomicInteger(0)

    internal fun notifyValueChanged() {
        numChangedValueNotifies.incrementAndGet()
    }

    private fun registryAdd(prefData: PreferenceData<*>) = scope.launch {
        registryGuard.withLock { registry.add(prefData) }
    }

    protected fun boolean(
        key: String,
        default: Boolean,
    ): BooleanPreferenceData {
        val prefData = BooleanPreferenceData(this, key, default)
        registryAdd(prefData)
        return prefData
    }

    protected fun <V : Any> custom(
        key: String,
        default: V,
        serializer: PreferenceSerializer<V>,
    ): CustomPreferenceData<V> {
        val prefData = CustomPreferenceData(this, key, default, serializer)
        registryAdd(prefData)
        return prefData
    }
}


class AppPrefs : PreferenceModel() {
    val showLight = boolean(key = "show_light", default = true)
}
