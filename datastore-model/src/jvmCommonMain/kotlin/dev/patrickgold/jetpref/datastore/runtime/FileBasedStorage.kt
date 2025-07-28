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

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Implements a file-based datastore storage for JVM-backed applications.
 *
 * @param path The path to the datastore file.
 */
class FileBasedStorage(path: String) : DataStoreReader, DataStoreWriter {
    private val datastoreFile = File(path)
    private val tempFile = File("${path}.tmp")

    override suspend fun read(): String {
        return withContext(Dispatchers.IO) {
            datastoreFile.readText()
        }
    }

    override suspend fun write(content: String) {
        withContext(Dispatchers.IO) {
            tempFile.parentFile?.mkdirs()
            tempFile.writeText(content)
            check(tempFile.renameTo(datastoreFile)) {
                "Failed to rename temp file to actual file name"
            }
        }
    }
}
