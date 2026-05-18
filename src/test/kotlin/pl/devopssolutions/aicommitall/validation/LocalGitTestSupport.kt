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
package pl.devopssolutions.aicommitall.validation

import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.TimeUnit
import kotlin.io.path.createDirectories
import kotlin.io.path.deleteIfExists
import kotlin.io.path.writeText

internal class LocalGitRepository private constructor(val root: Path) {
    fun git(vararg arguments: String): GitResult = GitCli.run(root, *arguments)

    fun gitAllowingFailure(vararg arguments: String): GitResult = GitCli.run(root, *arguments, checkExit = false)

    fun write(relativePath: String, content: String) {
        val file = root.resolve(relativePath)
        file.parent?.createDirectories()
        file.writeText(content)
    }

    fun delete(relativePath: String) {
        root.resolve(relativePath).deleteIfExists()
    }

    fun statusLines(vararg arguments: String): List<String> = git("status", "--porcelain", *arguments).stdout.lines()
        .filter { line -> line.isNotBlank() }

    companion object {
        fun init(root: Path): LocalGitRepository {
            Files.createDirectories(root)
            val repository = LocalGitRepository(root)
            repository.git("init")
            repository.git("config", "user.email", "validation@example.invalid")
            repository.git("config", "user.name", "AI Commit All Validation")
            return repository
        }

        fun initBare(root: Path): LocalGitRepository {
            Files.createDirectories(root.parent)
            GitCli.run(root.parent, "init", "--bare", root.fileName.toString())
            return LocalGitRepository(root)
        }

        fun clone(remote: Path, root: Path): LocalGitRepository {
            Files.createDirectories(root.parent)
            GitCli.run(root.parent, "clone", remote.toString(), root.fileName.toString())
            val repository = LocalGitRepository(root)
            repository.git("config", "user.email", "validation@example.invalid")
            repository.git("config", "user.name", "AI Commit All Validation")
            return repository
        }
    }
}

internal object GitCli {
    fun isAvailable(): Boolean = runCatching {
        val process = ProcessBuilder("git", "--version")
            .redirectErrorStream(true)
            .start()
        process.waitFor()
        process.exitValue() == 0
    }.getOrDefault(false)

    fun run(
        workingDirectory: Path,
        vararg arguments: String,
        checkExit: Boolean = true,
    ): GitResult {
        val command = listOf("git", "-C", workingDirectory.toString()) + arguments
        val process = ProcessBuilder(command)
            .redirectErrorStream(true)
            .start()
        val output = process.inputStream.bufferedReader().use { reader -> reader.readText() }
        val finished = process.waitFor(30, TimeUnit.SECONDS)
        check(finished) {
            "Timed out running `${command.joinToString(" ")}`."
        }
        if (checkExit) {
            check(process.exitValue() == 0) {
                "Command `${command.joinToString(" ")}` failed with exit ${process.exitValue()}:\n$output"
            }
        }
        return GitResult(exitCode = process.exitValue(), stdout = output)
    }
}

internal data class GitResult(
    val exitCode: Int,
    val stdout: String,
)
