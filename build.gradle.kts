plugins {
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.plugin.compose) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.kover) apply false
    alias(libs.plugins.agp.application) apply false
    alias(libs.plugins.agp.library.multiplatform) apply false
    alias(libs.plugins.vanniktech.maven.publish) apply false
}
