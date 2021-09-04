/*
 * Copyright 2021 Patrick Goldinger
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

package dev.patrickgold.jetpref.ui.compose

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import dev.patrickgold.jetpref.datastore.model.PreferenceDataEvaluator
import dev.patrickgold.jetpref.datastore.model.PreferenceModel
import dev.patrickgold.jetpref.datastore.preferenceModel

@Composable
inline fun <reified T : PreferenceModel> PreferenceScreen(
    noinline factory: () -> T,
    content: @Composable PreferenceScope<T>.() -> Unit,
) {
    Column {
        val prefModel by preferenceModel(factory)
        val preferenceScope = PreferenceScope(prefModel, this)

        content(preferenceScope)
    }
}

@Composable
fun <T : PreferenceModel> PreferenceScope<T>.PreferenceGroup(
    @DrawableRes iconId: Int? = null,
    iconSpaceReserved: Boolean = false,
    title: String,
    enabledIf: @Composable PreferenceDataEvaluator.() -> Boolean = { true },
    visibleIf: @Composable PreferenceDataEvaluator.() -> Boolean = { true },
    content: @Composable PreferenceScope<T>.() -> Unit,
) {

    if (visibleIf(PreferenceDataEvaluator.instance())) {
        Column {
            val preferenceScope = PreferenceScope(this@PreferenceGroup.prefs, this@Column)

            Text(text = title)
            content(preferenceScope)
        }
    }
}

class PreferenceScope<T : PreferenceModel>(
    val prefs: T,
    columnScope: ColumnScope,
) : ColumnScope by columnScope
