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
import dev.patrickgold.jetpref.datastore.runtime.DataStore
import dev.patrickgold.jetpref.datastore.runtime.PreferenceModelNotFoundException
import kotlin.reflect.KClass

/**
 * Creates a new datastore instance for given [modelClass] and returns it.
 *
 * @param modelClass The class of the preference model to create.
 * @throws PreferenceModelNotFoundException If the model referenced by [modelClass]
 *  does not exist.
 * @since 0.3.0
 */
@Throws(PreferenceModelNotFoundException::class)
expect fun <T : PreferenceModel> jetprefDataStoreOf(
    modelClass: KClass<T>,
): DataStore<T>
