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

import android.content.Context
import dev.patrickgold.jetpref.datastore.model.PreferenceModel
import dev.patrickgold.jetpref.datastore.model.Validator
import java.io.File
import java.io.FileNotFoundException

private class AndroidStorageProvider(
    context: Context,
    override val datastoreName: String,
) : JetPrefStorageProvider {
    private val datastoreDir: File = context.jetprefDatastoreDir
    private val datastoreFile = datastoreDir.jetprefDatastoreFile(datastoreName)
    private val tempDir: File = context.jetprefTempDir
    private val tempFile = tempDir.jetprefTempFile(datastoreName)

    init {
        Validator.validateFileName(datastoreName)
        datastoreDir.mkdirs()
        tempDir.mkdirs()
    }

    override fun load() = runCatchingCancellationAware {
        try {
            datastoreFile.readText()
        } catch (_: FileNotFoundException) {
            ""
        }
    }

    override fun persist(rawDatastoreContent: String) = runCatchingCancellationAware {
        tempFile.writeText(rawDatastoreContent)
        check(tempFile.renameTo(datastoreFile)) {
            "Failed to rename temp file to actual file name"
        }
    }

    companion object {
        const val JETPREF_DIR_NAME = "jetpref_datastore"
        const val JETPREF_FILE_EXT = "jetpref"
    }
}

suspend fun <T : PreferenceModel> JetPrefDataStore<T>.init(
    context: Context,
    datastoreName: String,
    shouldPersist: Boolean = true,
): Result<Unit> {
    val storageProvider = AndroidStorageProvider(context, datastoreName)
    return this.init(storageProvider, shouldPersist)
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
