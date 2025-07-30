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

import dev.patrickgold.jetpref.datastore.jetprefDataStoreOf
import dev.patrickgold.jetpref.datastore.model.LocalTime
import dev.patrickgold.jetpref.datastore.model.PreferenceType
import dev.patrickgold.jetpref.datastore.runtime.DataStoreReader
import dev.patrickgold.jetpref.datastore.runtime.LoadStrategy
import dev.patrickgold.jetpref.datastore.runtime.PersistStrategy
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

// TODO: extend with a lot more examples for error cases and edge cases!!
class ModelLoadTest {
    @Test
    fun `model is loaded correctly`() = runTest {
        val datastore = jetprefDataStoreOf(GenericFlatModel::class)
        val reader = createLoadableReader(
            """
            ${PreferenceType.boolean()};${GenericFlatModel.KEY_BOOLEAN};false
            ${PreferenceType.double()};${GenericFlatModel.KEY_DOUBLE};99.123
            ${PreferenceType.float()};${GenericFlatModel.KEY_FLOAT};12.34
            ${PreferenceType.integer()};${GenericFlatModel.KEY_INT};42
            ${PreferenceType.long()};${GenericFlatModel.KEY_LONG};123456789
            ${PreferenceType.string()};${GenericFlatModel.KEY_STRING};"random_text_456"
            ${PreferenceType.string()};${GenericFlatModel.KEY_ENUM};"PITCH_BLACK"
            ${PreferenceType.string()};${GenericFlatModel.KEY_LOCAL_TIME};"15:45:00.000"
        """.trimIndent()
        )
        val loadResult = datastore.init(
            loadStrategy = LoadStrategy.UseReader(reader),
            persistStrategy = PersistStrategy.Disabled,
        )
        assertNull(loadResult.exceptionOrNull())

        val prefs by datastore
        assertEquals(false, prefs.prefBoolean.getOrNull())
        assertEquals(99.123, prefs.prefDouble.getOrNull())
        assertEquals(12.34f, prefs.prefFloat.getOrNull())
        assertEquals(42, prefs.prefInt.getOrNull())
        assertEquals(123456789, prefs.prefLong.getOrNull())
        assertEquals("random_text_456", prefs.prefString.getOrNull())
        assertEquals(GenericFlatModel.Theme.PITCH_BLACK, prefs.prefEnum.getOrNull())
        assertEquals(LocalTime(15, 45), prefs.prefLocalTime.getOrNull())
    }

    @Test
    fun `model failed load sets all values default`() = runTest {
        val datastore = jetprefDataStoreOf(GenericFlatModel::class)
        val exception = Exception("File not found")
        val reader = createFailingReader(exception)
        val loadResult = datastore.init(
            loadStrategy = LoadStrategy.UseReader(reader),
            persistStrategy = PersistStrategy.Disabled,
        )
        assertEquals(exception, loadResult.exceptionOrNull())

        val prefs by datastore
        assertEquals(null, prefs.prefBoolean.getOrNull())
        assertEquals(null, prefs.prefDouble.getOrNull())
        assertEquals(null, prefs.prefFloat.getOrNull())
        assertEquals(null, prefs.prefInt.getOrNull())
        assertEquals(null, prefs.prefLong.getOrNull())
        assertEquals(null, prefs.prefString.getOrNull())
        assertEquals(null, prefs.prefEnum.getOrNull())
        assertEquals(null, prefs.prefLocalTime.getOrNull())
    }

    @Test
    fun `model failed re-load resets all entries`() = runTest {
        val datastore = jetprefDataStoreOf(GenericFlatModel::class)
        val reader = createLoadableReader(
            """
            ${PreferenceType.boolean()};${GenericFlatModel.KEY_BOOLEAN};false
            ${PreferenceType.double()};${GenericFlatModel.KEY_DOUBLE};99.123
            ${PreferenceType.float()};${GenericFlatModel.KEY_FLOAT};12.34
            ${PreferenceType.integer()};${GenericFlatModel.KEY_INT};42
            ${PreferenceType.long()};${GenericFlatModel.KEY_LONG};123456789
            ${PreferenceType.string()};${GenericFlatModel.KEY_STRING};"random_text_456"
            ${PreferenceType.string()};${GenericFlatModel.KEY_ENUM};"PITCH_BLACK"
            ${PreferenceType.string()};${GenericFlatModel.KEY_LOCAL_TIME};"15:45:00.000"
        """.trimIndent()
        )
        val loadResult = datastore.init(
            loadStrategy = LoadStrategy.UseReader(reader),
            persistStrategy = PersistStrategy.Disabled,
        )
        assertNull(loadResult.exceptionOrNull())

        val prefs by datastore
        assertEquals(false, prefs.prefBoolean.getOrNull())
        assertEquals(99.123, prefs.prefDouble.getOrNull())
        assertEquals(12.34f, prefs.prefFloat.getOrNull())
        assertEquals(42, prefs.prefInt.getOrNull())
        assertEquals(123456789, prefs.prefLong.getOrNull())
        assertEquals("random_text_456", prefs.prefString.getOrNull())
        assertEquals(GenericFlatModel.Theme.PITCH_BLACK, prefs.prefEnum.getOrNull())
        assertEquals(LocalTime(15, 45), prefs.prefLocalTime.getOrNull())

        val exception2 = Exception("File not found")
        val reader2 = createFailingReader(exception2)
        val loadResult2 = datastore.init(
            loadStrategy = LoadStrategy.UseReader(reader2),
            persistStrategy = PersistStrategy.Disabled,
        )
        assertEquals(exception2, loadResult2.exceptionOrNull())

        assertEquals(null, prefs.prefBoolean.getOrNull())
        assertEquals(null, prefs.prefDouble.getOrNull())
        assertEquals(null, prefs.prefFloat.getOrNull())
        assertEquals(null, prefs.prefInt.getOrNull())
        assertEquals(null, prefs.prefLong.getOrNull())
        assertEquals(null, prefs.prefString.getOrNull())
        assertEquals(null, prefs.prefEnum.getOrNull())
        assertEquals(null, prefs.prefLocalTime.getOrNull())
    }
}

private fun createLoadableReader(content: String): DataStoreReader {
    return object : DataStoreReader {
        override suspend fun read(): String {
            return content
        }
    }
}

private fun createFailingReader(exception: Throwable): DataStoreReader {
    return object : DataStoreReader {
        override suspend fun read(): String {
            throw exception
        }
    }
}
