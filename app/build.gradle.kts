plugins {
    kotlin("jvm").version("1.9.22")
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-server-core:2.3.7")
    implementation("io.ktor:ktor-server-netty:2.3.7")
    implementation("org.apache.logging.log4j:log4j-slf4j2-impl:2.22.1")
    implementation("io.ktor:ktor-server-default-headers-jvm:2.3.7")
    implementation("io.ktor:ktor-server-call-logging-jvm:2.3.7")
    implementation("io.ktor:ktor-server-compression-jvm:2.3.7")
    implementation("dev.inmo:tgbotapi:10.0.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

application {
    mainClass = "me.madhead.derezzed.Derezzed"
    applicationName = "derezzed"
}
