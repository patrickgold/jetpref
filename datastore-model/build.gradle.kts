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
    id("de.mannodermaus.android-junit5")
}

val jetprefCompileSdk: String by project
val jetprefMinSdk: String by project
val jetprefTargetSdk: String by project

val jetprefMavenGroupId: String by project
val jetprefJitpackGroupId: String by project
val jetprefVersion: String by project

val kotestVersion: String by project

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

    sourceSets {
        maybeCreate("main").apply {
            java.srcDir("src/main/kotlin")
        }
    }
}

dependencies {
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.4.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.5.2")
    implementation(project(":datastore-annotations"))

    testImplementation("io.kotest:kotest-runner-junit5:$kotestVersion")
    testImplementation("io.kotest:kotest-assertions-core:$kotestVersion")
    testImplementation("io.kotest:kotest-property:$kotestVersion")
}

tasks.withType<Test> {
    useJUnitPlatform()
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
            create<MavenPublication>("datastoreModelRelease").apply {
                from(components.findByName("release"))
                artifact(sourcesJar)

                groupId = jetprefMavenGroupId
                artifactId = "jetpref-datastore-model"
                version = jetprefVersion

                pom {
                    name.set("JetPref DataStore Model")
                    description.set("An alternative model to SharedPreferences.")
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
