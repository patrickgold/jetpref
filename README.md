# JetPref [![](https://jitpack.io/v/dev.patrickgold/jetpref.svg)](https://jitpack.io/#dev.patrickgold/jetpref)

A preference library (custom data handling + UI in JetPack Compose) for Android 6.0+ mainly developed for use in
[FlorisBoard](https://github.com/florisboard/florisboard). Currently in early-beta phase.

Disclaimer: This library is still in early-beta and therefore should only be used with caution in production code. It is
currently tested out in FlorisBoard, so bugs or other issues in runtime use can be found and fixed. Feel free to ask if
you need help using this library or to file feature requests / bug reports in the issue's page!

## Importing the library

### Since 0.1.0-beta14

JetPref is hosted on Maven Central:

```kts
subprojects {
    repositories {
        mavenCentral()
    }
}
```

### Until 0.1.0-beta14

JetPref dependencies were hosted on [JitPack](https://jitpack.io/#dev.patrickgold/jetpref). Thus the JitPack
repository needs to be added in your global repository config first:

```kts
subprojects {
    repositories {
        maven { url = uri("https://jitpack.io") }
    }
}
```

### Adding the dependencies

Then in your app module `build.gradle.kts`, add the dependencies:

```kt
// JetPref Datastore module
implementation("dev.patrickgold.jetpref:jetpref-datastore-model:$version")

// JetPref Datastore UI module
implementation("dev.patrickgold.jetpref:jetpref-datastore-ui:$version")

// JetPref Material UI components module (optional)
implementation("dev.patrickgold.jetpref:jetpref-material-ui:$version")
```

## Usage

### Set up model and initializer

First, you need to define the preference model of your app. Preference models are the core logic of this library and
manage loading the preferences from storage, persisting any changes back to storage and to manage observers on each
preference.

You can define different preference entries in a model, each having a `key`, `default value` and `serializer`. For
primitive types and enums a default serializer is already provided. For all other types (`custom`) a serializer must be
provided by you, which (de-)serializes the custom data value from/to a string.

Example preference model file with comments:

```kotlin
// AppPrefs.kt

// Defining a getter function for easy retrieval of the AppPrefs model.
// You can name this however you want, the convention is <projectName>PreferenceModel
fun examplePreferenceModel() = JetPref.getOrCreatePreferenceModel(AppPrefs::class, ::AppPrefs)

// Defining a preference model for our app prefs
// The name we give here is the file name of the preferences and is saved
// within the app's `jetpref_datastore` directory.
class AppPrefs : PreferenceModel("example-app-preferences") {
    val showExampleGroup = boolean(
        key = "show_example_group",
        default = true,
    )
    val boxSizePortrait = int(
        key = "box_size_portrait",
        default = 40,
    )
    val boxSizeLandscape = int(
        key = "box_size_landscape",
        default = 20,
    )
    val welcomeMessage = string(
        key = "welcome_message",
        default = "Hello world!",
    )
    val theme = enum(
        key = "theme",
        default = Theme.AUTO,
    )

    /* ... */
}
```

Next, we need to extend the default `Application` class to be able to initialize the model, configure `JetPref` and
define the custom application in the `manifest.xml` file. Should you already have a custom application class you can
insert the config and initializer into your existing one.

```kotlin
// ExampleApplication.kt

class ExampleApplication : Application() {
    private val prefs by examplePreferenceModel()

    override fun onCreate() {
        super.onCreate()

        // Optionally initialize global JetPref configs. This must be done before
        // any preference datastore is initialized!
        JetPref.configure(
            saveIntervalMs = 500,
            encodeDefaultValues = true,
        )

        // Initialize your datastore here (required)
        prefs.initializeBlocking(this)
    }
}
```

```xml
<!-- manifest.xml -->
<!-- ... -->
<application android:name=".ExampleApplication" android:label="@string/app_name">
<!-- ... -->
</application>
<!-- ... -->
```

### Using the preference model

Throughout your code base you can now use the preference model wherever you need it:

```kotlin
// Example.kt

// Get a reference to the preference model
val prefs by examplePreferenceModel()

// Read a preference value manually
prefs.preferenceName.get()

// Write a preference value manually
prefs.preferenceName.set(value)

// Resets a preference value back to default value (`null` internally)
prefs.preferenceName.reset()

// Observe a preference value which automatically removes observer if the lifecycle stops
prefs.preferenceName.observe(lifecycleOwner) { newValue ->
    // Do something with it
}

// Observe a preference value forever, requires manual removal of observer
prefs.preferenceName.observeForever { newValue ->
    // Do something with it
}

// Observe a preference value as a Jetpack Compose state with automatic disposal
val myPreference by prefs.preferenceName.observeAsState()
```

### Write a simple Settings UI in Jetpack Compose

This section assumes you already have set up Jetpack Compose properly and have prior basic experience with it.

JetPref provides a handful of pre-configured and ready Material preference widgets:

- [`Preference`](datastore-ui/src/main/kotlin/dev/patrickgold/jetpref/datastore/ui/Preference.kt): Widget without a
  backing preference data, which allows to have a custom UI element with the same semantics and behavior of a normal
  preference widget.
- [`SwitchPreference`](datastore-ui/src/main/kotlin/dev/patrickgold/jetpref/datastore/ui/SwitchPreference.kt): Widget
  which is backed by a boolean preference data and draws a switch on the end, representing the current state.
- [`ListPreference`](datastore-ui/src/main/kotlin/dev/patrickgold/jetpref/datastore/ui/ListPreference.kt): Widget which
  is backed by a preference data with any value and allows to choose from different pre-set values ina list-style format
  within a dialog. Optionally this can also be combined with an additional boolean preference data backer, which adds a
  switch in the same dialog as the list.
- [`DialogSliderPreference`](datastore-ui/src/main/kotlin/dev/patrickgold/jetpref/datastore/ui/DialogSliderPreference.kt):
  Widget is is backed by one or two numeric preference data fields and which provides a dialog slider for each data
  field.

Example Settings UI screen (detailed docs are provided through the docstrings of each widget):

```kotlin
// ExampleSettingsScreen.kt

@Composable
fun ExampleSettingsScreen() = ScrollablePreferenceLayout(examplePreferenceModel()) {
    Preference(
        onClick = { Log.d("example", "Custom preference clicked!") },
        title = "Custom preference",
    )
    ListPreference(
        prefs.theme,
        title = "Theme",
        entries = Theme.listEntries(),
    )
    DialogSliderPreference(
        primaryPref = prefs.boxSizePortrait,
        secondaryPref = prefs.boxSizeLandscape,
        title = "Example integer slider",
        valueLabel = { if (it == -1) "Automatic" else "$it%" },
        primaryLabel = "Portrait",
        secondaryLabel = "Landscape",
        min = -1,
        max = 100,
        stepIncrement = 1,
    )
    SwitchPreference(
        prefs.showExampleGroup,
        iconId = R.drawable.ic_question_answer_black_24dp,
        title = "Show example group",
        summary = "Show/hide the example group",
    )
    PreferenceGroup(title = "Example group", visibleIf = { prefs.showExampleGroup isEqualTo true }) {
        SwitchPreference(
            prefs.example.isButtonShowing,
            title = "isBtnShow",
        )
        SwitchPreference(
            prefs.example.isButtonShowing2,
            iconId = R.drawable.ic_question_answer_black_24dp,
            title = "isBtnShow2",
            summaryOn = "Hello",
            summaryOff = "Bye",
            enabledIf = { prefs.example.isButtonShowing isEqualTo true },
        )
        DialogSliderPreference(
            prefs.example.buttonSize,
            title = "Button Size",
            valueLabel = { "$it%" },
            min = 0,
            max = 100,
            stepIncrement = 1,
        )
        DialogSliderPreference(
            prefs.example.buttonWidth,
            title = "Button Size",
            valueLabel = { "$it dp" },
            min = 0,
            max = 100,
            stepIncrement = 5,
        )
        DialogSliderPreference(
            prefs.example.mainFontSize,
            title = "Main Font Size",
            valueLabel = { "$it sp" },
            min = 0.0,
            max = 100.0,
            stepIncrement = 5.0,
        )
        DialogSliderPreference(
            prefs.example.fontSize,
            title = "Font Size",
            valueLabel = { "$it sp" },
            min = 0.0f,
            max = 100.0f,
            stepIncrement = 5.0f,
        )
    }
    ListPreference(
        prefs.example.title,
        title = "Some lengthy title about this entry some lengthy title about this entry.",
        entries = listPrefEntries {
            entry(
                key = "str1",
                label = "String 1",
                description = "Some lengthy description about this entry.",
                showDescriptionOnlyIfSelected = true,
            )
            entry(
                key = "str2",
                label = "String 2",
                description = "Some lengthy description about this entry.",
                showDescriptionOnlyIfSelected = true,
            )
            entry(
                key = "str3",
                label = "String 3",
                description = "Some lengthy description about this entry.",
                showDescriptionOnlyIfSelected = true,
            )
        },
    )
    ListPreference(
        listPref = prefs.example.title,
        switchPref = prefs.example.showTitle,
        title = "Some lengthy title about this entry some lengthy title about this entry.",
        summarySwitchDisabled = "off",
        entries = listPrefEntries {
            entry(
                key = "str1",
                label = "String 1",
                description = "Some lengthy description about this entry.",
                showDescriptionOnlyIfSelected = true,
            )
            entry(
                key = "str2",
                label = "String 2",
                description = "Some lengthy description about this entry.",
                showDescriptionOnlyIfSelected = true,
            )
            entry(
                key = "str3",
                label = "String 3",
                description = "Some lengthy description about this entry.",
                showDescriptionOnlyIfSelected = true,
            )
        },
    )
}
```

### Migration of old preference data

JetPref supports basic migration of preference data out-of-the-box. To migrate data, override the `migrate()` method in
the `PreferenceModel` class:

```kotlin
class AppPrefs : PreferenceModel("example-app-preferences") {
    /* ... */

    override fun migrate(entry: PreferenceMigrationEntry): PreferenceMigrationEntry {
        return when {
            // Given migration example situation: The app theme was previously saved as either AUTO, DAY or NIGHT, but
            // since then it has changed to AUTO, LIGHT and DARK. As such we need to transform the DAY and NIGHT values.
            entry.key == "theme" && entry.rawValue == "DAY" -> entry.transform(rawValue = Theme.LIGHT.toString())
            entry.key == "theme" && entry.rawValue == "NIGHT" -> entry.transform(rawValue = Theme.DARK.toString())

            // Given migration example situation: We renamed a preference.
            entry.key == "show_group" -> entry.transform(key = "show_example_group")

            // Given migration example situation: We expanded and renamed a simple switch pref to a list pref
            entry.key == "foo_box_enabled" -> entry.transform(
                type = PreferenceType.string(), // Important: we change the type and thus must set the new one!
                key = "foo_box_mode", // New key
                rawValue = if (entry.rawValue.toBoolean()) "ENABLED_COLLAPSING_MODE" else "DISABLED", // New value
            )

            // Given migration example situation: We changed the value format of a pref and want to reset the pref
            // value it is in the old format (e.g. if there's a certain character in it, you can also use regex...)
            // You could also provide a new value directly via transform(), using reset() however guarantees to reset
            // it back to the default value you set in the preference entry.
            entry.key == "foo_box_names" && entry.rawValue.contains("#") -> entry.reset()

            // If we have a pref that does not exist nor is needed anymore we need to do nothing, the delete happens
            // automatically!

            // By default we keep each entry as is (you could also return entry directly but this is more readable)
            else -> entry.keepAsIs()
        }
    }
}
```

### Material UI widgets

JetPref additionally provides ready-to-use custom Material components Jetpack Compose misses through the optional
`dev.patrickgold.jetpref:jetpref-material-ui` package:

- [`JetPrefAlertDialog`](material-ui/src/main/kotlin/dev/patrickgold/jetpref/material/ui/JetPrefAlertDialog.kt):
  Advanced and easy-to-use alert dialog, based on Jetpack Compose's `Dialog`.
- [`JetPrefColorPicker`](material-ui/src/main/kotlin/dev/patrickgold/jetpref/material/ui/JetPrefColorPicker.kt): Simple
  but effective HSV color picker.
- [`JetPrefListItem`](material-ui/src/main/kotlin/dev/patrickgold/jetpref/material/ui/JetPrefListItem.kt): Compatibility
  and ease-of use for a list item.

The Material UI package can be used independently from JetPref too, if you only need one of the above components!

## Additional notes

This library is experimental, there's currently limited documentation. Additional examples can be found in the
[example app](https://github.com/patrickgold/jetpref/tree/main/example/src/main/kotlin/dev/patrickgold/jetpref/example)
or by browsing the source code.

A separate documentation page (with dokka) for the API and improved tutorials / samples are planned in the future.

## License

```
Copyright 2021-2022 Patrick Goldinger

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
