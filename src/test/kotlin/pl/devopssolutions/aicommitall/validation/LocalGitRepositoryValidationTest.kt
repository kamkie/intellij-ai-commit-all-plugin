package pl.devopssolutions.aicommitall.validation

import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.deleteIfExists
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class LocalGitRepositoryValidationTest {
    @TempDir
    lateinit var tempDirectory: Path

    @Test
    fun `local repositories cover committable file states without ignored files`() {
        assumeTrue(GitCli.isAvailable(), "git executable is required for local-repository validation")
        val repository = LocalGitRepository.init(tempDirectory.resolve("repo"))
        repository.write("tracked.txt", "original\n")
        repository.write("delete-me.txt", "delete\n")
        repository.write("rename-source.txt", "rename\n")
        repository.write(".gitignore", "ignored.txt\n")
        repository.git("add", ".")
        repository.git("commit", "-m", "initial")

        repository.write("tracked.txt", "modified\n")
        repository.write("staged-added.txt", "added\n")
        repository.git("add", "staged-added.txt")
        repository.delete("delete-me.txt")
        repository.git("mv", "rename-source.txt", "rename-target.txt")
        repository.write("unversioned.txt", "unversioned\n")
        repository.write("ignored.txt", "ignored\n")

        val committableStatus = repository.git("status", "--porcelain").stdout.lines()
            .filter { line -> line.isNotBlank() }
        val ignoredStatus = repository.git("status", "--porcelain", "--ignored").stdout.lines()
            .filter { line -> line.isNotBlank() }

        assertContains(committableStatus, " M tracked.txt")
        assertContains(committableStatus, "A  staged-added.txt")
        assertContains(committableStatus, " D delete-me.txt")
        assertContains(committableStatus, "R  rename-source.txt -> rename-target.txt")
        assertContains(committableStatus, "?? unversioned.txt")
        assertFalse(
            committableStatus.any { line -> line.contains("ignored.txt") },
            "Ignored files must not appear in the committable status fixture.",
        )
        assertContains(ignoredStatus, "!! ignored.txt")
    }

    @Test
    fun `multi-root local repository fixture covers independent git roots`() {
        assumeTrue(GitCli.isAvailable(), "git executable is required for local-repository validation")
        val firstRoot = LocalGitRepository.init(tempDirectory.resolve("root-a"))
        val secondRoot = LocalGitRepository.init(tempDirectory.resolve("root-b"))
        firstRoot.write("tracked-a.txt", "a\n")
        secondRoot.write("tracked-b.txt", "b\n")
        firstRoot.git("add", ".")
        secondRoot.git("add", ".")
        firstRoot.git("commit", "-m", "initial a")
        secondRoot.git("commit", "-m", "initial b")

        firstRoot.write("tracked-a.txt", "changed a\n")
        secondRoot.write("unversioned-b.txt", "new b\n")

        val firstStatus = firstRoot.git("status", "--porcelain").stdout.lines()
            .filter { line -> line.isNotBlank() }
        val secondStatus = secondRoot.git("status", "--porcelain").stdout.lines()
            .filter { line -> line.isNotBlank() }

        assertContains(firstStatus, " M tracked-a.txt")
        assertContains(secondStatus, "?? unversioned-b.txt")
    }

    @Test
    fun `commit and push validation uses only temporary local remotes`() {
        assumeTrue(GitCli.isAvailable(), "git executable is required for local-repository validation")
        val remote = LocalGitRepository.initBare(tempDirectory.resolve("remote.git"))
        val repository = LocalGitRepository.init(tempDirectory.resolve("work"))
        repository.git("remote", "add", "origin", remote.root.toString())
        repository.write("README.md", "initial\n")
        repository.git("add", ".")
        repository.git("commit", "-m", "initial")
        repository.git("push", "origin", "HEAD:main")

        repository.write("README.md", "updated by validation\n")
        repository.git("add", ".")
        repository.git("commit", "-m", "AI Commit All local validation")
        repository.git("push", "origin", "HEAD:main")

        assertEquals(remote.root.toString(), repository.git("remote", "get-url", "origin").stdout.trim())
        assertTrue(
            remote.git("log", "--oneline", "--all").stdout.contains("AI Commit All local validation"),
            "The validation push should land in the temporary bare remote.",
        )
    }

    private class LocalGitRepository private constructor(val root: Path) {
        fun git(vararg arguments: String): GitResult = GitCli.run(root, *arguments)

        fun write(relativePath: String, content: String) {
            val file = root.resolve(relativePath)
            file.parent?.createDirectories()
            file.writeText(content)
        }

        fun delete(relativePath: String) {
            root.resolve(relativePath).deleteIfExists()
        }

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
        }
    }

    private object GitCli {
        fun isAvailable(): Boolean =
            runCatching {
                val process = ProcessBuilder("git", "--version")
                    .redirectErrorStream(true)
                    .start()
                process.waitFor()
                process.exitValue() == 0
            }.getOrDefault(false)

        fun run(
            workingDirectory: Path,
            vararg arguments: String,
        ): GitResult {
            val command = listOf("git", "-C", workingDirectory.toString()) + arguments
            val process = ProcessBuilder(command)
                .redirectErrorStream(true)
                .start()
            val output = process.inputStream.bufferedReader().use { reader -> reader.readText() }
            val finished = process.waitFor(30, java.util.concurrent.TimeUnit.SECONDS)
            check(finished) {
                "Timed out running `${command.joinToString(" ")}`."
            }
            check(process.exitValue() == 0) {
                "Command `${command.joinToString(" ")}` failed with exit ${process.exitValue()}:\n$output"
            }
            return GitResult(stdout = output)
        }
    }

    private data class GitResult(val stdout: String)
}
