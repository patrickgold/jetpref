plugins {
    id("com.android.application")
    id("kotlin-android")
}

val jetprefCompileSdk: String by project
val jetprefMinSdk: String by project
val jetprefTargetSdk: String by project

val composeVersion: String by project

android {
    compileSdk = jetprefCompileSdk.toInt()

    defaultConfig {
        applicationId = "dev.patrickgold.jetpref.example"
        minSdk = jetprefMinSdk.toInt()
        targetSdk = jetprefTargetSdk.toInt()
        versionCode = 1
        versionName = "0.1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        sourceSets {
            maybeCreate("main").apply {
                java {
                    srcDirs("src/main/kotlin")
                }
            }
        }
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
    packagingOptions {
        resources {
            excludes.add("/META-INF/{AL2.0,LGPL2.1}")
        }
    }
}

dependencies {
    implementation("androidx.activity:activity-compose:1.4.0")
    implementation("androidx.appcompat:appcompat:1.4.0")
    implementation("androidx.compose.material:material:$composeVersion")
    implementation("androidx.compose.ui:ui:$composeVersion")
    implementation("androidx.compose.ui:ui-tooling-preview:$composeVersion")
    implementation("androidx.core:core-ktx:1.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.4.0")
    implementation("androidx.navigation:navigation-compose:2.4.0-rc01")
    implementation("com.google.android.material:material:1.4.0")
    implementation(project(":datastore-model"))
    implementation(project(":datastore-ui"))

    debugImplementation("androidx.compose.ui:ui-tooling:$composeVersion")
}
