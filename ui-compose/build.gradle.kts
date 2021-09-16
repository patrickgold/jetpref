plugins {
    id("com.android.library")
    id("kotlin-android")
    id("maven-publish")
}

val jetprefMavenGroupId: String by project
val jetprefJitpackGroupId: String by project
val jetprefVersion: String by project

val composeVersion: String by project

android {
    compileSdk = 30

    defaultConfig {
        targetSdk = 30

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            proguardFiles.add(getDefaultProguardFile("proguard-android-optimize.txt"))
        }
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
}

dependencies {
    implementation("androidx.activity:activity-compose:1.3.1")
    implementation("androidx.appcompat:appcompat:1.3.1")
    implementation("androidx.compose.material:material:$composeVersion")
    implementation("androidx.compose.ui:ui:$composeVersion")
    implementation("androidx.compose.ui:ui-tooling-preview:$composeVersion")
    implementation("androidx.compose.runtime:runtime-livedata:$composeVersion")
    implementation("androidx.core:core-ktx:1.6.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.3.1")
    implementation(project(":datastore-model"))

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:$composeVersion")
    debugImplementation("androidx.compose.ui:ui-tooling:$composeVersion")
}

group = jetprefJitpackGroupId
version = jetprefVersion

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("uiComposeRelease").apply {
                from(components.findByName("release"))

                groupId = jetprefMavenGroupId
                artifactId = "jetpref-ui-compose"
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
                        connection.set("scm:git:git://github.com/patrickgold/jetpref.git")
                        url.set("https://github.com/patrickgold/jetpref")
                    }
                }
            }
        }
    }
}
