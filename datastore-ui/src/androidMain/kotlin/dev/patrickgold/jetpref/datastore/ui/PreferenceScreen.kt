/*
 * Copyright 2026 Patrick Goldinger
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
import androidx.compose.ui.Modifier
import dev.patrickgold.jetpref.datastore.component.PreferenceComponentScreen

@Composable
fun PreferenceScreen(
    screen: PreferenceComponentScreen,
    modifier: Modifier = Modifier,
    iconSpaceReserved: Boolean = LocalIconSpaceReserved.current,
) {
    CompositionLocalProvider(LocalIconSpaceReserved provides iconSpaceReserved) {
        screen.Render(modifier)
    }
}
