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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.res.stringResource

data class DialogPrefStrings(
    val confirmLabel: String,
    val dismissLabel: String,
    val neutralLabel: String,
)

val LocalDefaultDialogPrefStrings = staticCompositionLocalOf {
    DialogPrefStrings(
        confirmLabel = "Ok",
        dismissLabel = "Cancel",
        neutralLabel = "Default",
    )
}

/**
 * Provides the default button strings to use for all preferences
 * which show a dialog.
 *
 * @param confirmLabel The label for the confirm button. Null means
 *  no preferred default value.
 * @param dismissLabel The label for the dismiss button. Null means
 *  no preferred default value.
 * @param neutralLabel The label for the neutral button. Null means
 *  no preferred default value.
 */
@Composable
fun ProvideDefaultDialogPrefStrings(
    confirmLabel: String = stringResource(android.R.string.ok),
    dismissLabel: String = stringResource(android.R.string.cancel),
    neutralLabel: String = "Default",
    content: @Composable () -> Unit,
) {
    val dialogPrefStrings = remember(confirmLabel, dismissLabel, neutralLabel) {
        DialogPrefStrings(confirmLabel, dismissLabel, neutralLabel)
    }
    CompositionLocalProvider(
        LocalDefaultDialogPrefStrings provides dialogPrefStrings,
        content = content,
    )
}
