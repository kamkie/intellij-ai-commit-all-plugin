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
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.w3c.dom.Element

abstract class VerifyDetektBaselineTask : DefaultTask() {
    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val baselineFile: RegularFileProperty

    @TaskAction
    fun verify() {
        val root = parseXmlDocumentElement(baselineFile.get().asFile)

        if (root.tagName != "SmellBaseline") {
            throw GradleException("Detekt baseline XML root must be SmellBaseline.")
        }

        val currentIssueCount = root.baselineSection("CurrentIssues").childElementCount()
        val manualSuppressionCount = root.baselineSection("ManuallySuppressedIssues").childElementCount()

        if (currentIssueCount > 0 || manualSuppressionCount > 0) {
            throw GradleException(
                "Detekt baseline must stay empty. Found $currentIssueCount current issues and " +
                    "$manualSuppressionCount manual suppressions in ${baselineFile.get().asFile}.",
            )
        }
    }

    private fun Element.baselineSection(tagName: String): Element {
        val sections = childElements(tagName)
        if (sections.size != 1) {
            throw GradleException("Detekt baseline XML must contain exactly one $tagName element.")
        }
        return sections.single()
    }

    private fun Element.childElementCount(): Int = childElements().size

    private fun Element.childElements(tagName: String? = null): List<Element> {
        val children = childNodes
        return (0 until children.length)
            .asSequence()
            .map { index -> children.item(index) }
            .filterIsInstance<Element>()
            .filter { child -> tagName == null || child.tagName == tagName }
            .toList()
    }
}
