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

import android.content.Context
import android.util.Log
import dev.patrickgold.jetpref.datastore.model.PreferenceModel
import java.io.File
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

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
            Log.e("JetPref", it.message ?: "(no message provided)")
        }
    }

    internal const val DELIMITER = ";"

    const val JETPREF_DIR_NAME = "jetpref_datastore"
    const val JETPREF_FILE_EXT = "jetpref"

    private val preferenceModelCache: HashMap<KClass<*>, CachedPreferenceModel<*>> = hashMapOf()

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

    /**
     * Gets or creates a preference model and returns a [CachedPreferenceModel] wrapper.
     * This method runs synchronized on the model cache and may block your thread for a
     * short period of time.
     *
     * @param kClass The class of the preference model to get, is is used as a key for the
     *  underlying cache.
     * @param factory A factory function to create a new instance of the model in case it
     *  does not exist yet.
     *
     * @since 0.1.0
     */
    @Suppress("unchecked_cast")
    fun <T : PreferenceModel> getOrCreatePreferenceModel(
        kClass: KClass<T>,
        factory: () -> T,
    ): CachedPreferenceModel<T> = synchronized(preferenceModelCache) {
        return preferenceModelCache.getOrPut(kClass) {
            CachedPreferenceModel(factory())
        } as CachedPreferenceModel<T>
    }
}

/**
 * Cached preference model wrapper. Allows to act as a delegate.
 *
 * @since 0.1.0
 */
class CachedPreferenceModel<T : PreferenceModel>(
    private val preferenceModel: T,
) : ReadOnlyProperty<Any?, T> {

    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return preferenceModel
    }
}

/**
 * Returns the absolute path to the directory on the filesystem where preference datastore
 * files are created and used by JetPref are stored. Note that this path may change over time,
 * so only relative paths should be stored.
 *
 * @return The path of the directory holding JetPref datastore files.
 *
 * @since 0.1.0
 */
val Context.jetprefDatastoreDir: File
    get() = File(this.filesDir.parent, JetPref.JETPREF_DIR_NAME)

/**
 * Returns the absolute path to the directory on the filesystem where temporary preference
 * datastore files of JetPref are stored. Note that files in this path can be incomplete and
 * should at no point be preserved in automatic backups.
 *
 * @return The path of the directory holding temporary JetPref datastore files.
 *
 * @since 0.1.0
 */
val Context.jetprefTempDir: File
    get() = File(this.cacheDir, JetPref.JETPREF_DIR_NAME)

internal fun File.jetprefDatastoreFile(name: String): File {
    return File(this, "$name.${JetPref.JETPREF_FILE_EXT}")
}

internal fun File.jetprefTempFile(name: String): File {
    return File(this, "$name.${JetPref.JETPREF_FILE_EXT}.tmp")
}

internal inline fun runSafely(block: () -> Unit) {
    try {
        block()
    } catch (e: Throwable) {
        JetPref.errorLogProcessor(e)
    }
}
