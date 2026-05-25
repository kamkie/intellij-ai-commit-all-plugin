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

import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
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

        val committableStatus = repository.statusLines()
        val ignoredStatus = repository.statusLines("--ignored")

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

        val firstStatus = firstRoot.statusLines()
        val secondStatus = secondRoot.statusLines()

        assertContains(firstStatus, " M tracked-a.txt")
        assertContains(secondStatus, "?? unversioned-b.txt")
    }

    @Test
    fun `already staged files stay staged when unstaged files are added`() {
        assumeTrue(GitCli.isAvailable(), "git executable is required for local-repository validation")
        val repository = LocalGitRepository.init(tempDirectory.resolve("mixed-staging"))
        repository.write("already-staged.txt", "original\n")
        repository.write("unstaged.txt", "original\n")
        repository.git("add", ".")
        repository.git("commit", "-m", "initial")

        repository.write("already-staged.txt", "staged change\n")
        repository.git("add", "already-staged.txt")
        repository.write("unstaged.txt", "unstaged change\n")
        repository.write("new-file.txt", "new\n")
        repository.git("add", "--all")

        val status = repository.statusLines()
        assertContains(status, "M  already-staged.txt")
        assertContains(status, "A  new-file.txt")
        assertContains(status, "M  unstaged.txt")
    }

    @Test
    fun `all intended file states can already be staged before the workflow runs`() {
        assumeTrue(GitCli.isAvailable(), "git executable is required for local-repository validation")
        val repository = LocalGitRepository.init(tempDirectory.resolve("all-staged"))
        repository.write("modified.txt", "original\n")
        repository.write("delete-me.txt", "delete\n")
        repository.write("rename-source.txt", "rename\n")
        repository.git("add", ".")
        repository.git("commit", "-m", "initial")

        repository.write("modified.txt", "modified\n")
        repository.delete("delete-me.txt")
        repository.git("mv", "rename-source.txt", "rename-target.txt")
        repository.write("unversioned.txt", "new\n")
        repository.git("add", "--all")

        val status = repository.statusLines()
        assertContains(status, "M  modified.txt")
        assertContains(status, "D  delete-me.txt")
        assertContains(status, "R  rename-source.txt -> rename-target.txt")
        assertContains(status, "A  unversioned.txt")
    }

    @Test
    fun `local repository fixture documents rename and move porcelain shapes`() {
        assumeTrue(GitCli.isAvailable(), "git executable is required for local-repository validation")
        val stagedRename = LocalGitRepository.init(tempDirectory.resolve("staged-rename"))
        stagedRename.write("docs/old-name.md", "line one\nline two\n")
        stagedRename.git("add", ".")
        stagedRename.git("commit", "-m", "initial")
        stagedRename.git("mv", "docs/old-name.md", "docs/new-name.md")

        val partialRename = LocalGitRepository.init(tempDirectory.resolve("partial-rename"))
        partialRename.write("docs/old-name.md", "line one\nline two\n")
        partialRename.git("add", ".")
        partialRename.git("commit", "-m", "initial")
        partialRename.git("mv", "docs/old-name.md", "docs/new-name.md")
        partialRename.write("docs/new-name.md", "line one\nline two\nworktree edit\n")

        val unstagedMove = LocalGitRepository.init(tempDirectory.resolve("unstaged-move"))
        unstagedMove.write("docs/old-location.md", "line one\nline two\n")
        unstagedMove.git("add", ".")
        unstagedMove.git("commit", "-m", "initial")
        unstagedMove.delete("docs/old-location.md")
        unstagedMove.write("docs/new-location.md", "line one\nline two\n")

        assertEquals(listOf("R  docs/old-name.md -> docs/new-name.md"), stagedRename.statusLines("-M"))
        assertEquals(listOf("RM docs/old-name.md -> docs/new-name.md"), partialRename.statusLines("-M"))
        assertEquals(
            listOf(
                " D docs/old-location.md",
                "?? docs/new-location.md",
            ),
            unstagedMove.statusLines("-M"),
        )
    }

    @Test
    fun `unstaged fixture can stage all eligible paths without ignored files`() {
        assumeTrue(GitCli.isAvailable(), "git executable is required for local-repository validation")
        val repository = LocalGitRepository.init(tempDirectory.resolve("unstaged-to-staged"))
        repository.write("modified.txt", "original\n")
        repository.write(".gitignore", "ignored.txt\n")
        repository.git("add", ".")
        repository.git("commit", "-m", "initial")

        repository.write("modified.txt", "modified\n")
        repository.write("unversioned.txt", "new\n")
        repository.write("ignored.txt", "ignored\n")
        assertContains(repository.statusLines(), " M modified.txt")
        assertContains(repository.statusLines(), "?? unversioned.txt")

        repository.git("add", "--all")

        val status = repository.statusLines()
        assertContains(status, "M  modified.txt")
        assertContains(status, "A  unversioned.txt")
        assertFalse(
            status.any { line -> line.contains("ignored.txt") },
            "Ignored files must remain outside the staged fixture.",
        )
    }

    @Test
    fun `multi-root nested repository fixture preserves staged paths per root`() {
        assumeTrue(GitCli.isAvailable(), "git executable is required for local-repository validation")
        val firstRoot = LocalGitRepository.init(tempDirectory.resolve("nested-root-a"))
        val secondRoot = LocalGitRepository.init(tempDirectory.resolve("nested-root-b"))
        firstRoot.write("modules/core/build.gradle.kts", "plugins {}\n")
        secondRoot.write("products/webstorm/plugin/src/Main.kt", "fun main() {}\n")
        firstRoot.git("add", ".")
        secondRoot.git("add", ".")
        firstRoot.git("commit", "-m", "initial a")
        secondRoot.git("commit", "-m", "initial b")

        firstRoot.write("modules/core/build.gradle.kts", "plugins { kotlin(\"jvm\") }\n")
        firstRoot.write("products/idea/plugin/src/Main.kt", "fun idea() {}\n")
        secondRoot.write("products/webstorm/plugin/src/Main.kt", "fun webstorm() {}\n")
        firstRoot.git("add", "--all")
        secondRoot.git("add", "--all")

        assertContains(firstRoot.statusLines(), "M  modules/core/build.gradle.kts")
        assertContains(firstRoot.statusLines(), "A  products/idea/plugin/src/Main.kt")
        assertContains(secondRoot.statusLines(), "M  products/webstorm/plugin/src/Main.kt")
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

    @Test
    fun `local repository fixture detects a missing tracked upstream`() {
        assumeTrue(GitCli.isAvailable(), "git executable is required for local-repository validation")
        val repository = LocalGitRepository.init(tempDirectory.resolve("missing-upstream"))
        repository.write("README.md", "initial\n")
        repository.git("add", ".")
        repository.git("commit", "-m", "initial")

        val upstream = repository.gitAllowingFailure("rev-parse", "--abbrev-ref", "--symbolic-full-name", "@{u}")

        assertTrue(upstream.exitCode != 0)
    }

    @Test
    fun `local repository fixture detects diverged local and upstream hashes`() {
        assumeTrue(GitCli.isAvailable(), "git executable is required for local-repository validation")
        val remote = LocalGitRepository.initBare(tempDirectory.resolve("diverged-remote.git"))
        val repository = LocalGitRepository.init(tempDirectory.resolve("diverged-work"))
        repository.git("checkout", "-b", "main")
        repository.git("remote", "add", "origin", remote.root.toString())
        repository.write("README.md", "initial\n")
        repository.git("add", ".")
        repository.git("commit", "-m", "initial")
        repository.git("push", "-u", "origin", "HEAD:main")

        val secondClone = LocalGitRepository.clone(remote.root, tempDirectory.resolve("diverged-other"))
        secondClone.git("checkout", "main")
        secondClone.write("remote.txt", "remote change\n")
        secondClone.git("add", ".")
        secondClone.git("commit", "-m", "remote change")
        secondClone.git("push", "origin", "HEAD:main")

        repository.write("local.txt", "local change\n")
        repository.git("add", ".")
        repository.git("commit", "-m", "local change")

        val localHead = repository.git("rev-parse", "HEAD").stdout.trim()
        val remoteHead = remote.git("rev-parse", "refs/heads/main").stdout.trim()
        assertTrue(localHead != remoteHead)
    }
}
