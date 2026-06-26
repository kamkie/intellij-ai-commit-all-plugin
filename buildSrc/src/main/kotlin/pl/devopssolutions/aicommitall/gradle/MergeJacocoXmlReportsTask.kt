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
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.w3c.dom.Document
import org.w3c.dom.Element
import java.io.File
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

abstract class MergeJacocoXmlReportsTask : DefaultTask() {
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val reportFiles: ConfigurableFileCollection

    @get:OutputFile
    abstract val outputReportFile: RegularFileProperty

    @TaskAction
    fun merge() {
        val existingReports = reportFiles.files
            .filter { file -> file.isFile }
            .sortedBy { file -> file.absolutePath }

        if (existingReports.isEmpty()) {
            throw GradleException("No JaCoCo XML reports were found to merge.")
        }

        val document = parseXmlDocument(existingReports.first())
        val targetReport = document.documentElement
        targetReport.setAttribute("name", "ai-commit-all-aggregate")

        existingReports.drop(1).forEach { reportFile ->
            mergeReport(targetReport, parseXmlDocument(reportFile).documentElement)
        }
        recalculateReportCounters(targetReport)
        writeXml(document, outputReportFile.get().asFile)
    }

    private fun mergeReport(
        targetReport: Element,
        incomingReport: Element,
    ) {
        incomingReport.childElements("sessioninfo").forEach { sessionInfo ->
            targetReport.importChild(sessionInfo, beforeTagNames = setOf("package", "counter"))
        }
        incomingReport.childElements("package").forEach { incomingPackage ->
            val packageName = incomingPackage.getAttribute("name")
            val targetPackage = targetReport.findChildElement("package", "name", packageName)
            if (targetPackage == null) {
                targetReport.importChild(incomingPackage, beforeTagNames = setOf("counter"))
            } else {
                mergePackage(targetPackage, incomingPackage)
            }
        }
    }

    private fun mergePackage(
        targetPackage: Element,
        incomingPackage: Element,
    ) {
        incomingPackage.childElements("class").forEach { incomingClass ->
            val className = incomingClass.getAttribute("name")
            val targetClass = targetPackage.findChildElement("class", "name", className)
            if (targetClass == null) {
                targetPackage.importChild(incomingClass, beforeTagNames = setOf("sourcefile", "counter"))
            } else {
                mergeClass(targetClass, incomingClass)
            }
        }

        incomingPackage.childElements("sourcefile").forEach { incomingSourceFile ->
            val sourceFileName = incomingSourceFile.getAttribute("name")
            val targetSourceFile = targetPackage.findChildElement("sourcefile", "name", sourceFileName)
            if (targetSourceFile == null) {
                targetPackage.importChild(incomingSourceFile, beforeTagNames = setOf("counter"))
            } else {
                mergeSourceFile(targetSourceFile, incomingSourceFile)
            }
        }

        mergeCounters(targetPackage, incomingPackage)
    }

    private fun mergeClass(
        targetClass: Element,
        incomingClass: Element,
    ) {
        incomingClass.childElements("method").forEach { incomingMethod ->
            val targetMethod = targetClass.childElements("method").firstOrNull { method ->
                method.getAttribute("name") == incomingMethod.getAttribute("name") &&
                    method.getAttribute("desc") == incomingMethod.getAttribute("desc") &&
                    method.getAttribute("line") == incomingMethod.getAttribute("line")
            }
            if (targetMethod == null) {
                targetClass.importChild(incomingMethod, beforeTagNames = setOf("counter"))
            } else {
                mergeCounters(targetMethod, incomingMethod)
            }
        }

        mergeCounters(targetClass, incomingClass)
        if (targetClass.childElements("method").isNotEmpty()) {
            recalculateClassCounters(targetClass)
        }
    }

    private fun mergeSourceFile(
        targetSourceFile: Element,
        incomingSourceFile: Element,
    ) {
        incomingSourceFile.childElements("line").forEach { incomingLine ->
            val targetLine = targetSourceFile.findChildElement("line", "nr", incomingLine.getAttribute("nr"))
            if (targetLine == null) {
                targetSourceFile.importChild(incomingLine, beforeTagNames = setOf("counter"))
            } else {
                mergeLine(targetLine, incomingLine)
            }
        }

        mergeCounters(targetSourceFile, incomingSourceFile)
        recalculateSourceFileCounters(targetSourceFile)
    }

    private fun mergeLine(
        targetLine: Element,
        incomingLine: Element,
    ) {
        listOf("mi", "mb").forEach { attribute ->
            targetLine.setAttribute(
                attribute,
                minOf(targetLine.longAttribute(attribute), incomingLine.longAttribute(attribute)).toString(),
            )
        }
        listOf("ci", "cb").forEach { attribute ->
            targetLine.setAttribute(
                attribute,
                maxOf(targetLine.longAttribute(attribute), incomingLine.longAttribute(attribute)).toString(),
            )
        }
    }

    private fun mergeCounters(
        target: Element,
        incoming: Element,
    ) {
        incoming.childElements("counter").forEach { incomingCounter ->
            val type = incomingCounter.getAttribute("type")
            val targetCounter = target.counterElement(type)
            if (targetCounter == null) {
                target.importChild(incomingCounter)
            } else {
                targetCounter.setAttribute(
                    "missed",
                    minOf(targetCounter.longAttribute("missed"), incomingCounter.longAttribute("missed")).toString(),
                )
                targetCounter.setAttribute(
                    "covered",
                    maxOf(targetCounter.longAttribute("covered"), incomingCounter.longAttribute("covered")).toString(),
                )
            }
        }
    }

