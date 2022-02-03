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

plugins {
    id("com.android.library")
    id("kotlin-android")
    id("maven-publish")
}

val jetprefCompileSdk: String by project
val jetprefMinSdk: String by project
val jetprefTargetSdk: String by project

val jetprefMavenGroupId: String by project
val jetprefJitpackGroupId: String by project
val jetprefVersion: String by project

val composeVersion: String by project

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
        freeCompilerArgs = listOf("-Xopt-in=kotlin.RequiresOptIn")
    }

    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = composeVersion
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
    implementation("androidx.activity:activity-compose:1.4.0")
    implementation("androidx.appcompat:appcompat:1.4.0")
    implementation("androidx.compose.material:material:$composeVersion")
    implementation("androidx.compose.ui:ui:$composeVersion")
    implementation("androidx.compose.ui:ui-tooling-preview:$composeVersion")
    implementation("androidx.compose.runtime:runtime-livedata:$composeVersion")
    implementation("androidx.core:core-ktx:1.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.4.0")
    implementation(project(":datastore-model"))
    implementation(project(":material-ui"))

    debugImplementation("androidx.compose.ui:ui-tooling:$composeVersion")
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
            create<MavenPublication>("datastoreUiRelease").apply {
                from(components.findByName("release"))
                artifact(sourcesJar)

                groupId = jetprefMavenGroupId
                artifactId = "jetpref-datastore-ui"
                version = jetprefVersion

                pom {
                    name.set("JetPref DataStore Model: Compose UI package")
                    description.set("An AndroidX preference like UI for JetPref datastore settings, written in Jetpack Compose.")
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
