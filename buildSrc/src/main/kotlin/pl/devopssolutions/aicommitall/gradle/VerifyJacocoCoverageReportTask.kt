/*
 * Copyright 2026 DevOps Solutions Kamil Kiewisz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pl.devopssolutions.aicommitall.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.w3c.dom.Element
import java.util.Locale

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
        val root = parseXmlDocumentElement(reportFile.get().asFile)
        val coveredInstructions = counter(root, "INSTRUCTION").covered

        if (coveredInstructions <= 0) {
            throw GradleException(
                "JaCoCo XML report contains zero covered instructions. " +
                    "Check the test JaCoCo agent and report class directories.",
            )
        }

        verifyMinimumCoverage(
            label = "line",
            actual = counter(root, "LINE").ratio,
            minimum = minimumLineCoverage.get(),
        )
        verifyMinimumCoverage(
            label = "branch",
            actual = counter(root, "BRANCH").ratio,
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
