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

object JetPref {
    private const val DEFAULT_SAVE_INTERVAL_MS: Long = 5_000
    internal const val DELIMITER = ";"

    const val JETPREF_DIR_NAME = "jetpref_datastore"
    const val JETPREF_FILE_EXT = "jetpref"

    const val LOG_TAG = "JetPref"

    private val preferenceModelCache: MutableList<CachedPreferenceModel<*>> = mutableListOf()
    internal var saveIntervalMs: Long = DEFAULT_SAVE_INTERVAL_MS

    fun init(saveIntervalMs: Long = DEFAULT_SAVE_INTERVAL_MS) {
        this.saveIntervalMs = saveIntervalMs
    }

    @Suppress("unchecked_cast")
    fun <T : PreferenceModel> getOrCreatePreferenceModel(
        kClass: KClass<T>,
        factory: () -> T
    ): CachedPreferenceModel<T> = synchronized(preferenceModelCache) {
        val cachedEntry = preferenceModelCache.find { it.kClass == kClass }
        if (cachedEntry != null) {
            return cachedEntry as CachedPreferenceModel<T>
        }
        val newModel = factory()
        val newCacheEntry = CachedPreferenceModel(kClass, newModel)
        preferenceModelCache.add(newCacheEntry)
        return@synchronized newCacheEntry
    }
}

data class CachedPreferenceModel<T : PreferenceModel>(
    val kClass: KClass<T>,
    val preferenceModel: T
) : ReadOnlyProperty<Any?, T> {

    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return preferenceModel
    }
}

fun <T : PreferenceModel> preferenceModel(kClass: KClass<T>, factory: () -> T): CachedPreferenceModel<T> {
    return JetPref.getOrCreatePreferenceModel(kClass, factory)
}

/**
 * Returns the absolute path to the directory on the filesystem where preference datastore
 * files are created and used by JetPref are stored. Note that this path may change over time,
 * so only relative paths should be stored.
 *
 * @return The path of the directory holding JetPref datastore files.
 */
val Context.jetprefDatastoreDir: File
    get() = File(this.filesDir.parent, JetPref.JETPREF_DIR_NAME)

/**
 * Returns the absolute path to the directory on the filesystem where temporary preference
 * datastore files of JetPref are stored. Note that files in this path can be incomplete and
 * should at no point be preserved in automatic backups.
 *
 * @return The path of the directory holding temporary JetPref datastore files.
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
        Log.e(JetPref.LOG_TAG, e.localizedMessage ?: "(no message provided)")
    }
}
