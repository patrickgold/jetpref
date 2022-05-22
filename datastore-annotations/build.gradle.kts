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
    alias(libs.plugins.kotlin.jvm)
    id("maven-publish")
}

val jetprefMavenGroupId: String by project
val jetprefJitpackGroupId: String by project
val jetprefVersion: String by project

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

sourceSets {
    maybeCreate("main").apply {
        java.srcDir("src/main/kotlin")
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

val sourcesJar = tasks.register<Jar>("sourcesJar") {
    archiveClassifier.set("sources")
    from(sourceSets.getByName("main").java.srcDirs)
}

group = jetprefJitpackGroupId
version = jetprefVersion

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("datastoreAnnotationsRelease").apply {
                from(components.findByName("java"))
                artifact(sourcesJar)

                groupId = jetprefMavenGroupId
                artifactId = "jetpref-datastore-annotations"
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
