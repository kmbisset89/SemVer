package io.github.kmbisset89.semver.plugin.logic

import org.eclipse.jgit.api.Git
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.platform.suite.api.Suite
import java.nio.file.Files
import java.nio.file.Path

@Suite
class ChangeDetectionUseCaseTest {

    @Test
    @DisplayName("Detects changes under module paths since global baseline when no module tag exists")
    fun detectsChangesSinceGlobalWhenNoModuleTag() {
        val tempDir = Files.createTempDirectory("semver-test").toFile()
        val git = Git.init().setDirectory(tempDir).call()

        // initial commit
        writeFile(tempDir.toPath().resolve("README.md"), "root")
        git.add().addFilepattern("README.md").call()
        git.commit().setMessage("init").call()

        // global baseline tag
        git.tag().setName("v0.1.0").call()

        // module change
        tempDir.toPath().resolve("api/src/main/kotlin").toFile().mkdirs()
        writeFile(tempDir.toPath().resolve("api/src/main/kotlin/Foo.kt"), "class Foo")
        git.add().addFilepattern("api").call()
        git.commit().setMessage("api change").call()

        val changed = ChangeDetectionUseCase().invoke(
            gitRoot = tempDir.absolutePath,
            moduleTagSuffix = "api",
            pathsToCheck = listOf("api", "api/src/main/kotlin")
        )
        assertTrue(changed)
    }

    @Test
    @DisplayName("Uses last module tag as baseline and ignores unrelated changes")
    fun usesModuleTagBaseline() {
        val tempDir = Files.createTempDirectory("semver-test").toFile()
        val git = Git.init().setDirectory(tempDir).call()

        // initial commit
        writeFile(tempDir.toPath().resolve("README.md"), "root")
        git.add().addFilepattern("README.md").call()
        val c1 = git.commit().setMessage("init").call()

        // module baseline tag
        git.tag().setName("v0.1.0-api").call()

        // unrelated change outside module
        writeFile(tempDir.toPath().resolve("docs/NOTE.md"), "note")
        git.add().addFilepattern("docs").call()
        git.commit().setMessage("docs change").call()

        val changedUnrelated = ChangeDetectionUseCase().invoke(
            gitRoot = tempDir.absolutePath,
            moduleTagSuffix = "api",
            pathsToCheck = listOf("api")
        )
        assertFalse(changedUnrelated)

        // now module change
        tempDir.toPath().resolve("api/src").toFile().mkdirs()
        writeFile(tempDir.toPath().resolve("api/src/Bar.kt"), "class Bar")
        git.add().addFilepattern("api").call()
        git.commit().setMessage("api change").call()

        val changedRelated = ChangeDetectionUseCase().invoke(
            gitRoot = tempDir.absolutePath,
            moduleTagSuffix = "api",
            pathsToCheck = listOf("api")
        )
        assertTrue(changedRelated)
    }

    private fun writeFile(path: Path, content: String) {
        path.parent?.toFile()?.mkdirs()
        Files.write(path, content.toByteArray())
    }
}


