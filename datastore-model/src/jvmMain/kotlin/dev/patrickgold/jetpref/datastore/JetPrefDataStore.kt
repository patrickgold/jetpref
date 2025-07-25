package dev.patrickgold.jetpref.datastore

import dev.patrickgold.jetpref.datastore.model.PreferenceModel
import kotlin.reflect.KClass

@Suppress("unchecked_cast")
actual fun <T : PreferenceModel> jetprefDataStoreOf(kClass: KClass<T>): JetPrefDataStore<T> {
    val modelImplName = kClass.qualifiedName!! + "Impl"
    val modelImplClass = Class.forName(modelImplName)
    val modelImplInstance = modelImplClass.getDeclaredConstructor().newInstance() as T
    return JetPrefDataStore(modelImplInstance)
}

actual fun generateDataStoreId(): Long {
    return System.currentTimeMillis()
}
