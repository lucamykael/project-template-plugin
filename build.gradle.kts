plugins {
    id("java")
    id("org.jetbrains.intellij") version "1.17.3"
}

group = "com.dev"
version = "1.0.0"

repositories {
    mavenCentral()
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

intellij {
    version.set("2024.3.4.1") // ou a versão do IntelliJ que você usa
    type.set("IC")        // IC = Community, IU = Ultimate
    plugins.set(listOf("java"))
}

tasks {
    patchPluginXml {
        sinceBuild.set("243")
        untilBuild.set("243.*")
    }

    runIde {
        autoReloadPlugins.set(false)
    }
}
