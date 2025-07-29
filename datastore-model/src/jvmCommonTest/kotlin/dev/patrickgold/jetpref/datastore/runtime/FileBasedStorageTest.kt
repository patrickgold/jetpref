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

import com.example.tests.assertIsSuccess
import com.example.tests.assertIsFailure
import dev.patrickgold.jetpref.datastore.annotations.Preferences
import dev.patrickgold.jetpref.datastore.jetprefDataStoreOf
import dev.patrickgold.jetpref.datastore.model.PreferenceModel
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.pathString
import kotlin.io.path.readText
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertEquals

@Preferences
abstract class ModelForFileBasedStorage : PreferenceModel() {
    val integer = int(
        key = "integer",
        default = 0,
    )
    val string = string(
        key = "string",
        default = "",
    )
}

@Suppress("JUnitMalformedDeclaration")
class FileBasedStorageTest {
    @Nested
    inner class Load {
        @Test
        fun `datastore file exists`(@TempDir tempDir: Path) = runTest {
            val datastorePath = tempDir.resolve("datastore_under_test.jetpref")
            datastorePath.writeText("""
                i;integer;-567
                s;string;"the grass is not greener on the moon"
            """.trimIndent())
            val fileBasedStorage = FileBasedStorage(datastorePath.pathString)
            val datastore = jetprefDataStoreOf(ModelForFileBasedStorage::class)
            datastore.init(
                loadStrategy = LoadStrategy.UseReader(fileBasedStorage),
                persistStrategy = PersistStrategy.Disabled,
            ).assertIsSuccess()

            val prefs by datastore
            assertEquals(-567, prefs.integer.getOrNull())
            assertEquals("the grass is not greener on the moon", prefs.string.getOrNull())
        }

        @Test
        fun `datastore file does not exist`(@TempDir tempDir: Path) = runTest {
            val datastorePath = tempDir.resolve("datastore_under_test.jetpref")
            val fileBasedStorage = FileBasedStorage(datastorePath.pathString)
            val datastore = jetprefDataStoreOf(ModelForFileBasedStorage::class)
            datastore.init(
                loadStrategy = LoadStrategy.UseReader(fileBasedStorage),
                persistStrategy = PersistStrategy.Disabled,
            ).assertIsFailure()

            val prefs by datastore
            assertEquals(null, prefs.integer.getOrNull())
            assertEquals(null, prefs.string.getOrNull())
        }
    }

    @Nested
    inner class Persist {
        suspend fun runDataStoreUnderTest(datastorePath: Path) {
            val fileBasedStorage = FileBasedStorage(datastorePath.pathString)
            val datastore = jetprefDataStoreOf(ModelForFileBasedStorage::class)
            datastore.init(
                loadStrategy = LoadStrategy.Disabled,
                persistStrategy = PersistStrategy.UseWriter(fileBasedStorage),
            ).assertIsSuccess()
            val prefs by datastore

            prefs.string.set("test string").assertIsSuccess()
            assertEquals(
                expected = """
                s;string;"test string"
            """.trimIndent(),
                actual = datastorePath.readText().trim(),
            )

            prefs.integer.set(46).assertIsSuccess()
            assertEquals(
                expected = """
                i;integer;46
                s;string;"test string"
            """.trimIndent(),
                actual = datastorePath.readText().trim(),
            )
        }

        @Test
        fun `datastore file exists`(@TempDir tempDir: Path) = runTest {
            Files.createDirectories(tempDir)
            val datastorePath = tempDir.resolve("datastore_under_test.jetpref")
            datastorePath.writeText("b;boolean;false\n")
            runDataStoreUnderTest(datastorePath)
        }

        @Test
        fun `datastore file does not exists, all parent dirs exist`(@TempDir tempDir: Path) = runTest {
            Files.createDirectories(tempDir)
            val datastorePath = tempDir.resolve("datastore_under_test.jetpref")
            runDataStoreUnderTest(datastorePath)
        }

        @Test
        fun `datastore file does not exists, one parent level dir does not exist`(@TempDir tempDir: Path) = runTest {
            Files.createDirectories(tempDir)
            val datastorePath = tempDir
                .resolve("level1")
                .resolve("datastore_under_test.jetpref")
            runDataStoreUnderTest(datastorePath)
        }

        @Test
        fun `datastore file does not exists, two parent level dirs do not exist`(@TempDir tempDir: Path) = runTest {
            Files.createDirectories(tempDir)
            val datastorePath = tempDir
                .resolve("level1")
                .resolve("level2")
                .resolve("datastore_under_test.jetpref")
            runDataStoreUnderTest(datastorePath)
        }
    }
}
