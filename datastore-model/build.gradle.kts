import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.agp.library.multiplatform)
    alias(libs.plugins.kover)
    alias(libs.plugins.ksp)
    alias(libs.plugins.vanniktech.maven.publish)
}

kotlin {
    jvm {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
    }

    android {
        namespace = "dev.patrickgold.jetpref.datastore"
        compileSdk = providers.gradleProperty("projectCompileSdk").map { it.toInt() }.get()
        minSdk = providers.gradleProperty("projectMinSdk").map { it.toInt() }.get()

        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }

        withHostTest {}

        optimization {
            consumerKeepRules.publish = true
            consumerKeepRules.files.add(File("consumer-rules.pro"))
        }
    }

    applyDefaultHierarchyTemplate()
    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.kotlinx.coroutines.core)
            }
        }
        commonTest {
            dependencies {
                implementation(libs.kotlin.test)
                implementation(libs.kotlinx.coroutines.test)
            }
        }
        val jvmCommonMain = create("jvmCommonMain") {
            dependsOn(commonMain.get())
        }
        val jvmCommonTest = create("jvmCommonTest") {
            dependsOn(commonTest.get())
            dependencies {
                implementation(libs.junit.jupiter.api)
                implementation(libs.junit.jupiter.params)
            }
        }
        jvmMain {
            dependsOn(jvmCommonMain)
        }
        jvmTest {
            dependsOn(jvmCommonTest)
        }
        androidMain {
            dependsOn(jvmCommonMain)
        }
        getByName("androidHostTest") {
            dependsOn(jvmCommonTest)
        }
    }
}

dependencies {
    listOf(
        "kspJvmTest",
        "kspAndroidHostTest",
    ).forEach { configurationName ->
        add(configurationName, projects.datastoreModelProcessor)
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

mavenPublishing {
    coordinates(
        groupId = providers.gradleProperty("projectGroupId").get(),
        artifactId = "jetpref-datastore-model",
        version = providers.gradleProperty("projectVersion").get(),
    )
}
