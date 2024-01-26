plugins {
    id("org.jetbrains.kotlin.jvm") version "1.8.20"
    application
}

repositories {
    mavenCentral()
}

dependencies {
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

application {
    mainClass.set("me.madhead.derezzed.AppKt")
}
