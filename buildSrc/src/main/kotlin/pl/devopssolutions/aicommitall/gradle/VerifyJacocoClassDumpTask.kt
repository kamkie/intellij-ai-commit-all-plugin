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
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.io.File

abstract class VerifyJacocoClassDumpTask : DefaultTask() {
    @get:Input
    abstract val execFilePath: Property<String>

    @get:Input
    abstract val classDumpDirPath: Property<String>

    @TaskAction
    fun verify() {
        val exec = File(execFilePath.get())
        if (!exec.isFile) {
            return
        }

        val dumpDir = File(classDumpDirPath.get())
        if (!dumpDir.isDirectory || !dumpDir.walkTopDown().any { file -> file.isFile && file.extension == "class" }) {
            throw GradleException(
                "JaCoCo exec data exists at $exec, but no dumped classes were found in $dumpDir.",
            )
        }
    }
}
