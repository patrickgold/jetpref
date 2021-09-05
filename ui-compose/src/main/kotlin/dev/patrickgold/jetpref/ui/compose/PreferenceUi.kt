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
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ListItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import dev.patrickgold.jetpref.datastore.model.PreferenceDataEvaluator
import dev.patrickgold.jetpref.datastore.model.PreferenceDataEvaluatorScope
import dev.patrickgold.jetpref.datastore.model.PreferenceModel
import dev.patrickgold.jetpref.datastore.preferenceModel

@DslMarker
@Target(AnnotationTarget.TYPE)
annotation class PreferenceUiScopeDsl

typealias PreferenceUiContent<T> = @Composable @PreferenceUiScopeDsl PreferenceUiScope<T>.() -> Unit

class PreferenceUiScope<T : PreferenceModel>(
    val prefs: T,
    val iconSpaceReserved: Boolean,
    val enabledIf: PreferenceDataEvaluator,
    val visibleIf: PreferenceDataEvaluator,
    columnScope: ColumnScope,
) : ColumnScope by columnScope

@Composable
inline fun <reified T : PreferenceModel> PreferenceScreen(
    noinline factory: () -> T,
    iconSpaceReserved: Boolean = false,
    noinline enabledIf: PreferenceDataEvaluator = { true },
    noinline visibleIf: PreferenceDataEvaluator = { true },
    content: PreferenceUiContent<T>,
) {
    Column {
        val prefModel by preferenceModel(factory)
        val preferenceScope = PreferenceUiScope(
            prefs = prefModel,
            iconSpaceReserved = iconSpaceReserved,
            enabledIf = enabledIf,
            visibleIf = visibleIf,
            columnScope = this,
        )

        content(preferenceScope)
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun <T : PreferenceModel> PreferenceUiScope<T>.PreferenceGroup(
    @DrawableRes iconId: Int? = null,
    iconSpaceReserved: Boolean = this.iconSpaceReserved,
    title: String,
    enabledIf: PreferenceDataEvaluator = this.enabledIf,
    visibleIf: PreferenceDataEvaluator = this.visibleIf,
    content: PreferenceUiContent<T>,
) {
    if (visibleIf(PreferenceDataEvaluatorScope.instance())) {
        Column {
            val preferenceScope = PreferenceUiScope(
                prefs = this@PreferenceGroup.prefs,
                iconSpaceReserved = iconSpaceReserved,
                enabledIf = enabledIf,
                visibleIf = visibleIf,
                columnScope = this@Column,
            )

            ListItem(
                icon = maybeJetIcon(iconId, iconSpaceReserved),
                text = { Text(
                    text = title,
                    color = MaterialTheme.colors.secondary,
                    fontWeight = FontWeight.Bold,
                ) },
            )
            content(preferenceScope)
        }
    }
}
