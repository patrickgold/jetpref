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
import dev.patrickgold.jetpref.datastore.runtime.LoadStrategy
import dev.patrickgold.jetpref.datastore.runtime.PersistStrategy
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class ModelPersistTest {
    suspend fun baseLoadDatastore(
        persistStrategy: PersistStrategy,
    ): DataStore<ModelToBePersisted> {
        val datastore = jetprefDataStoreOf(ModelToBePersisted::class)
        datastore.init(
            loadStrategy = LoadStrategy.Disabled,
            persistStrategy = persistStrategy,
        ).assertIsSuccess()
        val prefs by datastore
        assertEquals(null, prefs.integer.getOrNull())
        assertEquals(null, prefs.string.getOrNull())
        assertEquals(null, prefs.localTime.getOrNull())
        return datastore
    }

    @Test
    fun `values are persisted when changed`() = runTest {
        var actualContentWritten: String? = null
        val persistStrategy = PersistStrategy.UseWriter { content ->
            actualContentWritten = content.trim()
        }
        val datastore = baseLoadDatastore(persistStrategy)
        val prefs by datastore

        prefs.integer.set(34).assertIsSuccess()
        assertEquals("""
            i;integer;34
        """.trimIndent(), actualContentWritten)

        prefs.integer.set(20).assertIsSuccess()
        assertEquals("""
            i;integer;20
        """.trimIndent(), actualContentWritten)

        prefs.string.set("some string").assertIsSuccess()
        assertEquals("""
            i;integer;20
            s;string;"some string"
        """.trimIndent(), actualContentWritten)

        prefs.integer.reset().assertIsSuccess()
        assertEquals("""
            s;string;"some string"
        """.trimIndent(), actualContentWritten)

        prefs.localTime.reset().assertIsSuccess()
        assertEquals("""
            s;string;"some string"
        """.trimIndent(), actualContentWritten)

        prefs.localTime.set(LocalTime(14, 52, 18)).assertIsSuccess()
        assertEquals("""
            s;string;"some string"
            s;local_time;"14:52:18.000"
        """.trimIndent(), actualContentWritten)
    }

    @Test
    fun `failing writer does not affect in-memory behavior`() = runTest {
        class WriterFailedException : Exception()
        val persistStrategy = PersistStrategy.UseWriter { content ->
            throw WriterFailedException()
        }
        val datastore = baseLoadDatastore(persistStrategy)
        val prefs by datastore

        assertIs<WriterFailedException>(prefs.integer.set(42).assertIsFailure())
        assertEquals(42, prefs.integer.getOrNull())
        assertEquals(null, prefs.string.getOrNull())
        assertEquals(null, prefs.localTime.getOrNull())

        assertIs<WriterFailedException>(prefs.integer.reset().assertIsFailure())
        assertEquals(null, prefs.integer.getOrNull())
        assertEquals(null, prefs.string.getOrNull())
        assertEquals(null, prefs.localTime.getOrNull())

        assertIs<WriterFailedException>(prefs.string.set("test data string").assertIsFailure())
        assertEquals(null, prefs.integer.getOrNull())
        assertEquals("test data string", prefs.string.getOrNull())
        assertEquals(null, prefs.localTime.getOrNull())

        assertIs<WriterFailedException>(prefs.localTime.set(LocalTime(1, 34, 21, 961)).assertIsFailure())
        assertEquals(null, prefs.integer.getOrNull())
        assertEquals("test data string", prefs.string.getOrNull())
        assertEquals(LocalTime(1, 34, 21, 961), prefs.localTime.getOrNull())

        assertIs<WriterFailedException>(prefs.localTime.reset().assertIsFailure())
        assertEquals(null, prefs.integer.getOrNull())
        assertEquals("test data string", prefs.string.getOrNull())
        assertEquals(null, prefs.localTime.getOrNull())
    }
}

@Preferences
abstract class ModelToBePersisted : PreferenceModel() {
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
