/*
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

/**
 * Interface for observers to implement when observing a preference.
 */
fun interface PreferenceObserver<V : Any> {
    /**
     * Called either directly after a new value is set or on lifecycle
     * re-entry, if a value has been set in the meantime.
     *
     * @param newValue The new value for this preference.
     */
    fun onChanged(newValue: V)
}
