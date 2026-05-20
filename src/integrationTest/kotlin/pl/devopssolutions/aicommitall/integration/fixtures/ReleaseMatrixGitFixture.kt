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
package pl.devopssolutions.aicommitall.integration.fixtures

import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.TimeUnit
import kotlin.io.path.createDirectories
import kotlin.io.path.deleteIfExists
import kotlin.io.path.writeText

internal data class ReleaseMatrixGitFixture(
    val projectDirectory: Path,
    val primaryRepository: IntegrationGitRepository,
    val secondaryRepository: IntegrationGitRepository,
    val bareRemote: IntegrationGitRepository,
)

internal object ReleaseMatrixGitFixtureBuilder {
    fun create(baseDirectory: Path): ReleaseMatrixGitFixture {
        val projectDirectory = baseDirectory.resolve("release-matrix-project")
        val bareRemote = IntegrationGitRepository.initBare(baseDirectory.resolve("origin.git"))
        val primaryRepository = IntegrationGitRepository.init(projectDirectory.resolve("root-a"))
        val secondaryRepository = IntegrationGitRepository.init(projectDirectory.resolve("root-b"))

        configurePrimaryRepository(primaryRepository, bareRemote)
        configureSecondaryRepository(secondaryRepository)

        return ReleaseMatrixGitFixture(
            projectDirectory = projectDirectory,
            primaryRepository = primaryRepository,
            secondaryRepository = secondaryRepository,
            bareRemote = bareRemote,
        )
    }

    private fun configurePrimaryRepository(
        repository: IntegrationGitRepository,
        bareRemote: IntegrationGitRepository,
    ) {
        repository.write("modified.txt", "original\n")
        repository.write("delete-me.txt", "delete\n")
        repository.write("rename-source.txt", "rename\n")
        repository.write("already-staged.txt", "original staged\n")
        repository.write(".gitignore", "ignored.txt\n")
        repository.git("add", ".")
        repository.git("commit", "-m", "initial primary")
        repository.git("branch", "-M", "main")
        repository.git("remote", "add", "origin", bareRemote.root.toString())
        repository.git("push", "-u", "origin", "HEAD:main")

        repository.write("modified.txt", "modified\n")
        repository.delete("delete-me.txt")
        repository.git("mv", "rename-source.txt", "rename-target.txt")
        repository.write("unversioned.txt", "unversioned\n")
        repository.write("ignored.txt", "ignored\n")
        repository.write("already-staged.txt", "staged change\n")
        repository.git("add", "already-staged.txt")
    }

    private fun configureSecondaryRepository(repository: IntegrationGitRepository) {
        repository.write("secondary-tracked.txt", "secondary original\n")
        repository.git("add", ".")
        repository.git("commit", "-m", "initial secondary")
        repository.write("secondary-tracked.txt", "secondary modified\n")
        repository.write("secondary-unversioned.txt", "secondary new\n")
    }
}

internal class IntegrationGitRepository private constructor(val root: Path) {
    fun git(vararg arguments: String): IntegrationGitResult = IntegrationGitCli.run(root, *arguments)

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
        fun init(root: Path): IntegrationGitRepository {
            Files.createDirectories(root)
            val repository = IntegrationGitRepository(root)
            repository.git("init")
            repository.git("config", "user.email", "release-matrix@example.invalid")
            repository.git("config", "user.name", "AI Commit All Release Matrix")
            return repository
        }

        fun initBare(root: Path): IntegrationGitRepository {
            Files.createDirectories(root.parent)
            IntegrationGitCli.run(root.parent, "init", "--bare", root.fileName.toString())
            return IntegrationGitRepository(root)
        }
    }
}

internal object IntegrationGitCli {
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
    ): IntegrationGitResult {
        val command = listOf("git", "-C", workingDirectory.toString()) + arguments
        val process = ProcessBuilder(command)
            .redirectErrorStream(true)
            .start()
        val output = process.inputStream.bufferedReader().use { reader -> reader.readText() }
        val finished = process.waitFor(30, TimeUnit.SECONDS)
        check(finished) {
            "Timed out running `${command.joinToString(" ")}`."
        }
        check(process.exitValue() == 0) {
            "Command `${command.joinToString(" ")}` failed with exit ${process.exitValue()}:\n$output"
        }
        return IntegrationGitResult(stdout = output)
    }
}

internal data class IntegrationGitResult(
    val stdout: String,
)
