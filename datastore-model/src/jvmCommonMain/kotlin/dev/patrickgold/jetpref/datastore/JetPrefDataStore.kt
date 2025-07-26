/*
 * Copyright 2025 Patrick Goldinger
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

package dev.patrickgold.jetpref.datastore

import dev.patrickgold.jetpref.datastore.model.PreferenceModel
import kotlin.reflect.KClass

@Suppress("unchecked_cast")
@Throws(JetPrefModelNotFoundException::class)
actual fun <T : PreferenceModel> jetprefDataStoreOf(kClass: KClass<T>): JetPrefDataStore<T> {
    val modelImplInstance = try {
        val modelImplName = kClass.qualifiedName!! + "Impl"
        val modelImplClass = Class.forName(modelImplName)
        modelImplClass.getDeclaredConstructor().newInstance() as T
    } catch (e: Throwable) {
        throw JetPrefModelNotFoundException(kClass.qualifiedName.toString(), e)
    }
    return JetPrefDataStore(modelImplInstance)
}

actual fun generateDataStoreId(): Long {
    return System.currentTimeMillis()
}
