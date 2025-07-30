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

package com.example.tests

import dev.patrickgold.jetpref.datastore.annotations.Preferences
import dev.patrickgold.jetpref.datastore.jetprefDataStoreOf
import dev.patrickgold.jetpref.datastore.model.LocalTime
import dev.patrickgold.jetpref.datastore.model.PreferenceModel
import dev.patrickgold.jetpref.datastore.runtime.DataStore
import dev.patrickgold.jetpref.datastore.runtime.ImportStrategy
import dev.patrickgold.jetpref.datastore.runtime.LoadStrategy
import dev.patrickgold.jetpref.datastore.runtime.PersistStrategy
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@Preferences
abstract class ModelForImportTest : PreferenceModel() {
    val integer = int(
        key = "integer",
        default = 0,
    )
    val string = string(
        key = "string",
        default = "",
    )
    val localTime = localTime(
        key = "local_time",
        default = LocalTime(0, 0),
    )
}

class ModelImportTest {
    suspend fun baseLoadDatastore(
        persistStrategy: PersistStrategy,
    ): DataStore<ModelForImportTest> {
        val datastore = jetprefDataStoreOf(ModelForImportTest::class)
        datastore.init(
            loadStrategy = LoadStrategy.UseReader {
                """
                i;integer;43
                s;string;"test data"
                s;local_time;"14:35:00.000"
                """.trimIndent()
            },
            persistStrategy = persistStrategy,
        ).assertIsSuccess()
        val prefs by datastore
        assertEquals(43, prefs.integer.getOrNull())
        assertEquals("test data", prefs.string.getOrNull())
        assertEquals(LocalTime(14, 35), prefs.localTime.getOrNull())
        return datastore
    }

    @Test
    fun `merge import keeps current values and overrides incoming ones`() = runTest {
        val datastore = baseLoadDatastore(PersistStrategy.Disabled)
        val prefs by datastore

        datastore.import(ImportStrategy.Merge) {
            "" // empty datastore
        }.assertIsSuccess()
        assertEquals(43, prefs.integer.getOrNull())
        assertEquals("test data", prefs.string.getOrNull())
        assertEquals(LocalTime(14, 35), prefs.localTime.getOrNull())

        datastore.import(ImportStrategy.Merge) {
            """
            i;integer;20
            """.trimIndent()
        }.assertIsSuccess()
        assertEquals(20, prefs.integer.getOrNull())
        assertEquals("test data", prefs.string.getOrNull())
        assertEquals(LocalTime(14, 35), prefs.localTime.getOrNull())

        datastore.import(ImportStrategy.Merge) {
            """
            s;string;"new data"
            """.trimIndent()
        }.assertIsSuccess()
        assertEquals(20, prefs.integer.getOrNull())
        assertEquals("new data", prefs.string.getOrNull())
        assertEquals(LocalTime(14, 35), prefs.localTime.getOrNull())
    }

    @Test
    fun `merge import with incorrect types keeps current values`() = runTest {
        val datastore = baseLoadDatastore(PersistStrategy.Disabled)
        val prefs by datastore

        datastore.import(ImportStrategy.Merge) {
            """
            l;integer;43
            b;string;true
            d;local_time;14.35
            """.trimIndent()
        }.assertIsSuccess()
        assertEquals(43, prefs.integer.getOrNull())
        assertEquals("test data", prefs.string.getOrNull())
        assertEquals(LocalTime(14, 35), prefs.localTime.getOrNull())
    }

    @Test
    fun `merge import with failing reader keeps current values`() = runTest {
        val datastore = baseLoadDatastore(PersistStrategy.Disabled)
        val prefs by datastore

        datastore.import(ImportStrategy.Merge) {
            throw Exception()
        }.assertIsFailure()
        assertEquals(43, prefs.integer.getOrNull())
        assertEquals("test data", prefs.string.getOrNull())
        assertEquals(LocalTime(14, 35), prefs.localTime.getOrNull())
    }

    @Test
    fun `merge import persists new state correctly`() = runTest {
        val expectedContentWritten = """
            i;integer;43
            s;string;"new data"
            s;local_time;"14:35:00.000"
        """.trimIndent()
        var actualContentWritten: String? = null
        val persistStrategy = PersistStrategy.UseWriter { content ->
            actualContentWritten = content.trim()
        }
        val datastore = baseLoadDatastore(persistStrategy)

        datastore.import(ImportStrategy.Merge) {
            """
            s;string;"new data"
            i;non_existent_pref;42
            """.trimIndent()
        }.assertIsSuccess()
        assertEquals(expectedContentWritten, actualContentWritten)
    }

    @Test
    fun `erase import resets current values and sets incoming ones`() = runTest {
        val datastore = baseLoadDatastore(PersistStrategy.Disabled)
        val prefs by datastore

        datastore.import(ImportStrategy.Erase) {
            """
            i;integer;20
            """.trimIndent()
        }.assertIsSuccess()
        assertEquals(20, prefs.integer.getOrNull())
        assertNull(prefs.string.getOrNull())
        assertNull(prefs.localTime.getOrNull())
    }

    @Test
    fun `erase import with incorrect types resets current values`() = runTest {
        val datastore = baseLoadDatastore(PersistStrategy.Disabled)
        val prefs by datastore

        datastore.import(ImportStrategy.Erase) {
            """
            l;integer;43
            b;string;true
            d;local_time;14.35
            """.trimIndent()
        }.assertIsSuccess()
        assertNull(prefs.integer.getOrNull())
        assertNull(prefs.string.getOrNull())
        assertNull(prefs.localTime.getOrNull())
    }

    @Test
    fun `erase import with failing reader resets current values`() = runTest {
        val datastore = baseLoadDatastore(PersistStrategy.Disabled)
        val prefs by datastore

        datastore.import(ImportStrategy.Erase) {
            throw Exception()
        }.assertIsFailure()
        assertNull(prefs.integer.getOrNull())
        assertNull(prefs.string.getOrNull())
        assertNull(prefs.localTime.getOrNull())
    }

    @Test
    fun `erase import persists new state correctly`() = runTest {
        val expectedContentWritten = """
            s;string;"new data"
        """.trimIndent()
        var actualContentWritten: String? = null
        val persistStrategy = PersistStrategy.UseWriter { content ->
            actualContentWritten = content.trim()
        }
        val datastore = baseLoadDatastore(persistStrategy)

        datastore.import(ImportStrategy.Erase) {
            """
            s;string;"new data"
            i;non_existent_pref;42
            """.trimIndent()
        }.assertIsSuccess()
        assertEquals(expectedContentWritten, actualContentWritten)
    }
}
