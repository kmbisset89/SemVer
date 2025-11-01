package io.github.kmbisset89.semver.plugin

import io.github.kmbisset89.semver.plugin.logic.DetermineCurrentVersion
import io.github.kmbisset89.semver.plugin.logic.SemVer
import io.mockk.every
import io.mockk.mockk
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.revwalk.RevWalk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.platform.suite.api.Suite

@Suite
class DetermineCurrentVersionTest {

    @Nested
    @DisplayName("Given a DetermineCurrentVersion instance")
    inner class DetermineCurrentVersionInstance {

        @Nested
        @DisplayName("When determineCurrentVersion is called from a mocked git repository")
        inner class DetermineCurrentVersionCalledMocked {

            @Test
            @DisplayName("Then it returns the default version number")
            fun `determineCurrentVersion returns the default version number`() {
                val determineCurrentVersion = DetermineCurrentVersion()
                val repositoryMock = mockk<Repository>().also {
                    every { it.findRef("root") } returns mockk()
                    every { it.resolve("root") } returns mockk()
                }
                val gitMock = mockk<Git>().also {
                    every { it.tagList().call() } returns emptyList()
                }
                val revWalkMock = mockk<RevWalk>().also {
                    every { it.revFilter = any() } returns Unit
                    every { it.parseCommit(any()) } returns mockk()
                    every { it.isMergedInto(any(), any()) } returns true
                }
                val mockFactory: (String) -> Repository = {
                    repositoryMock
                }
                val revWalkFactory: (Repository) -> RevWalk = { revWalkMock }
                val mockGitFactory: (Repository) -> Git = { gitMock }
                val version = determineCurrentVersion.determineCurrentVersion(
                    "C:\\dev\\git\\HERC",
                    "root",
                    mockk(),
                    null,
                    mockFactory,
                    mockGitFactory,
                    revWalkFactory
                )
                assertEquals(SemVer.Default, version)
            }
        }
    }
}
