import com.adarshr.gradle.testlogger.theme.ThemeType
import com.palantir.gradle.gitversion.VersionDetails
import dev.detekt.gradle.Detekt
import org.jetbrains.intellij.platform.gradle.IntelliJPlatformType
import org.jetbrains.intellij.platform.gradle.TestFrameworkType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import pl.devopssolutions.aicommitall.gradle.VerifyDetektBaselineTask
import pl.devopssolutions.aicommitall.gradle.VerifyJacocoCoverageReportTask
import java.util.Locale

plugins {
    kotlin("jvm") version "2.3.21"
    jacoco
    id("dev.detekt") version "2.0.0-alpha.3"
    id("com.diffplug.spotless") version "8.5.1"
    id("com.palantir.git-version") version "5.0.0"
    id("com.adarshr.test-logger") version "4.0.0"
    id("org.jetbrains.intellij.platform")
}

val versionDetails: groovy.lang.Closure<VersionDetails> by extra
data class PluginBuildVersion(
    val pluginVersion: String,
    val archiveVersion: String,
)

val semanticVersionTagPattern =
    Regex("""^v?((?:0|[1-9]\d*)\.(?:0|[1-9]\d*)\.(?:0|[1-9]\d*)(?:-[0-9A-Za-z-]+(?:\.[0-9A-Za-z-]+)*)?)$""")

fun pluginBuildVersion(
    baseVersion: String,
    commitDistance: Int,
    gitHash: String,
    dirty: Boolean,
): PluginBuildVersion {
    val hash = normalizedGitHash(gitHash)
    if (commitDistance == 0 && !dirty) {
        return PluginBuildVersion(
            pluginVersion = baseVersion,
            archiveVersion = baseVersion,
        )
    }

    val pluginMetadata = listOfNotNull(
        commitDistance.toString(),
        "g$hash",
        "dirty".takeIf { dirty },
    ).joinToString(".")
    val archiveMetadata = listOfNotNull(
        commitDistance.toString(),
        hash,
        "dirty".takeIf { dirty },
    ).joinToString(".")
    return PluginBuildVersion(
        pluginVersion = "$baseVersion+$pluginMetadata",
        archiveVersion = "$baseVersion+$archiveMetadata",
    )
}

fun formatVersionDetails(details: VersionDetails): PluginBuildVersion {
    val tag = details.lastTag?.takeIf { lastTag -> lastTag.isNotBlank() }
    val dirty = details.version.endsWith(".dirty")
    if (tag == null) {
        return pluginBuildVersion(
            baseVersion = "0.0.0",
            commitDistance = details.commitDistance.takeIf { distance -> distance > 0 } ?: 1,
            gitHash = details.gitHash,
            dirty = dirty,
        )
    }

    return pluginBuildVersion(
        baseVersion = semanticVersionFromTag(tag),
        commitDistance = details.commitDistance,
        gitHash = details.gitHash,
        dirty = dirty,
    )
}

fun semanticVersionFromTag(tag: String): String {
    val match = semanticVersionTagPattern.matchEntire(tag)
        ?: throw GradleException("Git tag '$tag' must use vMAJOR.MINOR.PATCH or vMAJOR.MINOR.PATCH-PRERELEASE.")
    return match.groupValues[1]
}

fun normalizedGitHash(gitHash: String): String {
    val hash = gitHash.removePrefix("g")
    if (hash.isBlank()) {
        throw GradleException("Git hash must not be blank.")
    }
    return hash
}

val gitDerivedBuildVersion = providers.provider { formatVersionDetails(versionDetails()) }
val pluginVersion = gitDerivedBuildVersion.map { buildVersion -> buildVersion.pluginVersion }
val pluginArchiveVersion = gitDerivedBuildVersion.map { buildVersion -> buildVersion.archiveVersion }
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
val detektBaselineFile = layout.projectDirectory.file("config/detekt/baseline.xml")

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
    integrationTestImplementation("org.kodein.di:kodein-di-jvm:7.32.0")
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
    baseline = detektBaselineFile.asFile
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

