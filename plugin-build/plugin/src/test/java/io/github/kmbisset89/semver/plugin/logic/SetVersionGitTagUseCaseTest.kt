package io.github.kmbisset89.semver.plugin.logic

import io.github.kmbisset89.semver.plugin.logic.SemVer.Final
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.PushCommand
import org.eclipse.jgit.api.TagCommand
import org.eclipse.jgit.lib.Repository
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.platform.suite.api.Suite

@Suite
class SetVersionGitTagUseCaseTest {

    @Test
    @DisplayName("Creates suffix tag vX.Y.Z-module when subProjectTag provided")
    fun createsSuffixTagForModule() {
        val useCase = SetVersionGitTagUseCase()

        val repo = mockk<Repository>(relaxed = true)
        val tagCmd = mockk<TagCommand>(relaxed = true)
        val pushCmd = mockk<PushCommand>(relaxed = true)
        val git = mockk<Git>(relaxed = true)
        val statusCmd = mockk<org.eclipse.jgit.api.StatusCommand>(relaxed = true)
        val status = mockk<org.eclipse.jgit.api.Status>()

        every { git.tag() } returns tagCmd
        every { tagCmd.setName(any()) } returns tagCmd
        every { git.push() } returns pushCmd
        every { pushCmd.setCredentialsProvider(any()) } returns pushCmd
        every { pushCmd.setPushTags() } returns pushCmd
        every { git.status() } returns statusCmd
        every { statusCmd.call() } returns status
        every { status.added } returns emptySet()
        every { status.changed } returns emptySet()
        every { status.modified } returns emptySet()
        every { status.removed } returns emptySet()

        useCase.invoke(
            gitPath = "/tmp/repo",
            gitUser = "user",
            gitPat = "pat",
            version = Final(1, 2, 3),
            overrideBranch = null,
            subProjectTag = "api",
            repositoryFactory = { repo },
            gitFactory = { git }
        )

        verify { tagCmd.setName("v1.2.3-api") }
        verify { tagCmd.call() }
        verify { pushCmd.setPushTags() }
        verify { pushCmd.call() }
    }

    @Test
    @DisplayName("Creates global tag vX.Y.Z when no subProjectTag provided")
    fun createsGlobalTagWhenNoModule() {
        val useCase = SetVersionGitTagUseCase()

        val repo = mockk<Repository>(relaxed = true)
        val tagCmd = mockk<TagCommand>(relaxed = true)
        val pushCmd = mockk<PushCommand>(relaxed = true)
        val git = mockk<Git>(relaxed = true)
        val statusCmd = mockk<org.eclipse.jgit.api.StatusCommand>(relaxed = true)
        val status = mockk<org.eclipse.jgit.api.Status>()

        every { git.tag() } returns tagCmd
        every { tagCmd.setName(any()) } returns tagCmd
        every { git.push() } returns pushCmd
        every { pushCmd.setCredentialsProvider(any()) } returns pushCmd
        every { pushCmd.setPushTags() } returns pushCmd
        every { git.status() } returns statusCmd
        every { statusCmd.call() } returns status
        every { status.added } returns emptySet()
        every { status.changed } returns emptySet()
        every { status.modified } returns emptySet()
        every { status.removed } returns emptySet()

        useCase.invoke(
            gitPath = "/tmp/repo",
            gitUser = "user",
            gitPat = "pat",
            version = Final(0, 1, 0),
            overrideBranch = null,
            subProjectTag = null,
            repositoryFactory = { repo },
            gitFactory = { git }
        )

        verify { tagCmd.setName("v0.1.0") }
        verify { tagCmd.call() }
        verify { pushCmd.setPushTags() }
        verify { pushCmd.call() }
    }
}


