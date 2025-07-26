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

import dev.patrickgold.jetpref.datastore.JetPrefModelNotFoundException
import dev.patrickgold.jetpref.datastore.annotations.Preferences
import dev.patrickgold.jetpref.datastore.jetprefDataStoreOf
import dev.patrickgold.jetpref.datastore.model.LocalTime
import dev.patrickgold.jetpref.datastore.model.PreferenceModel
import kotlin.test.Test
import kotlin.test.assertEquals

class ModelProcessorTest {
    @Test
    fun `empty model correctly generated`() {
        val prefs by jetprefDataStoreOf(EmptyModel::class)
        assertEquals(MAGIC_VALUE, prefs.magicValue)
    }

    @Test
    fun `flat model correctly generated`() {
        val prefs by jetprefDataStoreOf(FlatModel::class)
        assertEquals(
            expected = mapOf(prefs.numericPref.key to prefs.numericPref),
            actual = prefs.declaredPreferenceEntries,
        )
    }

    @Test
    fun `nested shallow model correctly generated`() {
        val prefs by jetprefDataStoreOf(NestedShallowModel::class)
        assertEquals(
            expected = mapOf(
                prefs.numericPref.key to prefs.numericPref,
                prefs.theme.stringPref.key to prefs.theme.stringPref,
            ),
            actual = prefs.declaredPreferenceEntries,
        )
    }

    @Test
    fun `nested deep model correctly generated`() {
        val prefs by jetprefDataStoreOf(NestedDeepModel::class)
        assertEquals(
            expected = mapOf(
                prefs.numericPref.key to prefs.numericPref,
                prefs.theme.stringPref.key to prefs.theme.stringPref,
                prefs.system.time.key to prefs.system.time,
                prefs.system.audio.floatPref.key to prefs.system.audio.floatPref,
            ),
            actual = prefs.declaredPreferenceEntries,
        )
    }

    @Test
    fun `model with symbols requiring backtick escaping correctly generated`() {
        val prefs by jetprefDataStoreOf(`Model with symbols requiring backtick escaping`::class)
        assertEquals(
            expected = mapOf(
                prefs.`property with spaces`.key to prefs.`property with spaces`,
                prefs.`pref-group`.`package+name`.key to prefs.`pref-group`.`package+name`,
            ),
            actual = prefs.declaredPreferenceEntries,
        )
    }

    @Test
    fun `models without annotation not generated`() {
        listOf(
            EmptyModelWithoutAnnotation::class,
            FlatModelWithoutAnnotation::class,
            NestedShallowModelWithoutAnnotation::class,
            NestedDeepModelWithoutAnnotation::class,
        ).forEach { modelClass ->
            assertThrows<JetPrefModelNotFoundException> {
                jetprefDataStoreOf(modelClass)
            }
        }

    }
}

private const val MAGIC_VALUE = 0xdeadbeefL

@Preferences
abstract class EmptyModel : PreferenceModel() {
    val magicValue = MAGIC_VALUE
}

abstract class EmptyModelWithoutAnnotation : PreferenceModel()

@Preferences
abstract class FlatModel : PreferenceModel() {
    val numericPref = int(
        key = "numeric_pref",
        default = 0,
    )
}

@Suppress("unused")
abstract class FlatModelWithoutAnnotation : PreferenceModel() {
    val numericPref = int(
        key = "numeric_pref",
        default = 0,
    )
}

@Preferences
abstract class NestedShallowModel : PreferenceModel() {
    val numericPref = int(
        key = "numeric_pref",
        default = 0,
    )

    val theme = Theme()
    inner class Theme {
        val stringPref = string(
            key = "string_pref",
            default = "hello world",
        )
    }
}

@Suppress("unused")
abstract class NestedShallowModelWithoutAnnotation : PreferenceModel() {
    val numericPref = int(
        key = "numeric_pref",
        default = 0,
    )

    val theme = Theme()
    inner class Theme {
        val stringPref = string(
            key = "string_pref",
            default = "hello world",
        )
    }
}

@Preferences
abstract class NestedDeepModel : PreferenceModel() {
    val numericPref = int(
        key = "numeric_pref",
        default = 0,
    )

    val theme = Theme()
    inner class Theme {
        val stringPref = string(
            key = "string_pref",
            default = "hello world",
        )
    }

    val system = System()
    inner class System {
        val time = localTime(
            key = "local_time_pref",
            default = LocalTime(3, 45),
        )

        val audio = Audio()
        inner class Audio {
            val floatPref = float(
                key = "float_pref",
                default = 1.0f,
            )
        }
    }
}

@Suppress("unused")
abstract class NestedDeepModelWithoutAnnotation : PreferenceModel() {
    val numericPref = int(
        key = "numeric_pref",
        default = 0,
    )

    val theme = Theme()
    inner class Theme {
        val stringPref = string(
            key = "string_pref",
            default = "hello world",
        )
    }

    val system = System()
    inner class System {
        val time = localTime(
            key = "local_time_pref",
            default = LocalTime(3, 45),
        )

        val audio = Audio()
        inner class Audio {
            val floatPref = float(
                key = "float_pref",
                default = 1.0f,
            )
        }
    }
}

@Suppress("ClassName", "PropertyName")
@Preferences
abstract class `Model with symbols requiring backtick escaping` : PreferenceModel() {
    val `property with spaces` = int(
        key = "property_with_spaces",
        default = 0,
    )

    val `pref-group` = `Pref Group`()
    inner class `Pref Group` {
        val `package+name` = string(
            key = "package_name",
            default = "",
        )
    }
}
