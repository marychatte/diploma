val kotlinx_coroutines: String by project
val kotlinx_cli: String by project
val netty: String by project

plugins {
    kotlin("jvm") version "1.9.22"
    application
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

application {
    mainClass = "run.MainKt"
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinx_coroutines")
    implementation("org.jetbrains.kotlinx:kotlinx-cli:$kotlinx_cli")
    implementation("io.netty:netty-all:$netty")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}

tasks.named<Jar>("jar") {
    manifest {
        attributes["Main-Class"] = "run.MainKt"
    }
}

tasks.create<Jar>("fatJar") {
    group = "build"
    manifest {
        attributes["Main-Class"] = "run.MainKt"
    }

    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })

    with(tasks["jar"] as CopySpec)
}
