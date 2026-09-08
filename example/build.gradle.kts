import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.agp.application)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.plugin.compose)
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_11)
    }
}

android {
    namespace = "dev.patrickgold.jetpref.example"
    compileSdk = providers.gradleProperty("projectCompileSdk").map { it.toInt() }.get()

    defaultConfig {
        applicationId = "dev.patrickgold.jetpref.example"
        minSdk = providers.gradleProperty("projectMinSdk").map { it.toInt() }.get()
        targetSdk = providers.gradleProperty("projectTargetSdk").map { it.toInt() }.get()
        versionCode = 1
        versionName = providers.gradleProperty("projectVersion").get()

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
