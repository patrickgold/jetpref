package dev.patrickgold.jetpref.example.ui.settings

import dev.patrickgold.jetpref.datastore.component.PreferencePage
import dev.patrickgold.jetpref.datastore.ui.listPrefEntries
import dev.patrickgold.jetpref.example.ExamplePreferenceStore

data object SubPage : PreferencePage({
    title { "Sub Page" }

    val prefs by ExamplePreferenceStore
    components {
        listPicker(
            listPref = prefs.example.title,
            switchPref = prefs.example.showTitle,
            title = { "Some lengthy title about this entry some lengthy title about this entry." },
            summarySwitchDisabled = { "off" },
            entries = listPrefEntries {
                entry(
                    key = "str1",
                    label = { "String 1" },
                    description = { "Some lengthy description about this entry." },
                    showDescriptionOnlyIfSelected = true,
                )
                entry(
                    key = "str2",
                    label = { "String 2" },
                    description = { "Some lengthy description about this entry." },
                    showDescriptionOnlyIfSelected = true,
                )
                entry(
                    key = "str3",
                    label = { "String 3" },
                    description = { "Some lengthy description about this entry." },
                    showDescriptionOnlyIfSelected = true,
                )
            },
        )
    }
})
