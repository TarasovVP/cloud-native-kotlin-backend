plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.sqlDelight)
    application
}

kotlin {
    jvmToolchain(17)
}

application {
    mainClass.set("com.tarasovvp.cloudnativebackend.ApplicationKt")
}

dependencies {
    implementation(project(":core"))

    implementation(libs.logback)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.koin.core)
    implementation(libs.koin.ktor)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.cors)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)

    implementation(libs.postgresql)
    implementation(libs.hikari.cp)
    implementation(libs.jdbc.driver)
    implementation(libs.postgres.socket.factory)

    testImplementation(kotlin("test-junit5"))
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.ktor.server.test.host)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

sqldelight {
    databases {
        create("ServerDatabase") {
            packageName.set("com.tarasovvp.cloudnativebackend")
            dialect("app.cash.sqldelight:postgresql-dialect:2.0.2")
        }
    }
}

tasks.test {
    useJUnitPlatform()
}

tasks.register<Jar>("fatJar") {
    archiveFileName.set("cloud-native-kotlin-backend.jar")
    manifest {
        attributes["Main-Class"] = "com.tarasovvp.cloudnativebackend.ApplicationKt"
    }
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    dependsOn(tasks.classes)
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    from(sourceSets.main.get().output)
}
