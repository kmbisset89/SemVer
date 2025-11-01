package io.github.kmbisset89.semver.plugin

import io.github.kmbisset89.semver.plugin.logic.BumpLevel
import io.github.kmbisset89.semver.plugin.logic.DetermineNextVersionUseCase
import io.github.kmbisset89.semver.plugin.logic.SemVer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.platform.suite.api.Suite

@Suite
class DetermineNextVersionTest {

    @Nested
    @DisplayName("Given a DetermineNextVersion instance")
    inner class DetermineNextVersionInstance {
        val determineNextVersionUseCase = DetermineNextVersionUseCase()

        @Nested
        @DisplayName("When determine next version is invoked is called ")
        inner class DetermineNextVersionCalledMocked {

            @Test
            @DisplayName("Then make sure it increments the rc version")
            fun testRcBump() {
                val currentVersion = SemVer.ReleaseCandidate(1, 2, 3, 4)
                val bumpLevel = BumpLevel.RELEASE_CANDIDATE
                val isFinal = false
                val result = determineNextVersionUseCase.invoke(currentVersion, bumpLevel, isFinal)
                assertEquals(SemVer.ReleaseCandidate(1, 2, 3, 5), result)
            }


            @Test
            @DisplayName("Then make sure it finalizes the rc version")

            fun testFinalizeRc() {
                val currentVersion = SemVer.ReleaseCandidate(1, 0, 0, 1)
                val bumpLevel = BumpLevel.RELEASE_CANDIDATE
                val isFinal = true
                val result = determineNextVersionUseCase.invoke(currentVersion, bumpLevel, isFinal)
                assertEquals(SemVer.Final(1, 0, 0), result)
            }

            @Test
            @DisplayName("Then make sure it increments the patch version")
            fun testPatchBump() {
                val currentVersion = SemVer.Final(1, 2, 3)
                val bumpLevel = BumpLevel.PATCH
                val isFinal = true // `isFinal` might not be necessary for patch, considering it always finalizes
                val result = determineNextVersionUseCase.invoke(currentVersion, bumpLevel, isFinal)
                assertEquals(SemVer.Final(1, 2, 4), result)
            }

            @Test
            @DisplayName("Then make sure it increments the minor version")
            fun testMinorBump() {
                val currentVersion = SemVer.Final(1, 2, 3)
                val bumpLevel = BumpLevel.MINOR
                val isFinal = true
                val result = determineNextVersionUseCase.invoke(currentVersion, bumpLevel, isFinal)
                assertEquals(SemVer.Final(1, 3, 0), result)
            }

            @Test
            @DisplayName("Then make sure it increments the major version")
            fun testMajorBump() {
                val currentVersion = SemVer.Final(1, 2, 3)
                val bumpLevel = BumpLevel.MAJOR
                val isFinal = true
                val result = determineNextVersionUseCase.invoke(currentVersion, bumpLevel, isFinal)
                assertEquals(SemVer.Final(2, 0, 0), result)
            }

            @Test
            @DisplayName("Then make sure non-final rc increment does not finalize")
            fun testNonFinalRcIncrement() {
                val currentVersion = SemVer.ReleaseCandidate(1, 2, 3, 1)
                val bumpLevel = BumpLevel.PATCH
                val isFinal = false
                val result = determineNextVersionUseCase.invoke(currentVersion, bumpLevel, isFinal)
                assertEquals(SemVer.ReleaseCandidate(1, 2, 4, 1), result)
            }

            @Test
            @DisplayName("Then handle Default version correctly")
            fun testDefaultVersion() {
                val currentVersion = SemVer.Default
                val bumpLevel = BumpLevel.PATCH
                val isFinal = true
                val result = determineNextVersionUseCase.invoke(currentVersion, bumpLevel, isFinal)
                assertEquals(SemVer.Final(0, 0, 1), result)
            }
        }
    }
}
