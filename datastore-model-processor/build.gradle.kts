import org.gradle.kotlin.dsl.support.kotlinCompilerOptions

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.vanniktech.maven.publish)
}

kotlin {
    jvm()

    sourceSets {
        jvmMain {
            dependencies {
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
