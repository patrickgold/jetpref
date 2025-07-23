plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.plugin.compose)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.agp.application)
}

val projectCompileSdk: String by project
val projectMinSdk: String by project
val projectTargetSdk: String by project
val projectVersion: String by project

kotlin {
    androidTarget()

    sourceSets {
        androidMain {
            dependencies {
                implementation(compose.material3)
                implementation(compose.materialIconsExtended)
                implementation(compose.runtime)
                implementation(compose.ui)
                implementation(libs.androidx.activity.compose)
                implementation(libs.androidx.core.ktx)
                implementation(libs.androidx.lifecycle.runtime.ktx)
                implementation(libs.androidx.navigation.compose)
                implementation(project(":datastore-model"))
                implementation(project(":datastore-ui"))
                implementation(project(":material-ui"))
            }
        }
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
    }

    buildTypes {
        release {
            proguardFiles.add(getDefaultProguardFile("proguard-android-optimize.txt"))
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}
