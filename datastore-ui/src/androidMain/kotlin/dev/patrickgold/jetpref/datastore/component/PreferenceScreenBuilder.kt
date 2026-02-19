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

package dev.patrickgold.jetpref.datastore.component

import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import dev.patrickgold.jetpref.datastore.model.LocalTime
import dev.patrickgold.jetpref.datastore.model.PreferenceData
import dev.patrickgold.jetpref.datastore.model.PreferenceDataEvaluator
import dev.patrickgold.jetpref.datastore.model.PreferenceDataEvaluatorScope
import dev.patrickgold.jetpref.datastore.ui.ColorPickerPreference
import dev.patrickgold.jetpref.datastore.ui.DialogSliderPreference
import dev.patrickgold.jetpref.datastore.ui.ListPreference
import dev.patrickgold.jetpref.datastore.ui.ListPreferenceEntry
import dev.patrickgold.jetpref.datastore.ui.LocalTimePickerPreference
import dev.patrickgold.jetpref.datastore.ui.NavigationEntryPreference
import dev.patrickgold.jetpref.datastore.ui.SwitchPreference
import dev.patrickgold.jetpref.datastore.ui.TextFieldPreference
import dev.patrickgold.jetpref.datastore.ui.maybeJetIcon
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.reflect.KClass

@DslMarker
@Target(AnnotationTarget.CLASS)
annotation class PreferenceComponentBuilderDslMarker

@PreferenceComponentBuilderDslMarker
class PreferenceScreenBuilder(val kClass: KClass<*>) {
    internal var title: @Composable () -> String = { kClass.simpleName ?: "<unnamed screen>" }

    internal var icon: (@Composable () -> ImageVector)? = null

    internal var components: List<PreferenceComponent>? = null

    internal var content: (@Composable () -> Unit)? = null

    fun title(block: @Composable () -> String) {
        title = block
    }

    fun icon(block: @Composable () -> ImageVector) {
        icon = block
    }

    fun components(block: PreferenceComponentListBuilder.() -> Unit) {
        require(components == null && content == null)
        val builder = PreferenceComponentListBuilder(level = 0)
        builder.block()
        components = builder.components.toList()
    }

    fun content(block: @Composable () -> Unit) {
        require(components == null && content == null)
        content = block
    }
}

