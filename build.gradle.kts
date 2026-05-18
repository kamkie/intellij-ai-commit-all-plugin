import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm") version "2.3.20"
    id("com.diffplug.spotless") version "8.5.1"
    id("org.jetbrains.intellij.platform")
}

group = "pl.devopssolutions"
version = providers.gradleProperty("pluginVersion").get()

val pluginVerifierIdeVersions = providers.gradleProperty("pluginVerifierIdeVersions")
    .map { value ->
        value.split(',')
            .map { version -> version.trim() }
            .filter { version -> version.isNotEmpty() }
    }

dependencies {
    intellijPlatform {
        intellijIdea(providers.gradleProperty("platformVersion").get())
        bundledPlugin("Git4Idea")
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

spotless {
    kotlin {
        target("src/**/*.kt")
        ktlint("1.8.0")
        licenseHeaderFile(rootProject.file("config/spotless/apache-kotlin.license"), "package ")
    }

    kotlinGradle {
        target("*.gradle.kts", "gradle/**/*.gradle.kts")
        ktlint("1.8.0")
    }
}

intellijPlatform {
    projectName.set(rootProject.name)
    buildSearchableOptions.set(false)

    pluginVerification {
        ides {
            create(pluginVerifierIdeVersions)
        }
    }

    pluginConfiguration {
        id = "pl.devopssolutions.aicommitall"
        name = "AI Commit All"
        version = providers.gradleProperty("pluginVersion").get()
        description = """
            <p>AI Commit All adds an AI-assisted Commit tool window workflow for Git projects.</p>
            <p>The Commit tool window control has AI, Commit, and Push sections. AI selects or stages every non-ignored committable Git change and asks JetBrains AI Assistant to generate the commit message without committing. Commit continues through the standard IntelliJ commit workflow. Push commits and then pushes immediately only for safe tracked-upstream Git states, otherwise falling back to the IDE commit-and-push executor.</p>
            <p>Source code: <a href="https://github.com/kamkie/intellij-ai-commit-all-plugin">https://github.com/kamkie/intellij-ai-commit-all-plugin</a></p>
        """.trimIndent()
        changeNotes = """
            <p>v0.1.0-alpha.6 prerelease.</p>
            <ul>
                <li>Waits through initial AI Assistant startup polling so Commit and Push workflows do not stop before generation begins.</li>
                <li>Prevents duplicate workflow starts from rapid repeated action or shortcut invocations.</li>
                <li>Allows safe immediate push on protected branches when no force push is required.</li>
                <li>Binds plugin shortcut actions directly to the IDE commit shortcuts so takeover is registered even when shortcut mirroring is insufficient.</li>
                <li>Clears commit-message control and document data before AI generation, not only the public CommitMessageUi text accessor.</li>
                <li>Moves all-files selection and Git staging confirmation off the UI event thread while returning Commit UI updates and AI invocation to the UI thread, so staging-area mode can reach AI commit-message generation.</li>
                <li>Prevents the running indicator from throwing a negative dash phase paint exception after animation starts.</li>
                <li>Increases the default AI generation timeout to 30 seconds so cold AI Assistant requests can finish in the first Commit or Push attempt.</li>
                <li>Preserves Git staging-area paths when synchronizing fallback Commit tool window inclusion, so already staged files are not dropped from the plugin workflow.</li>
                <li>Ensures the three-section control exposes its fallback accessibility description when Swing provides a blank description.</li>
                <li>Refreshes the three-section control running indicator immediately when AI activity starts or finishes.</li>
                <li>Reloads externally changed staged files before each Git staging tracker recheck, with bounded retries before AI commit-message generation.</li>
                <li>Adds the working AI, Commit, and Push control to the Commit tool window.</li>
                <li>Selects or stages non-ignored Git changes across changelists, Git staging-area workflows, and Git roots, including unversioned and resolved-conflict files.</li>
                <li>Invokes JetBrains AI Assistant commit-message generation through the IntelliJ action system and waits for completion.</li>
                <li>Lets the AI section generate a message and stop before commit; Commit and Push continue only after successful AI generation.</li>
                <li>Stops without committing when AI generation times out, remains running, produces no usable message, or the user edits the message.</li>
                <li>Clears stale commit-message text before AI generation by default, with a settings opt-out.</li>
                <li>Confirms Git staging-area inclusion before AI generation, including nested module and product paths.</li>
                <li>Commits through the standard IntelliJ workflow and pushes immediately only when the tracked-upstream Git state is safe; otherwise falls back to the IDE commit-and-push executor.</li>
                <li>Routes IDE commit shortcuts to AI Commit All Commit and Push workflows by default, with a settings opt-out.</li>
                <li>Adds settings, validation tests, pull-request CI, Plugin Verifier CI, signing configuration, and gated Marketplace publishing automation.</li>
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
