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

@Preferences
abstract class ModelForValueFlowTest : PreferenceModel() {
    companion object {
        // Do not use 0-ish/-1 values, as these are often defaults on failures
        const val DEFAULT_INTEGER = 817
        const val DEFAULT_STRING = "default string 1287648764"
        val DEFAULT_LOCAL_TIME = LocalTime(14, 8, 34, 7)
    }

    val integer = int(
        key = "integer",
        default = DEFAULT_INTEGER,
    )
    val string = string(
        key = "string",
        default = DEFAULT_STRING,
    )
    val localTime = localTime(
        key = "local_time",
        default = DEFAULT_LOCAL_TIME,
    )
}

class ModelValueFlowTest {
    suspend fun baseLoadDatastore(
        persistStrategy: PersistStrategy,
    ): DataStore<ModelForValueFlowTest> {
        val datastore = jetprefDataStoreOf(ModelForValueFlowTest::class)
        val prefs by datastore

        datastore.init(
            loadStrategy = LoadStrategy.Disabled,
            persistStrategy = persistStrategy,
        ).assertIsSuccess()

        assertEquals(null, prefs.integer.getOrNull())
        assertEquals(null, prefs.string.getOrNull())
        assertEquals(null, prefs.localTime.getOrNull())
        return datastore
    }

    @Test
    fun `value state flow holds current or default value`() = runTest {
        val datastore = baseLoadDatastore(PersistStrategy.Disabled)
        val prefs by datastore

        assertEquals(ModelForValueFlowTest.DEFAULT_INTEGER, prefs.integer.asFlow().value)
        prefs.integer.set(14)
        assertEquals(14, prefs.integer.asFlow().value)
        prefs.integer.set(-567)
        assertEquals(-567, prefs.integer.asFlow().value)
        prefs.integer.reset()
        assertEquals(ModelForValueFlowTest.DEFAULT_INTEGER, prefs.integer.asFlow().value)
    }
}
