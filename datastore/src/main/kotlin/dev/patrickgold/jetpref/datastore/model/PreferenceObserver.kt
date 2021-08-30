package dev.patrickgold.jetpref.datastore.model

interface PreferenceObserver<V : Any> {
    fun onInterceptChange(oldValue: V, newValue: V): Boolean = false

    fun onChanged(oldValue: V, newValue: V)
}
