import com.palantir.gradle.gitversion.VersionDetails
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.w3c.dom.Element
import javax.xml.parsers.DocumentBuilderFactory

plugins {
    kotlin("jvm") version "2.3.21"
    jacoco
    id("com.diffplug.spotless") version "8.5.1"
    id("com.palantir.git-version") version "5.0.0"
    id("org.jetbrains.intellij.platform")
}

val versionDetails: groovy.lang.Closure<VersionDetails> by extra
val gitDerivedPluginVersion = providers.provider {
    val details = versionDetails()
    val tagOrHash = details.lastTag?.takeIf { tag -> tag.isNotBlank() } ?: details.gitHash
    val commitDistance = details.commitDistance.takeIf { distance -> distance > 0 }
        ?.let { distance -> "-$distance" }
        .orEmpty()
    val dirtySuffix = if (details.version.endsWith(".dirty")) ".dirty" else ""
    "$tagOrHash$commitDistance-g${details.gitHash}$dirtySuffix"
}
val pluginVersion = providers.environmentVariable("GIT_VERSION").orElse(gitDerivedPluginVersion)

group = "pl.devopssolutions"
version = pluginVersion.get()

val pluginVerifierIdeVersions = providers.gradleProperty("pluginVerifierIdeVersions")
    .map { value ->
        value.split(',')
            .map { version -> version.trim() }
            .filter { version -> version.isNotEmpty() }
    }
val pluginChangeNotes = providers.fileContents(
    layout.projectDirectory.file("config/intellij-platform/change-notes.html"),
).asText
val pluginDescription = providers.fileContents(
    layout.projectDirectory.file("config/intellij-platform/description.html"),
).asText

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

    extensions.configure<JacocoTaskExtension> {
        isIncludeNoLocationClasses = true
        excludes = listOf("jdk.internal.*")
    }
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    classDirectories.setFrom(layout.buildDirectory.dir("instrumented/instrumentCode"))

    reports {
        xml.required.set(true)
        csv.required.set(false)
        html.required.set(true)
    }
}

val jacocoXmlReport = layout.buildDirectory.file("reports/jacoco/test/jacocoTestReport.xml")

val verifyJacocoCoverageReport by tasks.registering {
    group = "verification"
    description = "Verifies the JaCoCo XML report contains executed production coverage."
    dependsOn(tasks.jacocoTestReport)

    inputs.file(jacocoXmlReport)

    doLast {
        val reportFile = jacocoXmlReport.get().asFile
        val documentBuilderFactory = DocumentBuilderFactory.newInstance()
        documentBuilderFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
        documentBuilderFactory.setFeature("http://xml.org/sax/features/external-general-entities", false)
        documentBuilderFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false)

        val document = documentBuilderFactory
            .newDocumentBuilder()
            .parse(reportFile)
        val rootChildren = document.documentElement.childNodes
        val coveredInstructions = (0 until rootChildren.length)
            .asSequence()
            .map { index -> rootChildren.item(index) }
            .filterIsInstance<Element>()
            .first { element ->
                element.tagName == "counter" &&
                    element.getAttribute("type") == "INSTRUCTION"
            }
            .getAttribute("covered")
            .toLong()

        if (coveredInstructions <= 0) {
            throw GradleException(
                "JaCoCo XML report contains zero covered instructions. " +
                    "Check the test JaCoCo agent and report class directories.",
            )
        }
    }
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
        version = pluginVersion.get()
        description = pluginDescription.get().trim()
        changeNotes = pluginChangeNotes.get().trim()

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
