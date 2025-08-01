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

package dev.patrickgold.jetpref.datastore.runtime

/**
 * Exception indicating that the requested preference model could not be found.
 *
 * Hint: most likely, this either means you forgot to annotate the model with
 * [dev.patrickgold.jetpref.datastore.annotations.Preferences], or you did not configure
 * KSP correctly in your `build.gradle.kts`.
 *
 * @since 0.3.0
 */
class PreferenceModelNotFoundException(
    modelQualifiedName: String,
    cause: Throwable,
) : Exception(
    "No preference model with qualified name '$modelQualifiedName' could be found",
    cause,
)

/**
 * Exception indicating that a preference model contains duplicate keys.
 *
 * Hint: have a look at your model's entries and remove duplicate keys. A common pitfall are
 * entries with the same key but different types, these are counted as duplicates!
 *
 * @since 0.3.0
 */
class PreferenceModelDuplicateKeyException(
    modelQualifiedName: String,
    duplicates: Map<String, List<String>>,
) : Exception(
    buildString {
        appendLine("Preference model '$modelQualifiedName' contains duplicate keys $duplicates")
    },
)
