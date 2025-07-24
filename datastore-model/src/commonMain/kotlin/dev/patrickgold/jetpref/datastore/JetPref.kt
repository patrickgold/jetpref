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

package dev.patrickgold.jetpref.datastore

/**
 * Global JetPref object bundling the global config, default values and model caching.
 *
 * @since 0.1.0
 */
object JetPref {
    object Defaults {
        const val SaveIntervalMs: Long = 1_000
        const val EncodeDefaultValues: Boolean = false
        val ErrorLogProcessor: (Throwable) -> Unit = {
            //Log.e("JetPref", it.message ?: "(no message provided)")
        }
    }

    internal const val DELIMITER = ";"

    internal var saveIntervalMs: Long = Defaults.SaveIntervalMs
    internal var encodeDefaultValues: Boolean = Defaults.EncodeDefaultValues
    internal var errorLogProcessor: (Throwable) -> Unit = Defaults.ErrorLogProcessor

    /**
     * Initialize the global JetPref config, which is applied to **all** datastore
     * models across the application.
     *
     * @param saveIntervalMs The interval in which the datastore will persist its state.
     *  Persistence will only be done if at least one preference data value has changed.
     *  Defaults to 1000 milliseconds.
     * @param encodeDefaultValues Specifies if default values should also be written to
     *  the datastore file. Defaults to false.
     * @param errorLogProcessor The error log processor which is responsible to handle
     *  errors. By default errors are logged with Android LogCat. You can either pass
     *  a processor which logs the error message to a custom logger or just pass an empty
     *  logger to suppress all error messages.
     *
     * @since 0.1.0
     */
    fun configure(
        saveIntervalMs: Long = Defaults.SaveIntervalMs,
        encodeDefaultValues: Boolean = Defaults.EncodeDefaultValues,
        errorLogProcessor: (Throwable) -> Unit = Defaults.ErrorLogProcessor,
    ) {
        this.saveIntervalMs = saveIntervalMs
        this.encodeDefaultValues = encodeDefaultValues
        this.errorLogProcessor = errorLogProcessor
    }
}
