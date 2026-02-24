/*
 * Copyright 2021-2026 Patrick Goldinger
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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import dev.patrickgold.jetpref.datastore.component.flash

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
val LocalIsPrefEnabled = compositionLocalOf { true }

/**
 * Composition local of the current isVisible state which applies.
 *
 * @since 0.2.0
 */
val LocalIsPrefVisible = compositionLocalOf { true }

/**
 * Composition local providing the flash modifier for component search targets.
 *
 * @since 0.4.0
 */
val LocalFlashModifierProvider = staticCompositionLocalOf<(() -> Modifier)> {
    error("No FlashModifierProvider provided")
}

/**
 * Root element for the JetPref hierarchy. Must be used to provide necessary composition locals.
 *
 * @param router The router that will map a screen route request to the in-app navigation controller.
 * @param iconSpaceReserved If the space at the start of the list item should be reserved (blank
*   space) if no `icon` is provided for a preference.
 * @param provideFlashModifier The modifier that will be applied after jumping to a component from search.
 *
 * @since 0.4.0
 */
@Composable
fun JetPrefHost(
    router: PreferenceNavigationRouter,
    iconSpaceReserved: Boolean = true,
    provideFlashModifier: (() -> Modifier) = { Modifier.flash() },
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalPreferenceNavigationRouter provides router,
        LocalFlashModifierProvider provides provideFlashModifier,
        LocalIconSpaceReserved provides iconSpaceReserved,
    ) {
        content()
    }
}
