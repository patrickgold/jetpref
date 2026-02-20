package dev.patrickgold.jetpref.datastore.component

import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.concurrent.atomics.incrementAndFetch

@OptIn(ExperimentalAtomicApi::class)
object PreferenceComponentId {
    private val generator = AtomicInt(0)

    fun next(): Int {
        return generator.incrementAndFetch()
    }
}
