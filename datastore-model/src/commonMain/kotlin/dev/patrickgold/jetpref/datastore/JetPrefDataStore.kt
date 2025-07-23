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

import dev.patrickgold.jetpref.datastore.model.PreferenceData
import dev.patrickgold.jetpref.datastore.model.PreferenceModel
import dev.patrickgold.jetpref.datastore.model.PreferenceType
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

class JetPrefDataStore<T : PreferenceModel>(
    private val model: T,
    private val entries: List<PreferenceData<*>>,
): ReadOnlyProperty<Any?, T> {
    private var persistenceHandler: PersistenceHandler? = null
    private var readOnly: Boolean = false

    suspend fun init(persistenceHandler: PersistenceHandler, readOnly: Boolean = false) {
        // TODO proper impl
        model.initialize(persistenceHandler, readOnly)
    }

    // Delegate for getting the model with Kotlin's by syntax
    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return model
    }

    private data class DataValue(
        val type: PreferenceType,
        val rawValue: String,
    )

    companion object {
        /**
         * Creates a preference model store and returns it.
         *
         * @param kClass The class of the preference model to create.
         *
         * @since 0.3.0
         */
        @Suppress("unchecked_cast")
        fun <T : PreferenceModel> newInstanceOf(
            kClass: KClass<T>,
        ): JetPrefDataStore<T> {
            val modelImplName = kClass.qualifiedName!! + "Impl"
            val modelImplClass = Class.forName(modelImplName)
            val modelImplInstance = modelImplClass.getDeclaredConstructor().newInstance() as T
            return JetPrefDataStore(modelImplInstance, emptyList()) // TODO
        }
    }
}
