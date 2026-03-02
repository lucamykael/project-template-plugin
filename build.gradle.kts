plugins {
    id("java")
    id("org.jetbrains.intellij") version "1.17.3"
}

group = "com.dev"
version = "1.0.3"

repositories {
    mavenCentral()
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

intellij {
    version.set("2022.2.2")
    type.set("IC")
    plugins.set(listOf("java", "maven"))
}

dependencies {
    implementation("org.eclipse.jgit:org.eclipse.jgit:6.9.0.202403050737-r"){
        exclude(group = "org.slf4j")
    }

    implementation("commons-io:commons-io:2.15.1")
}

tasks {
    patchPluginXml {
        sinceBuild.set("222")
        untilBuild.set("252.*")
    }

    runIde {
        autoReloadPlugins.set(false)
        jvmArgs("-Didea.package.search.enabled=false")
    }

    publishPlugin {
        token.set(System.getenv("JETBRAINS_TOKEN"))
    }

    buildSearchableOptions {
        enabled = false
    }
}
