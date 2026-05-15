import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm") version "2.3.20"
    id("org.jetbrains.intellij.platform")
}

group = "pl.devopssolutions"
version = providers.gradleProperty("pluginVersion").get()

dependencies {
    intellijPlatform {
        intellijIdea(providers.gradleProperty("platformVersion").get())
        plugin(
            providers.gradleProperty("aiAssistantPluginId").get(),
            providers.gradleProperty("aiAssistantPluginVersion").get(),
        )
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

intellijPlatform {
    projectName.set(rootProject.name)
    buildSearchableOptions.set(false)

    pluginConfiguration {
        id = "pl.devopssolutions.aicommitall"
        name = "AI Commit All"
        version = providers.gradleProperty("pluginVersion").get()
        description = """
            <p>Adds an AI-assisted commit flow for JetBrains IDEs with the VCS Commit tool window.</p>
            <p>The workflow implementation is still pending in this prerelease scaffold.</p>
        """.trimIndent()
        changeNotes = """
            <p>Initial executable plugin scaffold.</p>
        """.trimIndent()

        ideaVersion {
            sinceBuild = providers.gradleProperty("pluginSinceBuild").get()
            untilBuild = provider { null }
        }

        vendor {
            name = "DevOps Solutions Kamil Kiewisz"
            email = "kontakt@devopssolutions.pl"
            url = "https://devopssolutions.pl"
        }
    }
}
