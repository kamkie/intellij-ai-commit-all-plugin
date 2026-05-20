import com.palantir.gradle.gitversion.VersionDetails
import dev.detekt.gradle.Detekt
import org.jetbrains.intellij.platform.gradle.IntelliJPlatformType
import org.jetbrains.intellij.platform.gradle.TestFrameworkType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.w3c.dom.Element
import java.util.*
import javax.xml.parsers.DocumentBuilderFactory

plugins {
    kotlin("jvm") version "2.3.21"
    jacoco
    id("dev.detekt") version "2.0.0-alpha.3"
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
val jdkVersion = JavaVersion.VERSION_21
val jdkVersionTarget = jdkVersion.majorVersion

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
val releaseMatrixIdeProducts = providers.gradleProperty("ideProducts").orElse("IU")
val releaseMatrixIdeVersion = providers.gradleProperty("ideVersion")
    .orElse(providers.gradleProperty("platformVersion"))

val integrationTestSourceSet = sourceSets.create("integrationTest") {
    compileClasspath += sourceSets.main.get().output
    runtimeClasspath += sourceSets.main.get().output
}
val integrationTestImplementation by configurations.getting {
    extendsFrom(configurations.testImplementation.get())
}
val integrationTestCompileOnly by configurations.getting {
    extendsFrom(configurations.testCompileOnly.get())
}
val integrationTestRuntimeOnly by configurations.getting {
    extendsFrom(configurations.testRuntimeOnly.get())
}

dependencies {
    intellijPlatform {
        intellijIdea(providers.gradleProperty("platformVersion").get())
        bundledPlugin("Git4Idea")
        plugin(
            providers.gradleProperty("aiAssistantPluginId").get(),
            providers.gradleProperty("aiAssistantPluginVersion").get(),
        )
        testFramework(TestFrameworkType.Starter, configurationName = integrationTestImplementation.name)
        zipSigner()
    }

    testImplementation(kotlin("test"))
    integrationTestImplementation(kotlin("test"))
    integrationTestImplementation("org.junit.jupiter:junit-jupiter:5.7.1")
    integrationTestImplementation("org.kodein.di:kodein-di-jvm:7.20.2")
    integrationTestImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.10.1")
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.fromTarget(jdkVersionTarget))
    }
}

java {
    sourceCompatibility = jdkVersion
    targetCompatibility = jdkVersion
}

detekt {
    toolVersion = "2.0.0-alpha.3"
    source.setFrom("src/main/kotlin", "src/test/kotlin")
    baseline = file("config/detekt/baseline.xml")
    basePath.set(projectDir)
}

tasks.withType<Detekt>().configureEach {
    jvmTarget.set(jdkVersionTarget)

    reports {
        checkstyle.required.set(true)
        html.required.set(true)
        sarif.required.set(true)
        markdown.required.set(true)
    }
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

abstract class VerifyJacocoCoverageReportTask : DefaultTask() {
    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val reportFile: RegularFileProperty

    @get:Input
    abstract val minimumLineCoverage: Property<Double>

    @get:Input
    abstract val minimumBranchCoverage: Property<Double>

    @TaskAction
    fun verify() {
        val documentBuilderFactory = DocumentBuilderFactory.newInstance()
        documentBuilderFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
        documentBuilderFactory.setFeature("http://xml.org/sax/features/external-general-entities", false)
        documentBuilderFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false)

        val document = documentBuilderFactory
            .newDocumentBuilder()
            .parse(reportFile.get().asFile)
        val coveredInstructions = counter(document.documentElement, "INSTRUCTION").covered

        if (coveredInstructions <= 0) {
            throw GradleException(
                "JaCoCo XML report contains zero covered instructions. " +
                    "Check the test JaCoCo agent and report class directories.",
            )
        }

        verifyMinimumCoverage(
            label = "line",
            actual = counter(document.documentElement, "LINE").ratio,
            minimum = minimumLineCoverage.get(),
        )
        verifyMinimumCoverage(
            label = "branch",
            actual = counter(document.documentElement, "BRANCH").ratio,
            minimum = minimumBranchCoverage.get(),
        )
    }

    private fun counter(
        root: Element,
        type: String,
    ): CoverageCounter {
        val rootChildren = root.childNodes
        val element = (0 until rootChildren.length)
            .asSequence()
            .map { index -> rootChildren.item(index) }
            .filterIsInstance<Element>()
            .firstOrNull { element ->
                element.tagName == "counter" &&
                    element.getAttribute("type") == type
            }
            ?: throw GradleException("JaCoCo XML report is missing the $type counter.")
        return CoverageCounter(
            covered = element.getAttribute("covered").toLong(),
            missed = element.getAttribute("missed").toLong(),
        )
    }

    private fun verifyMinimumCoverage(
        label: String,
        actual: Double,
        minimum: Double,
    ) {
        if (actual < minimum) {
            throw GradleException(
                "JaCoCo $label coverage ${actual.asPercentage()} is below the required " +
                    "${minimum.asPercentage()}.",
            )
        }
    }

    private fun Double.asPercentage(): String = String.format(Locale.ROOT, "%.1f%%", this * 100)

    private data class CoverageCounter(
        val covered: Long,
        val missed: Long,
    ) {
        val ratio: Double
            get() {
                val total = covered + missed
                return if (total == 0L) {
                    1.0
                } else {
                    covered.toDouble() / total
                }
            }
    }
}

