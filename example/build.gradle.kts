import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.agp.application)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.plugin.compose)
}

val projectCompileSdk: String by project
val projectMinSdk: String by project
val projectTargetSdk: String by project
val projectVersion: String by project

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_11)
    }
}

android {
    namespace = "dev.patrickgold.jetpref.example"
    compileSdk = projectCompileSdk.toInt()

    defaultConfig {
        applicationId = "dev.patrickgold.jetpref.example"
        minSdk = projectMinSdk.toInt()
        targetSdk = projectTargetSdk.toInt()
        versionCode = 1
        versionName = projectVersion

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
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
            isMinifyEnabled = true
            isShrinkResources = true
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material.icons)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.navigation.compose)
    implementation(projects.datastoreModel)
    ksp(projects.datastoreModelProcessor)
    implementation(projects.datastoreUi)
    implementation(projects.materialUi)

    debugImplementation(libs.androidx.compose.ui.tooling)
}
