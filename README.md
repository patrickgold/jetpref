# JetPref [![](https://jitpack.io/v/dev.patrickgold/jetpref.svg)](https://jitpack.io/#dev.patrickgold/jetpref)

A preference library (custom data handling + UI in JetPack Compose) mainly developed for use in
[FlorisBoard](https://github.com/florisboard/florisboard). Currently in alpha/early-beta phase.

## Usage

Disclaimer: This library is still in early alpha and therefore not recommended for production use.
It is currently tested out in the beta channel of FlorisBoard, so bugs or other issues
in runtime use can be found and fixed. Feel free to ask if you need help using this library
or to file feature requests / bug reports in the issue's page!

If you want to play around with this library, here's how to include JetPref in your app:

First, add JitPack to the list of repositories in your global `build.gradle.kts`:

```kts
subprojects {
    repositories {
        maven { url = uri("https://jitpack.io") }
    }
}
```

Then in your app module `build.gradle.kts`, add the dependencies:

```kt
// JetPref Datastore module
implementation("dev.patrickgold.jetpref:jetpref-datastore-model:$version")

// JetPref Datastore UI module
implementation("dev.patrickgold.jetpref:jetpref-datastore-ui:$version")

// JetPref Material UI components module (optional)
implementation("dev.patrickgold.jetpref:jetpref-material-ui:$version")
```

This library is experimental, there's currently limited documentation. It is best
if you look at the example app or the source code and see how the library is used,
but especially for the `jetpref-material-ui` artifact there's already good documentation
available in the source files.

A separate documentation page (with dokka) and better tutorials / samples are planned once
more code is documented and the API stabilizes.

## License
```
Copyright 2021 Patrick Goldinger

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