val jacocoXmlReport = layout.buildDirectory.file("reports/jacoco/test/jacocoTestReport.xml")

val verifyJacocoCoverageReport by tasks.registering(VerifyJacocoCoverageReportTask::class) {
    group = "verification"
    description = "Verifies the JaCoCo XML report contains executed production coverage."
    dependsOn(tasks.jacocoTestReport)
    reportFile.set(jacocoXmlReport)
    minimumLineCoverage.set(0.68)
    minimumBranchCoverage.set(0.62)
}

val fakeAiAssistantPluginJar by tasks.registering(Jar::class) {
    group = "verification"
    description = "Builds the test-only AI Assistant substitute plugin JAR."
    archiveBaseName.set("fake-ai-assistant-plugin")
    archiveVersion.set("0.0.1-test")
    from(integrationTestSourceSet.output.classesDirs) {
        include("pl/devopssolutions/aicommitall/integration/fakeai/**")
    }
    from(layout.projectDirectory.dir("src/integrationTest/resources/fake-ai-assistant-plugin")) {
        include("META-INF/plugin.xml")
    }
}

val fakeAiAssistantPlugin by tasks.registering(Zip::class) {
    group = "verification"
    description = "Packages the test-only AI Assistant substitute plugin."
    archiveBaseName.set("fake-ai-assistant-plugin")
    archiveVersion.set("0.0.1-test")
    destinationDirectory.set(layout.buildDirectory.dir("integrationTest/plugins"))
    from(fakeAiAssistantPluginJar) {
        into("fake-ai-assistant-plugin/lib")
    }
}
val fakeAiAssistantPluginArchiveFile = fakeAiAssistantPlugin.flatMap { plugin -> plugin.archiveFile }

fun ideaReleaseMatrixProduct(products: String, version: String): IntelliJPlatformType {
    val productCodes = products.split(',')
        .map { product -> product.trim() }
        .filter { product -> product.isNotEmpty() }
    if (productCodes != listOf("IU")) {
        throw GradleException(
            "releaseMatrixUiTest currently supports IDEA only. Use -PideProducts=IU, not '$products'.",
        )
    }
    return IntelliJPlatformType.fromCode("IU", version)
}

val releaseMatrixUiTest by intellijPlatformTesting.testIdeUi.registering {
    type.set(
        releaseMatrixIdeProducts.zip(releaseMatrixIdeVersion) { products, version ->
            ideaReleaseMatrixProduct(products, version)
        },
    )
    version.set(releaseMatrixIdeVersion)

    task {
        group = "verification"
        description = "Runs IDEA release-matrix UI automation with Starter and Driver."
        testClassesDirs = integrationTestSourceSet.output.classesDirs
        classpath = integrationTestSourceSet.runtimeClasspath
        useJUnitPlatform()
        shouldRunAfter(tasks.test)
        dependsOn(fakeAiAssistantPlugin)
        systemProperty("aicommitall.ide.products", releaseMatrixIdeProducts.get())
        systemProperty("aicommitall.ide.version", releaseMatrixIdeVersion.get())
        systemProperty(
            "aicommitall.fake.ai.plugin.path",
            fakeAiAssistantPluginArchiveFile.get().asFile.absolutePath,
        )
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
