plugins {
    id("java")
    id("org.jetbrains.intellij") version "1.17.3"
}

group = "com.dev"
version = "1.0.2"

repositories {
    mavenCentral()
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

intellij {
    version.set("2025.2.2")
    type.set("IC")
    plugins.set(listOf("java"))
}

tasks {
    patchPluginXml {
        sinceBuild.set("193")
        untilBuild.set("252.*")
    }

    runIde {
        autoReloadPlugins.set(false)
    }

    publishPlugin {
        token.set(System.getenv("JETBRAINS_TOKEN"))
    }

    buildSearchableOptions {
        enabled = false
    }
}