@PreferenceComponentBuilderDslMarker
open class PreferenceComponentListBuilder(
    val groupEnabledIf: PreferenceDataEvaluator? = null,
    val groupVisibleIf: PreferenceDataEvaluator? = null,
    val level: Int,
) {
    @PublishedApi
    internal val components = mutableListOf<PreferenceComponent>()

    @PublishedApi
    internal fun combineEvaluators(
        groupEvaluator: PreferenceDataEvaluator?,
        localEvaluator: PreferenceDataEvaluator?,
    ): PreferenceDataEvaluator {
        return when {
            groupEvaluator != null && localEvaluator != null -> {
                ({ groupEvaluator(PreferenceDataEvaluatorScope) && localEvaluator(PreferenceDataEvaluatorScope) })
            }
            groupEvaluator != null -> groupEvaluator
            localEvaluator != null -> localEvaluator
            else -> ({ true })
        }
    }

    @PublishedApi
    internal fun combineEnabledIf(enabledIf: PreferenceDataEvaluator?) = combineEvaluators(groupEnabledIf, enabledIf)

    @PublishedApi
    internal fun combineVisibleIf(visibleIf: PreferenceDataEvaluator?) = combineEvaluators(groupVisibleIf, visibleIf)

    @OptIn(ExperimentalContracts::class)
    inline fun group(
        noinline title: @Composable () -> String,
        noinline icon: (@Composable () -> ImageVector)? = null,
        noinline enabledIf: PreferenceDataEvaluator? = null,
        noinline visibleIf: PreferenceDataEvaluator? = null,
        block: PreferenceComponentListBuilder.() -> Unit,
    ) {
        contract {
            callsInPlace(block, InvocationKind.EXACTLY_ONCE)
        }
        val builder = PreferenceComponentListBuilder(combineEnabledIf(enabledIf), combineVisibleIf(visibleIf), level + 1)
        builder.groupTitle(title, icon)
        builder.block()
        components.addAll(builder.components)
    }

    @PublishedApi
    internal fun groupTitle(
        title: @Composable () -> String,
        icon: (@Composable () -> ImageVector)? = null,
    ) {
        val component = object : PreferenceComponent.GroupTitle {
            override val id = PreferenceComponentId.next()
            override val title = title
            override val icon = icon
            override val enabledIf = this@PreferenceComponentListBuilder.groupEnabledIf ?: { true }
            override val visibleIf = this@PreferenceComponentListBuilder.groupVisibleIf ?: { true }
            override val level = this@PreferenceComponentListBuilder.level

            @Composable
            override fun Render() {
                if (visibleIf(PreferenceDataEvaluatorScope)) {
                    ListItem(
                        leadingContent = maybeJetIcon(icon?.invoke()),
                        headlineContent = {
                            Text(
                                text = title.invoke(),
                                color = MaterialTheme.colorScheme.secondary,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        },
                    )
                }
            }
        }
        components.add(component)
    }

    fun composableContent(
        enabledIf: PreferenceDataEvaluator = { true },
        visibleIf: PreferenceDataEvaluator = { true },
        content: @Composable () -> Unit,
    ) {
        val component = object : PreferenceComponent.ComposableContent {
            override val id = PreferenceComponentId.next()
            override val title = @Composable { "<generic component impl>" }
            override val icon = null
            override val enabledIf = combineEnabledIf(enabledIf)
            override val visibleIf = combineVisibleIf(visibleIf)
            override val content = content
            override val level = this@PreferenceComponentListBuilder.level

            @Composable
            override fun Render() {
                content()
            }
        }
        components.add(component)
    }

    fun navigationTo(
        targetScreen: PreferenceScreen,
        title: @Composable () -> String = targetScreen.title,
        icon: (@Composable () -> ImageVector)? = targetScreen.icon,
        summary: (@Composable () -> String)? = null,
        enabledIf: PreferenceDataEvaluator = { true },
        visibleIf: PreferenceDataEvaluator = { true },
    ) {
        val component = object : PreferenceComponent.NavigationEntry {
            override val id = PreferenceComponentId.next()
            override val targetScreen = targetScreen
            override val title = title
            override val icon = icon
            override val summary = summary
            override val enabledIf = combineEnabledIf(enabledIf)
            override val visibleIf = combineVisibleIf(visibleIf)
            override val level = this@PreferenceComponentListBuilder.level

            @Composable
            override fun Render() {
                NavigationEntryPreference(this)
            }
        }
        components.add(component)
    }

    fun switch(
        pref: PreferenceData<Boolean>,
        title: @Composable () -> String,
        icon: (@Composable () -> ImageVector)? = null,
        summary: (@Composable () -> String)? = null,
        summaryOn: (@Composable () -> String)? = null,
        summaryOff: (@Composable () -> String)? = null,
        enabledIf: PreferenceDataEvaluator = { true },
        visibleIf: PreferenceDataEvaluator = { true },
    ) {
        val component = object : PreferenceComponent.Switch {
            override val id = PreferenceComponentId.next()
            override val pref = pref
            override val title = title
            override val icon = icon
            override val summary = summary
            override val summaryOn = summaryOn
            override val summaryOff = summaryOff
            override val enabledIf = combineEnabledIf(enabledIf)
            override val visibleIf = combineVisibleIf(visibleIf)
            override val level = this@PreferenceComponentListBuilder.level

            @Composable
            override fun Render() {
                SwitchPreference(this)
            }
        }
        components.add(component)
    }

    fun <V : Any> listPicker(
        listPref: PreferenceData<V>,
        title: @Composable () -> String,
        icon: (@Composable () -> ImageVector)? = null,
        enabledIf: PreferenceDataEvaluator = { true },
        visibleIf: PreferenceDataEvaluator = { true },
        entries: List<ListPreferenceEntry<V>>,
    ) {
        val component = object : PreferenceComponent.ListPicker<V> {
            override val id = PreferenceComponentId.next()
            override val listPref = listPref
            override val title = title
            override val icon = icon
            override val enabledIf = combineEnabledIf(enabledIf)
            override val visibleIf = combineVisibleIf(visibleIf)
            override val entries = entries
            override val level = this@PreferenceComponentListBuilder.level

            @Composable
            override fun Render() {
                ListPreference(this)
            }
        }
        components.add(component)
    }

    fun <V : Any> listPicker(
        listPref: PreferenceData<V>,
        switchPref: PreferenceData<Boolean>,
        title: @Composable () -> String,
        icon: (@Composable () -> ImageVector)? = null,
        summarySwitchDisabled: (@Composable () -> String)? = null,
        enabledIf: PreferenceDataEvaluator = { true },
        visibleIf: PreferenceDataEvaluator = { true },
        entries: List<ListPreferenceEntry<V>>,
    ) {
        val component = object : PreferenceComponent.ListPickerWithSwitch<V> {
            override val id = PreferenceComponentId.next()
            override val listPref = listPref
            override val switchPref = switchPref
            override val title = title
            override val icon = icon
            override val summarySwitchDisabled = summarySwitchDisabled
            override val enabledIf = combineEnabledIf(enabledIf)
            override val visibleIf = combineVisibleIf(visibleIf)
            override val entries = entries
            override val level = this@PreferenceComponentListBuilder.level

            @Composable
            override fun Render() {
                ListPreference(this)
            }
        }
        components.add(component)
    }

    fun colorPicker(
        pref: PreferenceData<Color>,
        title: @Composable () -> String,
        icon: (@Composable () -> ImageVector)? = null,
        summary: (@Composable () -> String)? = null,
        defaultValueLabel: (@Composable () -> String)? = null,
        showAlphaSlider: Boolean = false,
        enableAdvancedLayout: Boolean = false,
        defaultColors: List<Color>,
        enabledIf: PreferenceDataEvaluator = { true },
        visibleIf: PreferenceDataEvaluator = { true },
    ) {
        val component = object : PreferenceComponent.ColorPicker {
            override val id = PreferenceComponentId.next()
            override val pref = pref
            override val title = title
            override val icon = icon
            override val summary = summary
            override val defaultValueLabel = defaultValueLabel
            override val showAlphaSlider = showAlphaSlider
            override val enableAdvancedLayout = enableAdvancedLayout
            override val defaultColors = defaultColors
            override val enabledIf = combineEnabledIf(enabledIf)
            override val visibleIf = combineVisibleIf(visibleIf)
            override val level = this@PreferenceComponentListBuilder.level

            @Composable
            override fun Render() {
                ColorPickerPreference(this)
            }
        }
        components.add(component)
    }

    fun localTimePicker(
        pref: PreferenceData<LocalTime>,
        title: @Composable () -> String,
        icon: (@Composable () -> ImageVector)? = null,
        enabledIf: PreferenceDataEvaluator = { true },
        visibleIf: PreferenceDataEvaluator = { true },
    ) {
        val component = object : PreferenceComponent.LocalTimePicker {
            override val id = PreferenceComponentId.next()
            override val pref = pref
            override val title = title
            override val icon = icon
            override val enabledIf = combineEnabledIf(enabledIf)
            override val visibleIf = combineVisibleIf(visibleIf)
            override val level = this@PreferenceComponentListBuilder.level

            @Composable
            override fun Render() {
                LocalTimePickerPreference(this)
            }
        }
        components.add(component)
    }

    fun textField(
        pref: PreferenceData<String>,
        title: @Composable () -> String,
        icon: (@Composable () -> ImageVector)? = null,
        summaryIfBlank: (@Composable () -> String)? = null,
        summaryIfEmpty: (@Composable () -> String)? = null,
        summary: @Composable (String) -> String? = { v ->
            when {
                v.isEmpty() -> summaryIfEmpty?.invoke() ?: v
                v.isBlank() -> summaryIfBlank?.invoke() ?: v
                else -> v
            }
        },
        transformValue: (String) -> String = { it },
        validateValue: (String) -> Unit = { },
        enabledIf: PreferenceDataEvaluator = { true },
        visibleIf: PreferenceDataEvaluator = { true },
    ) {
        val component = object : PreferenceComponent.TextField {
            override val id = PreferenceComponentId.next()
            override val pref = pref
            override val title = title
            override val icon = icon
            override val summaryIfBlank = summaryIfBlank
            override val summaryIfEmpty = summaryIfEmpty
            override val summary = summary
            override val transformValue = transformValue
            override val validateValue = validateValue
            override val enabledIf = combineEnabledIf(enabledIf)
            override val visibleIf = combineVisibleIf(visibleIf)
            override val level = this@PreferenceComponentListBuilder.level

            @Composable
            override fun Render() {
                TextFieldPreference(this)
            }
        }
        components.add(component)
    }

    @PublishedApi
    internal fun <V> slider(
        pref: PreferenceData<V>,
        title: @Composable () -> String,
        icon: (@Composable () -> ImageVector)?,
        valueLabel: @Composable (V) -> String,
        summary: @Composable (V) -> String,
        min: V,
        max: V,
        stepIncrement: V,
        enabledIf: PreferenceDataEvaluator,
        visibleIf: PreferenceDataEvaluator,
        convertToV: (Float) -> V,
    ) where V : Number, V : Comparable<V> {
        val component = object : PreferenceComponent.SingleSlider<V> {
            override val id = PreferenceComponentId.next()
            override val pref = pref
            override val title = title
            override val icon = icon
            override val valueLabel = valueLabel
            override val summary = summary
            override val min = min
            override val max = max
            override val stepIncrement = stepIncrement
            override val enabledIf = combineEnabledIf(enabledIf)
            override val visibleIf = combineVisibleIf(visibleIf)
            override val level = this@PreferenceComponentListBuilder.level
            override val convertToV = convertToV

            @Composable
            override fun Render() {
                DialogSliderPreference(this)
            }
        }
        components.add(component)
    }

    inline fun <reified V> slider(
        pref: PreferenceData<V>,
        noinline title: @Composable () -> String,
        noinline icon: (@Composable () -> ImageVector)? = null,
        noinline valueLabel: @Composable (V) -> String = { v -> v.toString() },
        noinline summary: @Composable (V) -> String = valueLabel,
        min: V,
        max: V,
        stepIncrement: V,
        noinline enabledIf: PreferenceDataEvaluator = { true },
        noinline visibleIf: PreferenceDataEvaluator = { true },
    ) where V : Number, V : Comparable<V> {
        slider(
            pref = pref,
            title = title,
            icon = icon,
            valueLabel = valueLabel,
            summary = summary,
            min = min,
            max = max,
            stepIncrement = stepIncrement,
            enabledIf = enabledIf,
            visibleIf = visibleIf,
            convertToV = selectConverter()
        )
    }

    @PublishedApi
    internal fun <V> dualSlider(
        pref1: PreferenceData<V>,
        pref2: PreferenceData<V>,
        title: @Composable () -> String,
        icon: (@Composable () -> ImageVector)?,
        pref1Label: @Composable () -> String,
        pref2Label: @Composable () -> String,
        valueLabel: @Composable (V) -> String,
        summary: @Composable (V, V) -> String,
        min: V,
        max: V,
        stepIncrement: V,
        enabledIf: PreferenceDataEvaluator,
        visibleIf: PreferenceDataEvaluator,
        convertToV: (Float) -> V,
    ) where V : Number, V : Comparable<V> {
        val component = object : PreferenceComponent.DualSlider<V> {
            override val id = PreferenceComponentId.next()
            override val pref1 = pref1
            override val pref2 = pref2
            override val title = title
            override val icon = icon
            override val pref1Label = pref1Label
            override val pref2Label = pref2Label
            override val valueLabel = valueLabel
            override val summary = summary
            override val min = min
            override val max = max
            override val stepIncrement = stepIncrement
            override val enabledIf = combineEnabledIf(enabledIf)
            override val visibleIf = combineVisibleIf(visibleIf)
            override val level = this@PreferenceComponentListBuilder.level
            override val convertToV = convertToV

            @Composable
            override fun Render() {
                DialogSliderPreference(this)
            }
        }
        components.add(component)
    }

    inline fun <reified V> dualSlider(
        pref1: PreferenceData<V>,
        pref2: PreferenceData<V>,
        noinline title: @Composable () -> String,
        noinline icon: (@Composable () -> ImageVector)? = null,
        noinline pref1Label: @Composable () -> String,
        noinline pref2Label: @Composable () -> String,
        noinline valueLabel: @Composable (V) -> String = { v -> v.toString() },
        noinline summary: @Composable (V, V) -> String = { v1, v2 ->
            "${valueLabel.invoke(v1)} / ${valueLabel.invoke(v2)}"
        },
        min: V,
        max: V,
        stepIncrement: V,
        noinline enabledIf: PreferenceDataEvaluator = { true },
        noinline visibleIf: PreferenceDataEvaluator = { true },
    ) where V : Number, V : Comparable<V> {
        dualSlider(
            pref1 = pref1,
            pref2 = pref2,
            title = title,
            icon = icon,
            pref1Label = pref1Label,
            pref2Label = pref2Label,
            valueLabel = valueLabel,
            summary = summary,
            min = min,
            max = max,
            stepIncrement = stepIncrement,
            enabledIf = enabledIf,
            visibleIf = visibleIf,
            convertToV = selectConverter()
        )
    }
}

@PublishedApi
internal inline fun <reified V : Number> selectConverter(): (Float) -> V {
    return when (V::class) {
        Int::class -> { v -> v.toInt() as V }
        Long::class -> { v -> v.toLong() as V }
        Float::class -> { v -> v as V }
        Double::class -> { v -> v.toDouble() as V }
        else -> error("Unknown subclass of V: ${V::class}")
    }
}