    private fun recalculateReportCounters(report: Element) {
        report.childElements("package").forEach(::recalculatePackageCounters)
        COUNTER_TYPES.forEach { type ->
            report.setCounter(type, report.childElements("package").sumCounter(type))
        }
    }

    private fun recalculatePackageCounters(packageElement: Element) {
        packageElement.childElements("class").forEach { classElement ->
            if (classElement.childElements("method").isNotEmpty()) {
                recalculateClassCounters(classElement)
            }
        }
        packageElement.childElements("sourcefile").forEach(::recalculateSourceFileCounters)

        listOf("INSTRUCTION", "BRANCH", "LINE").forEach { type ->
            packageElement.setCounter(type, packageElement.childElements("sourcefile").sumCounter(type))
        }
        listOf("COMPLEXITY", "METHOD", "CLASS").forEach { type ->
            packageElement.setCounter(type, packageElement.childElements("class").sumCounter(type))
        }
    }

    private fun recalculateClassCounters(classElement: Element) {
        listOf("INSTRUCTION", "BRANCH", "LINE", "COMPLEXITY", "METHOD").forEach { type ->
            classElement.setCounter(type, classElement.childElements("method").sumCounter(type))
        }
        val instructionCounter = classElement.counter("INSTRUCTION") ?: CoverageCounter.ZERO
        classElement.setCounter(
            "CLASS",
            if (instructionCounter.covered > 0) {
                CoverageCounter(missed = 0, covered = 1)
            } else {
                CoverageCounter(missed = 1, covered = 0)
            },
        )
    }

    private fun recalculateSourceFileCounters(sourceFile: Element) {
        val lines = sourceFile.childElements("line")
        sourceFile.setCounter(
            "INSTRUCTION",
            CoverageCounter(
                missed = lines.sumOf { line -> line.longAttribute("mi") },
                covered = lines.sumOf { line -> line.longAttribute("ci") },
            ),
        )
        sourceFile.setCounter(
            "BRANCH",
            CoverageCounter(
                missed = lines.sumOf { line -> line.longAttribute("mb") },
                covered = lines.sumOf { line -> line.longAttribute("cb") },
            ),
        )
        sourceFile.setCounter(
            "LINE",
            CoverageCounter(
                missed = lines.count { line -> line.longAttribute("ci") == 0L }.toLong(),
                covered = lines.count { line -> line.longAttribute("ci") > 0L }.toLong(),
            ),
        )
    }

    private fun writeXml(
        document: Document,
        outputFile: File,
    ) {
        outputFile.parentFile.deleteRecursively()
        outputFile.parentFile.mkdirs()

        val transformer = TransformerFactory.newInstance().newTransformer()
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8")
        transformer.setOutputProperty(OutputKeys.STANDALONE, "yes")
        transformer.setOutputProperty(OutputKeys.INDENT, "no")
        document.doctype?.publicId?.let { publicId -> transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, publicId) }
        document.doctype?.systemId?.let { systemId -> transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, systemId) }
        transformer.transform(DOMSource(document), StreamResult(outputFile))
    }

    private fun Element.importChild(
        child: Element,
        beforeTagNames: Set<String> = emptySet(),
    ): Element {
        val imported = ownerDocument.importNode(child, true) as Element
        val reference = childElements().firstOrNull { element -> element.tagName in beforeTagNames }
        if (reference == null) {
            appendChild(imported)
        } else {
            insertBefore(imported, reference)
        }
        return imported
    }

    private fun Element.setCounter(
        type: String,
        counter: CoverageCounter,
    ) {
        val counterElement = counterElement(type) ?: ownerDocument.createElement("counter").also { element ->
            element.setAttribute("type", type)
            appendChild(element)
        }
        counterElement.setAttribute("missed", counter.missed.toString())
        counterElement.setAttribute("covered", counter.covered.toString())
    }

    private fun List<Element>.sumCounter(type: String): CoverageCounter = fold(CoverageCounter.ZERO) { total, element ->
        total + (element.counter(type) ?: CoverageCounter.ZERO)
    }

    private fun Element.counter(type: String): CoverageCounter? = childElements("counter")
        .firstOrNull { counter -> counter.getAttribute("type") == type }
        ?.let { counter ->
            CoverageCounter(
                missed = counter.longAttribute("missed"),
                covered = counter.longAttribute("covered"),
            )
        }

    private fun Element.counterElement(type: String): Element? = childElements("counter")
        .firstOrNull { counter -> counter.getAttribute("type") == type }

    private fun Element.findChildElement(
        tagName: String,
        attributeName: String,
        attributeValue: String,
    ): Element? = childElements(tagName)
        .firstOrNull { child -> child.getAttribute(attributeName) == attributeValue }

    private fun Element.childElements(tagName: String? = null): List<Element> {
        val children = childNodes
        return (0 until children.length)
            .asSequence()
            .map { index -> children.item(index) }
            .filterIsInstance<Element>()
            .filter { element -> tagName == null || element.tagName == tagName }
            .toList()
    }

    private fun Element.longAttribute(name: String): Long = getAttribute(name)
        .takeIf { value -> value.isNotEmpty() }
        ?.toLong()
        ?: 0

    private data class CoverageCounter(
        val missed: Long,
        val covered: Long,
    ) {
        operator fun plus(other: CoverageCounter): CoverageCounter = CoverageCounter(
            missed = missed + other.missed,
            covered = covered + other.covered,
        )

        companion object {
            val ZERO = CoverageCounter(missed = 0, covered = 0)
        }
    }

    private companion object {
        val COUNTER_TYPES = listOf("INSTRUCTION", "BRANCH", "LINE", "COMPLEXITY", "METHOD", "CLASS")
    }
}
