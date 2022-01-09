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
import dev.patrickgold.jetpref.datastore.model.PreferenceModel
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.FileNotFoundException
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

object JetPrefManager {
    private const val DEFAULT_SAVE_INTERVAL_MS: Long = 5_000
    internal const val DELIMITER = ";"

    const val JETPREF_DIR_NAME = "jetpref_datastore"
    const val JETPREF_FILE_EXT = "jetpref"

    private val preferenceModelCache: MutableList<CachedPreferenceModel<*>> = mutableListOf()
    internal var saveIntervalMs: Long = DEFAULT_SAVE_INTERVAL_MS

    fun init(saveIntervalMs: Long = DEFAULT_SAVE_INTERVAL_MS) {
        this.saveIntervalMs = saveIntervalMs
    }

    internal fun setupJetPrefDir(context: Context): Boolean {
        val dir = context.jetprefDatastoreDir
        return try {
            dir.mkdirs()
            true
        } catch (e: SecurityException) {
            android.util.Log.e("JetPref", "Cannot initialize datastore directory at '$dir'! Reason: ${e.message}")
            false
        } catch (e: FileNotFoundException) {
            android.util.Log.e("JetPref", "Cannot initialize datastore directory at '$dir'! Reason: ${e.message}")
            false
        }
    }

    internal inline fun loadPrefFile(context: Context, name: String, block: (BufferedReader) -> Unit) {
        if (!setupJetPrefDir(context)) return
        val path = context.jetprefPath(name)
        try {
            path.bufferedReader().use { block(it) }
        } catch (e: SecurityException) {
            android.util.Log.e("JetPref", "Cannot read from $path! Reason: ${e.message}")
        } catch (e: FileNotFoundException) {
            android.util.Log.e("JetPref", "Cannot read from $path! Reason: ${e.message}")
        }
    }

    internal inline fun savePrefFile(context: Context, name: String, block: (BufferedWriter) -> Unit) {
        if (!setupJetPrefDir(context)) return
        val path = context.jetprefPath(name)
        try {
            path.bufferedWriter().use { block(it) }
        } catch (e: SecurityException) {
            android.util.Log.e("JetPref", "Cannot write to $path! Reason: ${e.message}")
        } catch (e: FileNotFoundException) {
            android.util.Log.e("JetPref", "Cannot write to $path! Reason: ${e.message}")
        }
    }

    private fun Context.jetprefPath(name: String): File {
        return File(this.jetprefDatastoreDir, "$name.$JETPREF_FILE_EXT")
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
    return JetPrefManager.getOrCreatePreferenceModel(kClass, factory)
}

/**
 * Returns the absolute path to the directory on the filesystem where preference datastore
 * files are created and used by JetPref are stored. Note that this path may change over time,
 * so only relative paths should be stored.
 *
 * @return The path of the directory holding JetPref datastore files.
 */
val Context.jetprefDatastoreDir: File
    get() = File(this.filesDir.parent, JetPrefManager.JETPREF_DIR_NAME)

/**
 * Returns the absolute path to the directory on the filesystem where temporary preference
 * datastore files of JetPref are stored. Note that files in this path can be incomplete and
 * should at no point be preserved in automatic backups.
 *
 * @return The path of the directory holding temporary JetPref datastore files.
 */
val Context.jetprefTempDir: File
    get() = File(this.cacheDir, JetPrefManager.JETPREF_DIR_NAME)