val verifyDetektBaseline by tasks.registering(VerifyDetektBaselineTask::class) {
    group = "verification"
    description = "Verifies the Detekt baseline contains no suppressed issues."
    baselineFile.set(detektBaselineFile)
}

tasks.check {
    dependsOn(verifyDetektBaseline)
}

testlogger {
    theme = ThemeType.MOCHA
    showPassed = true
    showSkipped = true
    showFailed = true
    showSummary = true
    showStandardStreams = false
    showExceptions = true
    showCauses = true
    showStackTraces = true
    showFullStackTraces = false
    slowThreshold = 2_000
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

tasks.buildPlugin {
    archiveVersion.set(pluginArchiveVersion)
}

val releaseMatrixSmokeTag = "releaseMatrixSmoke"
val releaseMatrixSmokeProductCodes = setOf("PY", "WS")
val releaseMatrixSupportedProductCodes = setOf("IU") + releaseMatrixSmokeProductCodes
val releaseMatrixLegacyProductAliases = mapOf(
    "IIU" to "IU",
    "PCP" to "PY",
)

fun releaseMatrixProductCode(products: String): String {
    val productCodes = products.split(',')
        .map { product -> product.trim() }
        .filter { product -> product.isNotEmpty() }

    if (productCodes.size != 1) {
        throw GradleException(
            "releaseMatrixUiTest runs one IDE product per invocation. " +
                "Use -PideProducts=IU for the full IDEA lane or -PideProducts=PY/-PideProducts=WS for smoke lanes.",
        )
    }

    val productCode = productCodes.single().uppercase(Locale.ROOT)
    val canonicalProductCode = releaseMatrixLegacyProductAliases[productCode] ?: productCode
    if (canonicalProductCode !in releaseMatrixSupportedProductCodes) {
        throw GradleException(
            "releaseMatrixUiTest supports IU as the full lane and PY or WS as smoke lanes, not '$products'.",
        )
    }
    return canonicalProductCode
}

val releaseMatrixIdeProductCode = releaseMatrixIdeProducts.map(::releaseMatrixProductCode)

val releaseMatrixUiTest by intellijPlatformTesting.testIdeUi.registering {
    type.set(
        releaseMatrixIdeProductCode.zip(releaseMatrixIdeVersion) { productCode, version ->
            IntelliJPlatformType.fromCode(productCode, version)
        },
    )
    version.set(releaseMatrixIdeVersion)

    task {
        val selectedProductCode = releaseMatrixIdeProductCode.get()
        group = "verification"
        description = "Runs release-matrix UI automation with Starter and Driver."
        testClassesDirs = integrationTestSourceSet.output.classesDirs
        classpath = integrationTestSourceSet.runtimeClasspath
        useJUnitPlatform {
            if (selectedProductCode in releaseMatrixSmokeProductCodes) {
                includeTags(releaseMatrixSmokeTag)
            }
        }
        exclude("pl/devopssolutions/aicommitall/integration/fakeai/**")
        shouldRunAfter(tasks.test)
        dependsOn(fakeAiAssistantPlugin)
        systemProperty("aicommitall.ide.product", selectedProductCode)
        systemProperty("aicommitall.ide.products", selectedProductCode)
        systemProperty("aicommitall.ide.version", releaseMatrixIdeVersion.get())
        systemProperty(
            "aicommitall.fake.ai.plugin.path",
            fakeAiAssistantPluginArchiveFile.get().asFile.absolutePath,
        )
    }
}

spotless {
    kotlin {
        target("src/**/*.kt", "buildSrc/src/**/*.kt")
        ktlint("1.8.0")
        licenseHeaderFile(rootProject.file("config/spotless/apache-kotlin.license"), "package ")
    }

    kotlinGradle {
        target("*.gradle.kts", "gradle/**/*.gradle.kts", "buildSrc/**/*.gradle.kts")
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
