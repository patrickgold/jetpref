plugins {
    id("com.android.application")
    id("kotlin-android")
}

val composeVersion: String by project

android {
    compileSdk = 31

    defaultConfig {
        applicationId = "dev.patrickgold.jetpref.example"
        minSdk = 23
        targetSdk = 30
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
    implementation("androidx.activity:activity-compose:1.3.1")
    implementation("androidx.appcompat:appcompat:1.3.1")
    implementation("androidx.compose.material:material:$composeVersion")
    implementation("androidx.compose.ui:ui:$composeVersion")
    implementation("androidx.compose.ui:ui-tooling-preview:$composeVersion")
    implementation("androidx.core:core-ktx:1.6.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.3.1")
    implementation("androidx.navigation:navigation-compose:2.4.0-alpha09")
    implementation("com.google.android.material:material:1.4.0")
    implementation(project(":datastore-model"))
    implementation(project(":ui-compose"))

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:$composeVersion")
    debugImplementation("androidx.compose.ui:ui-tooling:$composeVersion")
}
