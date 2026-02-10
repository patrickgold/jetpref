import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.vanniktech.maven.publish)
}

kotlin {
    jvm {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
    }

    sourceSets {
        jvmMain {
            dependencies {
                implementation(libs.kotlinpoet)
                implementation(libs.kotlinpoet.ksp)
                implementation(libs.ksp.api)
            }
        }
    }
}

mavenPublishing {
    val projectGroupId: String by project
    val artifactId = "jetpref-datastore-model-processor"
    val projectVersion: String by project
    coordinates(projectGroupId, artifactId, projectVersion)
}
