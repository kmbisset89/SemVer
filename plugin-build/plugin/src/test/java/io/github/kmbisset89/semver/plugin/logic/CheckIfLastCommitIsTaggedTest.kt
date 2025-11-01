package io.github.kmbisset89.semver.plugin.logic

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.platform.suite.api.Suite

@Suite

class CheckIfLastCommitIsTaggedTest {

    @Nested
    @DisplayName("When CheckIfLastCommitIsTaggedTest is called from a mocked git repository")
    inner class CheckIfLastCommitIsTaggedMocked {

        @Test
        @DisplayName("Then it returns the default version number")
        fun `determineCurrentVersion returns the default version number`() {
            val check = CheckIfLastCommitIsTagged()

        }
    }
}
