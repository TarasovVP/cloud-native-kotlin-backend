plugins {
    alias(libs.plugins.kotlinJvm) apply false
    alias(libs.plugins.kotlinSerialization) apply false
    alias(libs.plugins.sqlDelight) apply false
    alias(libs.plugins.ktor) apply false
}

allprojects {
    group = "com.tarasovvp.cloudnativebackend"
    version = "1.0.0"
}
