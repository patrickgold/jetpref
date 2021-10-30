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
    private const val JETPREF_DIR_NAME = "jetpref_datastore"
    private const val JETPREF_EXT = "jetpref"

    internal const val DELIMITER = ";"

    private const val DEFAULT_SAVE_INTERVAL_MS: Long = 5_000

    internal var saveIntervalMs: Long = DEFAULT_SAVE_INTERVAL_MS

    private val preferenceModelCache: MutableList<CachedPreferenceModel<*>> = mutableListOf()

    fun init(saveIntervalMs: Long = DEFAULT_SAVE_INTERVAL_MS) {
        this.saveIntervalMs = saveIntervalMs
    }

    internal fun setupJetPrefDir(context: Context): Boolean {
        val dir = context.jetPrefDir
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
        val path = context.jetPrefPath(name)
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
        val path = context.jetPrefPath(name)
        try {
            path.bufferedWriter().use { block(it) }
        } catch (e: SecurityException) {
            android.util.Log.e("JetPref", "Cannot write to $path! Reason: ${e.message}")
        } catch (e: FileNotFoundException) {
            android.util.Log.e("JetPref", "Cannot write to $path! Reason: ${e.message}")
        }
    }

    private val Context.jetPrefDir: File
        get() = File(this.filesDir.parent, JETPREF_DIR_NAME)

    private fun Context.jetPrefPath(name: String): File {
        return File(this.jetPrefDir, "$name.$JETPREF_EXT")
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

