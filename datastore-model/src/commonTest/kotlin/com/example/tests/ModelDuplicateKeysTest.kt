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
import dev.patrickgold.jetpref.datastore.model.PreferenceModel
import dev.patrickgold.jetpref.datastore.runtime.PreferenceModelDuplicateKeyException
import kotlin.test.Test

@Preferences
abstract class ModelForDuplicateKeysSameTypeTest : PreferenceModel() {
    val pref1 = int(
        key = "numeric_pref",
        default = 0,
    )
    val pref2 = int(
        key = "numeric_pref",
        default = 0,
    )
}

@Preferences
abstract class ModelForDuplicateKeysDifferentTypeTest : PreferenceModel() {
    val pref1 = int(
        key = "numeric_pref",
        default = 0,
    )
    val pref2 = string(
        key = "numeric_pref",
        default = "0",
    )
}

class ModelDuplicateKeysTest {
    @Test
    fun `duplicate keys with same type should throw`() {
        assertThrows<PreferenceModelDuplicateKeyException> {
            jetprefDataStoreOf(ModelForDuplicateKeysSameTypeTest::class)
        }
    }

    @Test
    fun `duplicate keys with different type should throw`() {
        assertThrows<PreferenceModelDuplicateKeyException> {
            jetprefDataStoreOf(ModelForDuplicateKeysDifferentTypeTest::class)
        }
    }
}
