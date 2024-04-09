package io.github.kmbisset89.semver.plugin.logic

import org.junit.jupiter.api.Assertions.*
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

           assertTrue(check.invoke("C:\\dev\\git\\vmf-evaluation-tool\\vet", "6fb23fd544bfad6f482a86703a884c0f3110a21a"))

        }
    }
}
