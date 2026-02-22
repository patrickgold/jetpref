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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import dev.patrickgold.jetpref.datastore.model.LocalTime
import dev.patrickgold.jetpref.datastore.model.PreferenceData
import dev.patrickgold.jetpref.datastore.model.PreferenceDataEvaluator
import dev.patrickgold.jetpref.datastore.model.PreferenceDataEvaluatorScope
import dev.patrickgold.jetpref.datastore.model.collectAsState
import dev.patrickgold.jetpref.datastore.ui.ColorPickerPreference
import dev.patrickgold.jetpref.datastore.ui.DialogSliderPreference
import dev.patrickgold.jetpref.datastore.ui.ListPreference
import dev.patrickgold.jetpref.datastore.ui.ListPreferenceEntry
import dev.patrickgold.jetpref.datastore.ui.LocalTimePickerPreference
import dev.patrickgold.jetpref.datastore.ui.PreferenceNavigationEntry
import dev.patrickgold.jetpref.datastore.ui.SwitchPreference
import dev.patrickgold.jetpref.datastore.ui.TextFieldPreference
import dev.patrickgold.jetpref.datastore.ui.maybeJetIcon
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.math.roundToInt
import kotlin.math.roundToLong
import kotlin.reflect.KClass

@DslMarker
@Target(AnnotationTarget.CLASS)
annotation class PreferenceComponentBuilderDslMarker

@PreferenceComponentBuilderDslMarker
open class PreferenceScreenBuilder(val kClass: KClass<*>) {
    internal var title: @Composable () -> String = { kClass.simpleName ?: "<unnamed screen>" }

    internal var summary: @Composable () -> String? = { null }

    internal var icon: @Composable () -> ImageVector? = { null }

    internal var components: List<PreferenceComponent>? = null

    internal var content: (@Composable () -> Unit)? = null

    fun title(block: @Composable () -> String) {
        title = block
    }

    fun summary(block: @Composable () -> String?) {
        summary = block
    }

    fun icon(block: @Composable () -> ImageVector?) {
        icon = block
    }

    fun components(block: PreferenceComponentScreenBuilder.() -> Unit) {
        require(components == null && content == null)
        val builder = PreferenceComponentScreenBuilder(level = 0)
        builder.block()
        components = builder.components.toList()
    }

    fun content(block: @Composable () -> Unit) {
        require(components == null && content == null)
        content = block
    }
}

@PreferenceComponentBuilderDslMarker
open class PreferenceComponentScreenBuilder(
    groupEnabledIf: PreferenceDataEvaluator? = null,
    groupVisibleIf: PreferenceDataEvaluator? = null,
    level: Int,
) : PreferenceComponentGroupBuilder(
    groupEnabledIf,
    groupVisibleIf,
    level,
) {
    @OptIn(ExperimentalContracts::class)
    inline fun group(
        noinline title: @Composable () -> String,
        noinline summary: @Composable () -> String? = { null },
        noinline icon: @Composable () -> ImageVector? = { null },
        noinline enabledIf: PreferenceDataEvaluator? = null,
        noinline visibleIf: PreferenceDataEvaluator? = null,
        block: PreferenceComponentGroupBuilder.() -> Unit,
    ) {
        contract {
            callsInPlace(block, InvocationKind.EXACTLY_ONCE)
        }
        val builder = PreferenceComponentGroupBuilder(combineEnabledIf(enabledIf), combineVisibleIf(visibleIf), level + 1)
        builder.groupHeader(title, summary, icon)
        builder.block()
        components.addAll(builder.components)
    }
}

