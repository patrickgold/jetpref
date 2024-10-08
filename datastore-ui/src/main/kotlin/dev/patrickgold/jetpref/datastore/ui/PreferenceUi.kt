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

package dev.patrickgold.jetpref.datastore.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import dev.patrickgold.jetpref.datastore.CachedPreferenceModel
import dev.patrickgold.jetpref.datastore.model.PreferenceDataEvaluator
import dev.patrickgold.jetpref.datastore.model.PreferenceDataEvaluatorScope
import dev.patrickgold.jetpref.datastore.model.PreferenceModel

@DslMarker
@Target(AnnotationTarget.TYPE)
annotation class PreferenceUiScopeDsl

typealias PreferenceUiContent<T> = @Composable @PreferenceUiScopeDsl PreferenceUiScope<T>.() -> Unit

/**
 * Composition local for the global setting if all sub-preference composables should reserve an icon space.
 * This can be overridden for each individual preference composable.
 *
 * @since 0.2.0
 */
val LocalIconSpaceReserved = staticCompositionLocalOf { false }

/**
 * Composition local of the current isEnabled state which applies.
 *
 * @since 0.2.0
 */
val LocalIsPrefEnabled = staticCompositionLocalOf { true }

/**
 * Composition local of the current isVisible state which applies.
 *
 * @since 0.2.0
 */
val LocalIsPrefVisible = staticCompositionLocalOf { true }

/**
 * Preference UI scope which allows access to the current datastore model.
 *
 * @property prefs The current datastore model.
 *
 * @since 0.1.0
 */
class PreferenceUiScope<T : PreferenceModel>(
    val prefs: T,
    columnScope: ColumnScope,
) : ColumnScope by columnScope

/**
 * Material preference layout which allows for easy access to the preference datastore.
 * All preference composables within this layout will make use of the provided datastore
 * automatically.
 *
 * @param cachedPrefModel The cached preference datastore model of your app.
 * @param modifier Modifier to be applied to this layout.
 * @param iconSpaceReserved Global setting if all sub-preference composables should reserve
 *  an additional space if no icon is specified. Can be overridden for each individual preference
 *  composable.
 * @param enabledIf Evaluator scope which allows to dynamically decide if this preference layout
 *  should be enabled (true) or disabled (false).
 * @param visibleIf Evaluator scope which allows to dynamically decide if this preference layout
 *  should be visible (true) or hidden (false).
 * @param content The content of this preference layout.
 *
 * @since 0.1.0
 */
@Composable
fun <T : PreferenceModel> PreferenceLayout(
    cachedPrefModel: CachedPreferenceModel<T>,
    modifier: Modifier = Modifier,
    iconSpaceReserved: Boolean = true,
    enabledIf: PreferenceDataEvaluator = { true },
    visibleIf: PreferenceDataEvaluator = { true },
    content: PreferenceUiContent<T>,
) {
    CompositionLocalProvider(
        LocalIconSpaceReserved provides iconSpaceReserved,
        LocalIsPrefEnabled provides enabledIf(PreferenceDataEvaluatorScope),
        LocalIsPrefVisible provides visibleIf(PreferenceDataEvaluatorScope),
    ) {
        Column(modifier = modifier) {
            val prefModel by cachedPrefModel
            val preferenceScope = PreferenceUiScope(
                prefs = prefModel,
                columnScope = this,
            )
            content(preferenceScope)
        }
    }
}

/**
 * Material preference layout which allows for easy access to the preference datastore.
 * All preference composables within this layout will make use of the provided datastore
 * automatically. Additionally this layout also provides a default scroll modifier.
 *
 * @param cachedPrefModel The cached preference datastore model of your app.
 * @param modifier Modifier to be applied to this layout.
 * @param iconSpaceReserved Global setting if all sub-preference composables should reserve
 *  an additional space if no icon is specified. Can be overridden for each individual preference
 *  composable.
 * @param enabledIf Evaluator scope which allows to dynamically decide if this preference layout
 *  should be enabled (true) or disabled (false).
 * @param visibleIf Evaluator scope which allows to dynamically decide if this preference layout
 *  should be visible (true) or hidden (false).
 * @param content The content of this preference layout.
 *
 * @since 0.1.0
 */
@Composable
fun <T : PreferenceModel> ScrollablePreferenceLayout(
    cachedPrefModel: CachedPreferenceModel<T>,
    modifier: Modifier = Modifier,
    iconSpaceReserved: Boolean = true,
    enabledIf: PreferenceDataEvaluator = { true },
    visibleIf: PreferenceDataEvaluator = { true },
    content: PreferenceUiContent<T>,
) {
    PreferenceLayout(
        cachedPrefModel = cachedPrefModel,
        modifier = modifier.verticalScroll(rememberScrollState()),
        iconSpaceReserved = iconSpaceReserved,
        enabledIf = enabledIf,
        visibleIf = visibleIf,
        content = content,
    )
}

/**
 * Material preference group which automatically provides a title UI.
 *
 * @param modifier Modifier to be applied to this group.
 * @param icon The [ImageVector] of the group title.
 * @param iconSpaceReserved If the space at the start of the list item should be reserved (blank
 *  space) if no `icon` is provided. Also acts as a local setting if all sub-preference composables
 *  should reserve an additional space if no icon is specified. It Can be overridden for each
 *  preference composable.
 * @param title The title of this preference group.
 * @param enabledIf Evaluator scope which allows to dynamically decide if this preference layout
 *  should be enabled (true) or disabled (false).
 * @param visibleIf Evaluator scope which allows to dynamically decide if this preference layout
 *  should be visible (true) or hidden (false).
 * @param content The content of this preference group.
 *
 * @since 0.1.0
 */
@Composable
fun <T : PreferenceModel> PreferenceUiScope<T>.PreferenceGroup(
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    iconSpaceReserved: Boolean = LocalIconSpaceReserved.current,
    title: String,
    enabledIf: PreferenceDataEvaluator = { true },
    visibleIf: PreferenceDataEvaluator = { true },
    content: PreferenceUiContent<T>,
) {
    if (LocalIsPrefVisible.current && visibleIf(PreferenceDataEvaluatorScope)) {
        Column(modifier = modifier) {
            val preferenceScope = PreferenceUiScope(
                prefs = this@PreferenceGroup.prefs,
                columnScope = this@Column,
            )
            CompositionLocalProvider(
                LocalIconSpaceReserved provides iconSpaceReserved,
                LocalIsPrefEnabled provides enabledIf(PreferenceDataEvaluatorScope),
                LocalIsPrefVisible provides visibleIf(PreferenceDataEvaluatorScope),
            ) {
                ListItem(
                    leadingContent = maybeJetIcon(imageVector = icon, iconSpaceReserved = iconSpaceReserved),
                    headlineContent = { Text(
                        text = title,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    ) },
                )
                content(preferenceScope)
            }
        }
    }
}
