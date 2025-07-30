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
import dev.patrickgold.jetpref.datastore.model.PreferenceType
import dev.patrickgold.jetpref.datastore.runtime.LoadStrategy
import dev.patrickgold.jetpref.datastore.runtime.PersistStrategy
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

@Preferences
abstract class ModelForLoadTest : PreferenceModel() {
    enum class Theme {
        SYSTEM_DEFAULT,
        EYE_BURNER,
        NIGHT_OWL,
    }

    companion object {
        const val DEFAULT_BOOLEAN: Boolean = true
        const val DEFAULT_DOUBLE: Double = 42.8
        const val DEFAULT_FLOAT: Float = 31.7f
        const val DEFAULT_INT: Int = 13
        const val DEFAULT_LONG: Long = 17L
        const val DEFAULT_STRING: String = "hello_world_123"
        val DEFAULT_ENUM: Theme = Theme.NIGHT_OWL
        val DEFAULT_LOCAL_TIME: LocalTime = LocalTime(20, 0)
    }

    val boolean = boolean(
        key = "boolean",
        default = DEFAULT_BOOLEAN,
    )
    val double = double(
        key = "double",
        default = DEFAULT_DOUBLE,
    )
    val float = float(
        key = "float",
        default = DEFAULT_FLOAT,
    )
    val integer = int(
        key = "integer",
        default = DEFAULT_INT,
    )
    val long = long(
        key = "long",
        default = DEFAULT_LONG,
    )
    val string = string(
        key = "string",
        default = DEFAULT_STRING,
    )
    val enum = enum(
        key = "enum",
        default = DEFAULT_ENUM,
    )
    val localTime = localTime(
        key = "local_time",
        default = DEFAULT_LOCAL_TIME,
    )
}

// TODO: extend with a lot more examples for error cases and edge cases!!
class ModelLoadTest {
    @Test
    fun `model is loaded correctly`() = runTest {
        val datastore = jetprefDataStoreOf(ModelForLoadTest::class)
        datastore.init(
            loadStrategy = LoadStrategy.UseReader {
                """
                ${PreferenceType.boolean()};boolean;false
                ${PreferenceType.double()};double;99.123
                ${PreferenceType.float()};float;12.34
                ${PreferenceType.integer()};integer;42
                ${PreferenceType.long()};long;123456789
                ${PreferenceType.string()};string;"random_text_456"
                ${PreferenceType.string()};enum;"EYE_BURNER"
                ${PreferenceType.string()};local_time;"15:45:00.000"
                """.trimIndent()
            },
            persistStrategy = PersistStrategy.Disabled,
        ).assertIsSuccess()

        val prefs by datastore
        assertEquals(false, prefs.boolean.getOrNull())
        assertEquals(99.123, prefs.double.getOrNull())
        assertEquals(12.34f, prefs.float.getOrNull())
        assertEquals(42, prefs.integer.getOrNull())
        assertEquals(123456789, prefs.long.getOrNull())
        assertEquals("random_text_456", prefs.string.getOrNull())
        assertEquals(ModelForLoadTest.Theme.EYE_BURNER, prefs.enum.getOrNull())
        assertEquals(LocalTime(15, 45), prefs.localTime.getOrNull())
    }

    @Test
    fun `model failed load sets all values default`() = runTest {
        val datastore = jetprefDataStoreOf(ModelForLoadTest::class)
        val exception = Exception("File not found")
        datastore.init(
            loadStrategy = LoadStrategy.UseReader {
                throw exception
            },
            persistStrategy = PersistStrategy.Disabled,
        ).assertIsFailure(exception)

        val prefs by datastore
        assertEquals(null, prefs.boolean.getOrNull())
        assertEquals(null, prefs.double.getOrNull())
        assertEquals(null, prefs.float.getOrNull())
        assertEquals(null, prefs.integer.getOrNull())
        assertEquals(null, prefs.long.getOrNull())
        assertEquals(null, prefs.string.getOrNull())
        assertEquals(null, prefs.enum.getOrNull())
        assertEquals(null, prefs.localTime.getOrNull())
    }

    @Test
    fun `model failed re-load resets all entries`() = runTest {
        val datastore = jetprefDataStoreOf(ModelForLoadTest::class)
        datastore.init(
            loadStrategy = LoadStrategy.UseReader {
                """
                ${PreferenceType.boolean()};boolean;false
                ${PreferenceType.double()};double;99.123
                ${PreferenceType.float()};float;12.34
                ${PreferenceType.integer()};integer;42
                ${PreferenceType.long()};long;123456789
                ${PreferenceType.string()};string;"random_text_456"
                ${PreferenceType.string()};enum;"EYE_BURNER"
                ${PreferenceType.string()};local_time;"15:45:00.000"
                """.trimIndent()
            },
            persistStrategy = PersistStrategy.Disabled,
        ).assertIsSuccess()

        val prefs by datastore
        assertEquals(false, prefs.boolean.getOrNull())
        assertEquals(99.123, prefs.double.getOrNull())
        assertEquals(12.34f, prefs.float.getOrNull())
        assertEquals(42, prefs.integer.getOrNull())
        assertEquals(123456789, prefs.long.getOrNull())
        assertEquals("random_text_456", prefs.string.getOrNull())
        assertEquals(ModelForLoadTest.Theme.EYE_BURNER, prefs.enum.getOrNull())
        assertEquals(LocalTime(15, 45), prefs.localTime.getOrNull())

        val exception2 = Exception("File not found")
        datastore.init(
            loadStrategy = LoadStrategy.UseReader {
                throw exception2
            },
            persistStrategy = PersistStrategy.Disabled,
        ).assertIsFailure(exception2)

        assertEquals(null, prefs.boolean.getOrNull())
        assertEquals(null, prefs.double.getOrNull())
        assertEquals(null, prefs.float.getOrNull())
        assertEquals(null, prefs.integer.getOrNull())
        assertEquals(null, prefs.long.getOrNull())
        assertEquals(null, prefs.string.getOrNull())
        assertEquals(null, prefs.enum.getOrNull())
        assertEquals(null, prefs.localTime.getOrNull())
    }
}
