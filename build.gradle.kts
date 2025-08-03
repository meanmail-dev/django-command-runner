import org.jetbrains.intellij.platform.gradle.TestFrameworkType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

fun config(name: String) = project.findProperty(name).toString()

repositories {
    mavenCentral()

    intellijPlatform {
        defaultRepositories()
    }
}

plugins {
    java
// https://plugins.jetbrains.com/docs/intellij/using-kotlin.html#kotlin-standard-library
    alias(libs.plugins.kotlin) // Kotlin support
    alias(libs.plugins.intelliJPlatform) // IntelliJ Platform Gradle Plugin
}

group = config("group")
version = config("version")

dependencies {
    testImplementation(libs.junit)

    intellijPlatform {
        create(config("platformType"), config("platformVersion"))
        plugins(providers.gradleProperty("plugins").map { it.split(',') })
        val platformBundledPlugins = providers.gradleProperty("platformBundledPlugins").map { it.split(',') }
        if (platformBundledPlugins.isPresent && platformBundledPlugins.get().isNotEmpty()) {
            bundledPlugins(platformBundledPlugins)
        }

        instrumentationTools()
        pluginVerifier()
        zipSigner()
        testFramework(TestFrameworkType.Platform)
    }
}

intellijPlatform {
    pluginConfiguration {
        name.set(config("pluginName"))
        version.set(project.version.toString())
        ideaVersion {
            sinceBuild.set(config("platformSinceBuild"))
        }
    }

    buildSearchableOptions = false

    pluginVerification.ides {
        recommended()
    }

    publishing {
        try {
            token.set(file("token.txt").readLines()[0])
        } catch (e: Exception) {
            println("No token.txt found")
        }
        channels.set(listOf(config("publishChannel")))
    }
}

tasks {
    config("jvmVersion").let {
        withType<JavaCompile> {
            sourceCompatibility = it
            targetCompatibility = it
        }
        withType<KotlinCompile> {
            kotlinOptions.jvmTarget = it
        }
    }

    wrapper {
        distributionType = Wrapper.DistributionType.ALL
        gradleVersion = config("gradleVersion")
    }

    test {
        useJUnit()

        maxHeapSize = "1G"
    }
}
