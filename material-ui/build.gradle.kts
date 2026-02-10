import com.android.build.api.dsl.androidLibrary
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.plugin.compose)
    alias(libs.plugins.agp.library.multiplatform)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.vanniktech.maven.publish)
}

kotlin {
    androidLibrary {
        val projectCompileSdk: String by project
        val projectMinSdk: String by project

        namespace = "dev.patrickgold.jetpref.material.ui"
        compileSdk = projectCompileSdk.toInt()
        minSdk = projectMinSdk.toInt()

        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }

        withHostTest {}

        optimization {
            consumerKeepRules.publish = true
            consumerKeepRules.files.add(File("proguard-rules.pro"))
        }
    }

    sourceSets {
        androidMain {
            dependencies {
                implementation(libs.jetbrains.compose.material3)
                implementation(libs.jetbrains.compose.material.icons.extended)
                implementation(libs.jetbrains.compose.runtime)
                implementation(libs.jetbrains.compose.ui)
            }
        }
        getByName("androidHostTest") {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

mavenPublishing {
    val projectGroupId: String by project
    val artifactId = "jetpref-material-ui"
    val projectVersion: String by project
    coordinates(projectGroupId, artifactId, projectVersion)
}
