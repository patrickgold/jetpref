import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.plugin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.agp.library)
    alias(libs.plugins.vanniktech.maven.publish)
}

kotlin {
    jvm()
    androidTarget {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
    }

    applyDefaultHierarchyTemplate()
    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.serialization.json)
            }
        }
        commonTest {
            dependencies {
                implementation(libs.kotlin.test)
                implementation(libs.kotlinx.coroutines.test)
            }
        }
        val jvmCommonMain by creating {
            dependsOn(commonMain.get())
        }
        jvmMain {
            dependsOn(jvmCommonMain)
        }
        androidMain {
            dependsOn(jvmCommonMain)
        }
    }
}

dependencies {
    listOf(
        "kspJvmTest",
        "kspAndroidTest",
    ).forEach { configurationName ->
        add(configurationName, project(":datastore-model-processor"))
    }
}

android {
    val projectCompileSdk: String by project
    val projectMinSdk: String by project

    namespace = "dev.patrickgold.jetpref.datastore"
    compileSdk = projectCompileSdk.toInt()

    defaultConfig {
        minSdk = projectMinSdk.toInt()
        consumerProguardFiles("proguard-rules.pro")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

mavenPublishing {
    val projectGroupId: String by project
    val artifactId = "jetpref-datastore-model"
    val projectVersion: String by project
    coordinates(projectGroupId, artifactId, projectVersion)
}
