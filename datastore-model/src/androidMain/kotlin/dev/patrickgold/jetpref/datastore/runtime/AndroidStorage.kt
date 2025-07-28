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

package dev.patrickgold.jetpref.datastore.runtime

import android.content.Context
import dev.patrickgold.jetpref.datastore.model.PreferenceModel
import dev.patrickgold.jetpref.datastore.model.Validator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

private class AndroidStorageProvider(
    context: Context,
    datastoreName: String,
) : DataStoreReader, DataStoreWriter {
    private val datastoreDir: File = context.jetprefDatastoreDir
    private val datastoreFile = datastoreDir.jetprefDatastoreFile(datastoreName)
    private val tempDir: File = context.jetprefTempDir
    private val tempFile = tempDir.jetprefTempFile(datastoreName)

    init {
        Validator.validateFileName(datastoreName)
        datastoreDir.mkdirs()
        tempDir.mkdirs()
    }

    override suspend fun read(): String {
        return withContext(Dispatchers.IO) {
            datastoreFile.readText()
        }
    }

    override suspend fun write(content: String) {
        withContext(Dispatchers.IO) {
            tempFile.writeText(content)
        }
        check(tempFile.renameTo(datastoreFile)) {
            "Failed to rename temp file to actual file name"
        }
    }

    companion object {
        const val JETPREF_DIR_NAME = "jetpref_datastore"
        const val JETPREF_FILE_EXT = "jetpref"
    }
}

/**
 * Initialize the datastore with given [context] and [datastoreName]. The name should be constant, as it
 * is used for loading and persisting the datastore file from the same location again.
 *
 * Typically, this method is called once in the `Application.onCreate()` method. If this method is never
 * called, the model behaves as if it was an in-memory-only model.
 *
 * This method can be called multiple times, this can be useful in context switch scenarios (e.g. direct
 * boot to user unlocked switch). In this case, ALL existing runtime data gets overridden, regardless of
 * the success or failure of this request.
 *
 * @param context The Android context, typically the application context. Caution: the datastore file location
 *  is derived from given context. For work profiles or direct-boot contexts the locations might differ.
 * @param datastoreName The name of the datastore name. Should be constant.
 * @param shouldPersist If live changes to the model's values should be persisted back to the datastore file
 *  on-disk. Defaults to true.
 * @return A result object. When this method returns, the model is guaranteed to have been initialized,
 *  either with the values, or with default values for failure.
 *
 * @since 0.3.0
 */
suspend fun <T : PreferenceModel> DataStore<T>.initAndroid(
    context: Context,
    datastoreName: String,
    shouldPersist: Boolean = true,
): Result<Unit> {
    val storageProvider = AndroidStorageProvider(context, datastoreName)
    return this.init(
        loadStrategy = LoadStrategy.UseReader(storageProvider),
        persistStrategy = if (shouldPersist) {
            PersistStrategy.UseWriter(storageProvider)
        } else {
            PersistStrategy.Disabled
        },
    )
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
    get() = File(this.filesDir.parent, AndroidStorageProvider.JETPREF_DIR_NAME)

private fun File.jetprefDatastoreFile(name: String): File {
    return File(this, "$name.${AndroidStorageProvider.JETPREF_FILE_EXT}")
}

private val Context.jetprefTempDir: File
    get() = File(this.jetprefDatastoreDir, "temp")

private fun File.jetprefTempFile(name: String): File {
    return File(this, "$name.${AndroidStorageProvider.JETPREF_FILE_EXT}.tmp")
}