@PreferenceComponentBuilderDslMarker
open class PreferenceComponentGroupBuilder(
    val groupEnabledIf: PreferenceDataEvaluator? = null,
    val groupVisibleIf: PreferenceDataEvaluator? = null,
    val level: Int,
) {
    internal var associatedGroup: PreferenceComponent.GroupHeader? = null

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

    @PublishedApi
    internal fun groupHeader(
        title: @Composable () -> String,
        summary: @Composable () -> String? = { null },
        icon: @Composable () -> ImageVector? = { null },
    ) {
        val component = object : PreferenceComponent.GroupHeader {
            override val id = PreferenceComponentId.next()
            override val title = title
            override val summary = summary
            override val icon = icon
            override val enabledIf = this@PreferenceComponentGroupBuilder.groupEnabledIf ?: { true }
            override val visibleIf = this@PreferenceComponentGroupBuilder.groupVisibleIf ?: { true }
            override val associatedGroup = this@PreferenceComponentGroupBuilder.associatedGroup
            override val level = this@PreferenceComponentGroupBuilder.level

            @Composable
            override fun Render() {
                if (visibleIf(PreferenceDataEvaluatorScope)) {
                    ListItem(
                        leadingContent = maybeJetIcon(icon.invoke()),
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
        associatedGroup = component
        components.add(component)
    }

    fun content(
        title: @Composable () -> String,
        summary: @Composable () -> String? = { null },
        icon: @Composable () -> ImageVector? = { null },
        enabledIf: PreferenceDataEvaluator = { true },
        visibleIf: PreferenceDataEvaluator = { true },
        content: @Composable () -> Unit,
    ) {
        val component = object : PreferenceComponent.ComposableContent {
            override val id = PreferenceComponentId.next()
            override val title = title
            override val summary = summary
            override val icon = icon
            override val enabledIf = combineEnabledIf(enabledIf)
            override val visibleIf = combineVisibleIf(visibleIf)
            override val content = content
            override val associatedGroup = this@PreferenceComponentGroupBuilder.associatedGroup
            override val level = this@PreferenceComponentGroupBuilder.level

            @Composable
            override fun Render() {
                content()
            }
        }
        components.add(component)
    }

    fun content(
        content: @Composable () -> Unit,
    ) {
        content(
            title = @Composable { "<anonymous content impl>" },
            content = content,
        )
    }

    fun navigationTo(
        targetScreen: PreferenceScreen,
        title: @Composable () -> String = targetScreen.title,
        summary: @Composable () -> String? = targetScreen.summary,
        icon: @Composable () -> ImageVector? = targetScreen.icon,
        enabledIf: PreferenceDataEvaluator = { true },
        visibleIf: PreferenceDataEvaluator = { true },
    ) {
        val component = object : PreferenceComponent.NavigationEntry {
            override val id = PreferenceComponentId.next()
            override val targetScreen = targetScreen
            override val title = title
            override val summary = summary
            override val icon = icon
            override val enabledIf = combineEnabledIf(enabledIf)
            override val visibleIf = combineVisibleIf(visibleIf)
            override val associatedGroup = this@PreferenceComponentGroupBuilder.associatedGroup
            override val level = this@PreferenceComponentGroupBuilder.level

            @Composable
            override fun Render() {
                PreferenceNavigationEntry(this)
            }
        }
        components.add(component)
    }

    fun switch(
        pref: PreferenceData<Boolean>,
        title: @Composable () -> String,
        summary: @Composable () -> String? = { null },
        icon: @Composable () -> ImageVector? = { null },
        enabledIf: PreferenceDataEvaluator = { true },
        visibleIf: PreferenceDataEvaluator = { true },
    ) {
        val component = object : PreferenceComponent.Switch {
            override val id = PreferenceComponentId.next()
            override val pref = pref
            override val title = title
            override val summary = summary
            override val icon = icon
            override val enabledIf = combineEnabledIf(enabledIf)
            override val visibleIf = combineVisibleIf(visibleIf)
            override val associatedGroup = this@PreferenceComponentGroupBuilder.associatedGroup
            override val level = this@PreferenceComponentGroupBuilder.level

            @Composable
            override fun Render() {
                SwitchPreference(this)
            }
        }
        components.add(component)
    }

    fun switch(
        pref: PreferenceData<Boolean>,
        title: @Composable () -> String,
        summaryOn: @Composable () -> String? = { null },
        summaryOff: @Composable () -> String? = { null },
        icon: @Composable () -> ImageVector? = { null },
        enabledIf: PreferenceDataEvaluator = { true },
        visibleIf: PreferenceDataEvaluator = { true },
    ) {
        switch(
            pref = pref,
            title = title,
            summary = {
                val prefValue by pref.collectAsState()
                when (prefValue) {
                    true -> summaryOn
                    false -> summaryOff
                }.invoke()
            },
            icon = icon,
            enabledIf = enabledIf,
            visibleIf = visibleIf,
        )
    }

    fun <V : Any> listPicker(
        listPref: PreferenceData<V>,
        title: @Composable () -> String,
        icon: @Composable () -> ImageVector? = { null },
        enabledIf: PreferenceDataEvaluator = { true },
        visibleIf: PreferenceDataEvaluator = { true },
        entries: List<ListPreferenceEntry<V>>,
    ) {
        val component = object : PreferenceComponent.ListPicker<V> {
            override val id = PreferenceComponentId.next()
            override val listPref = listPref
            override val title = title
            override val summary = @Composable {
                val listPrefValue by listPref.collectAsState()
                val entry = remember(listPrefValue) {
                    entries.find {
                        it.key == listPrefValue
                    }
                }
                entry?.label?.invoke() ?: "!! invalid !!"
            }
            override val icon = icon
            override val enabledIf = combineEnabledIf(enabledIf)
            override val visibleIf = combineVisibleIf(visibleIf)
            override val entries = entries
            override val associatedGroup = this@PreferenceComponentGroupBuilder.associatedGroup
            override val level = this@PreferenceComponentGroupBuilder.level

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
        summarySwitchDisabled: @Composable () -> String? = { null },
        icon: @Composable () -> ImageVector? = { null },
        enabledIf: PreferenceDataEvaluator = { true },
        visibleIf: PreferenceDataEvaluator = { true },
        entries: List<ListPreferenceEntry<V>>,
    ) {
        val component = object : PreferenceComponent.ListPickerWithSwitch<V> {
            override val id = PreferenceComponentId.next()
            override val listPref = listPref
            override val switchPref = switchPref
            override val title = title
            override val summary = @Composable {
                val listPrefValue by listPref.collectAsState()
                val switchPrefValue by switchPref.collectAsState()
                val entry = remember(listPrefValue) {
                    entries.find {
                        it.key == listPrefValue
                    }
                }
                when (switchPrefValue) {
                    false -> summarySwitchDisabled.invoke()
                    else -> entry?.label?.invoke() ?: "!! invalid !!"
                }
            }
            override val icon = icon
            override val enabledIf = combineEnabledIf(enabledIf)
            override val visibleIf = combineVisibleIf(visibleIf)
            override val entries = entries
            override val associatedGroup = this@PreferenceComponentGroupBuilder.associatedGroup
            override val level = this@PreferenceComponentGroupBuilder.level

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
        summary: @Composable () -> String? = { null },
        defaultValueLabel: @Composable () -> String? = { null },
        icon: @Composable () -> ImageVector? = { null },
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
            override val summary = summary
            override val icon = icon
            override val defaultValueLabel = defaultValueLabel
            override val showAlphaSlider = showAlphaSlider
            override val enableAdvancedLayout = enableAdvancedLayout
            override val defaultColors = defaultColors
            override val enabledIf = combineEnabledIf(enabledIf)
            override val visibleIf = combineVisibleIf(visibleIf)
            override val associatedGroup = this@PreferenceComponentGroupBuilder.associatedGroup
            override val level = this@PreferenceComponentGroupBuilder.level

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
        icon: @Composable () -> ImageVector? = { null },
        enabledIf: PreferenceDataEvaluator = { true },
        visibleIf: PreferenceDataEvaluator = { true },
    ) {
        val component = object : PreferenceComponent.LocalTimePicker {
            override val id = PreferenceComponentId.next()
            override val pref = pref
            override val title = title
            override val summary = @Composable { null }
            override val icon = icon
            override val enabledIf = combineEnabledIf(enabledIf)
            override val visibleIf = combineVisibleIf(visibleIf)
            override val associatedGroup = this@PreferenceComponentGroupBuilder.associatedGroup
            override val level = this@PreferenceComponentGroupBuilder.level

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
        summaryIfBlank: @Composable () -> String? = { null },
        summaryIfEmpty: @Composable () -> String? = { null },
        summary: @Composable (String) -> String? = { value ->
            when {
                value.isEmpty() -> summaryIfEmpty.invoke() ?: value
                value.isBlank() -> summaryIfBlank.invoke() ?: value
                else -> value
            }
        },
        icon: @Composable () -> ImageVector? = { null },
        transformValue: (String) -> String = { it },
        validateValue: (String) -> Unit = { },
        enabledIf: PreferenceDataEvaluator = { true },
        visibleIf: PreferenceDataEvaluator = { true },
    ) {
        val component = object : PreferenceComponent.TextField {
            override val id = PreferenceComponentId.next()
            override val pref = pref
            override val title = title
            override val summary = @Composable {
                val prefValue by pref.collectAsState()
                summary(prefValue)
            }
            override val icon = icon
            override val transformValue = transformValue
            override val validateValue = validateValue
            override val enabledIf = combineEnabledIf(enabledIf)
            override val visibleIf = combineVisibleIf(visibleIf)
            override val associatedGroup = this@PreferenceComponentGroupBuilder.associatedGroup
            override val level = this@PreferenceComponentGroupBuilder.level

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
        valueLabel: @Composable (V) -> String,
        summary: @Composable (V) -> String,
        icon: @Composable () -> ImageVector?,
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
            override val valueLabel = valueLabel
            override val summary = @Composable {
                val prefValue by pref.collectAsState()
                summary(prefValue)
            }
            override val icon = icon
            override val min = min
            override val max = max
            override val stepIncrement = stepIncrement
            override val enabledIf = combineEnabledIf(enabledIf)
            override val visibleIf = combineVisibleIf(visibleIf)
            override val level = this@PreferenceComponentGroupBuilder.level
            override val associatedGroup = this@PreferenceComponentGroupBuilder.associatedGroup
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
        noinline valueLabel: @Composable (V) -> String = { v -> v.toString() },
        noinline summary: @Composable (V) -> String = valueLabel,
        noinline icon: @Composable () -> ImageVector? = { null },
        min: V,
        max: V,
        stepIncrement: V,
        noinline enabledIf: PreferenceDataEvaluator = { true },
        noinline visibleIf: PreferenceDataEvaluator = { true },
    ) where V : Number, V : Comparable<V> {
        slider(
            pref = pref,
            title = title,
            valueLabel = valueLabel,
            summary = summary,
            icon = icon,
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
        pref1Label: @Composable () -> String,
        pref2Label: @Composable () -> String,
        valueLabel: @Composable (V) -> String,
        summary: @Composable (V, V) -> String,
        icon: @Composable () -> ImageVector?,
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
            override val pref1Label = pref1Label
            override val pref2Label = pref2Label
            override val valueLabel = valueLabel
            override val summary = @Composable {
                val pref1Value by pref1.collectAsState()
                val pref2Value by pref2.collectAsState()
                summary(pref1Value, pref2Value)
            }
            override val icon = icon
            override val min = min
            override val max = max
            override val stepIncrement = stepIncrement
            override val enabledIf = combineEnabledIf(enabledIf)
            override val visibleIf = combineVisibleIf(visibleIf)
            override val level = this@PreferenceComponentGroupBuilder.level
            override val associatedGroup = this@PreferenceComponentGroupBuilder.associatedGroup
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
        noinline pref1Label: @Composable () -> String,
        noinline pref2Label: @Composable () -> String,
        noinline valueLabel: @Composable (V) -> String = { v -> v.toString() },
        noinline summary: @Composable (V, V) -> String = { v1, v2 ->
            "${valueLabel.invoke(v1)} / ${valueLabel.invoke(v2)}"
        },
        noinline icon: @Composable () -> ImageVector? = { null },
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
            pref1Label = pref1Label,
            pref2Label = pref2Label,
            valueLabel = valueLabel,
            summary = summary,
            icon = icon,
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
        Int::class -> { value ->
            try {
                value.roundToInt()
            } catch (_: IllegalArgumentException) {
                value.toInt()
            } as V
        }
        Long::class -> { value ->
            try {
                value.roundToLong()
            } catch (_: IllegalArgumentException) {
                value.toLong()
            } as V
        }
        Float::class -> { value -> value as V }
        Double::class -> { value -> value.toDouble() as V }
        else -> error("Unknown subclass of V: ${V::class}")
    }
}
