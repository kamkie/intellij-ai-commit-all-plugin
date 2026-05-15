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
        zipSigner()
    }

    testImplementation(kotlin("test"))
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

tasks.test {
    useJUnitPlatform()
}

intellijPlatform {
    projectName.set(rootProject.name)
    buildSearchableOptions.set(false)

    pluginVerification {
        ides {
            current()
        }
    }

    pluginConfiguration {
        id = "pl.devopssolutions.aicommitall"
        name = "AI Commit All"
        version = providers.gradleProperty("pluginVersion").get()
        description = """
            <p>AI Commit All adds an AI-assisted Commit tool window workflow for Git projects.</p>
            <p>The split-button action selects every non-ignored committable Git change, asks JetBrains AI Assistant to generate the commit message, then commits through the standard IntelliJ commit workflow. The push segment uses the IDE commit-and-push executor.</p>
            <p>Source code: <a href="https://github.com/kamkie/intellij-ai-commit-all-plugin">https://github.com/kamkie/intellij-ai-commit-all-plugin</a></p>
        """.trimIndent()
        changeNotes = """
            <p>v0.1.0-alpha.1 prerelease.</p>
            <ul>
                <li>Adds the AI Commit All split-button actions to the Commit tool window.</li>
                <li>Selects non-ignored committable Git changes across changelists and Git roots.</li>
                <li>Invokes JetBrains AI Assistant commit-message generation through the IntelliJ action system.</li>
                <li>Commits and commits-and-pushes through standard IntelliJ workflow executors.</li>
                <li>Adds configurable AI generation timeout and completion-check settings.</li>
            </ul>
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

    signing {
        certificateChain.convention(providers.environmentVariable("CERTIFICATE_CHAIN"))
        privateKey.convention(providers.environmentVariable("PRIVATE_KEY"))
        password.convention(providers.environmentVariable("PRIVATE_KEY_PASSWORD"))
    }

    publishing {
        token.convention(providers.environmentVariable("PUBLISH_TOKEN"))
        channels.set(
            providers.gradleProperty("pluginPublishChannels")
                .map { value ->
                    value.split(',')
                        .map { channel -> channel.trim() }
                        .filter { channel -> channel.isNotEmpty() }
                },
        )
    }
}
