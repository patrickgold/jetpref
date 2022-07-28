/*
 * Copyright 2022 Patrick Goldinger
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

// Suppress needed until https://youtrack.jetbrains.com/issue/KTIJ-19369 is fixed
@file:Suppress("DSL_SCOPE_VIOLATION")

plugins {
    alias(libs.plugins.agp.library)
    alias(libs.plugins.kotlin.android)
    id("maven-publish")
}

val jetprefCompileSdk: String by project
val jetprefMinSdk: String by project
val jetprefTargetSdk: String by project

val jetprefMavenGroupId: String by project
val jetprefJitpackGroupId: String by project
val jetprefVersion: String by project

android {
    compileSdk = jetprefCompileSdk.toInt()

    defaultConfig {
        minSdk = jetprefMinSdk.toInt()
        targetSdk = jetprefTargetSdk.toInt()
        consumerProguardFiles("proguard-rules.pro")
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.androidx.compose.compiler.get()
    }

    sourceSets {
        maybeCreate("main").apply {
            java {
                srcDirs("src/main/kotlin")
            }
        }
    }

    publishing {
        singleVariant("release")
    }
}

dependencies {
    implementation(libs.androidx.compose.material)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)

    debugImplementation(libs.androidx.compose.ui.tooling)
}

val sourcesJar = tasks.register<Jar>("sourcesJar") {
    archiveClassifier.set("sources")
    from(android.sourceSets.getByName("main").java.srcDirs)
}

group = jetprefJitpackGroupId
version = jetprefVersion

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("materialUiRelease").apply {
                from(components.findByName("release"))
                artifact(sourcesJar)

                groupId = jetprefMavenGroupId
                artifactId = "jetpref-material-ui"
                version = jetprefVersion

                pom {
                    name.set("JetPref Material UI")
                    description.set("Material components for JetPref and general use, written in Jetpack Compose.")
                    url.set("https://patrickgold.dev/jetpref")
                    licenses {
                        license {
                            name.set("The Apache License, Version 2.0")
                            url.set("http://www.apache.org/licenses/LICENSE-2.0")
                        }
                    }
                    developers {
                        developer {
                            id.set("patrickgold")
                            name.set("Patrick Goldinger")
                            email.set("patrick@patrickgold.dev")
                        }
                    }
                    scm {
                        connection.set("scm:git:https://github.com/patrickgold/jetpref/")
                        developerConnection.set("scm:git:https://github.com/patrickgold/jetpref/")
                        url.set("https://github.com/patrickgold/jetpref/")
                    }
                }
            }
        }
    }
}
