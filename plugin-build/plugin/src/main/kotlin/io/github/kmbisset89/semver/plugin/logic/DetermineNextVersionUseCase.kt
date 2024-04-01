package io.github.kmbisset89.semver.plugin.logic

/**
 * Determines the next semantic version based on the current version and the specified bump level.
 * It supports incrementing major, minor, and patch versions, as well as managing release candidate versions.
 */
class DetermineNextVersionUseCase {

    /**
     * Invokes the use case to calculate the next version.
     *
     * @param currentVersion The current [SemVer] version from which to calculate the next version.
     * @param bumpLevel The [BumpLevel] indicating how to increment the current version.
     * @return The next [SemVer] version after applying the bump level to the current version.
     */
    operator fun invoke(currentVersion: SemVer, bumpLevel: BumpLevel): SemVer = when (bumpLevel) {
        BumpLevel.RELEASE_CANDIDATE -> currentVersion.toReleaseCandidate()
        BumpLevel.PATCH -> currentVersion.toPatch()
        BumpLevel.MINOR -> currentVersion.toMinor()
        BumpLevel.MAJOR -> currentVersion.toMajor()
    }

    /**
     * Converts a [SemVer] version to its next release candidate version.
     * If the current version is already a release candidate, increments the release candidate number.
     * If the current version is final or default, starts a new release candidate sequence.
     *
     * @receiver The [SemVer] version to convert.
     * @return The next release candidate [SemVer] version.
     */
    private fun SemVer.toReleaseCandidate(): SemVer = when (this) {
        is SemVer.ReleaseCandidate -> SemVer.ReleaseCandidate(major, minor, patch, releaseCandidateNumber + 1)
        is SemVer.Final -> SemVer.ReleaseCandidate(major, minor, patch, 1)
        SemVer.Default -> SemVer.ReleaseCandidate(0, 0, 0, 1)
    }

    /**
     * Increments the patch version of a [SemVer] version.
     * If the current version is a release candidate, finalizes the release candidate without incrementing.
     *
     * @receiver The [SemVer] version to convert.
     * @return The [SemVer] version with the patch number incremented, or finalized from a release candidate.
     */
    private fun SemVer.toPatch(): SemVer = when (this) {
        is SemVer.Final -> SemVer.Final(major, minor, patch + 1)
        is SemVer.ReleaseCandidate -> SemVer.Final(major, minor, patch)
        SemVer.Default -> SemVer.Final(0, 0, 1)
    }

    /**
     * Increments the minor version of a [SemVer] version, resetting the patch number to 0.
     * If the current version is a release candidate, finalizes the release candidate as a minor version.
     *
     * @receiver The [SemVer] version to convert.
     * @return The [SemVer] version with the minor number incremented and patch number reset to 0.
     */
    private fun SemVer.toMinor(): SemVer = when (this) {
        is SemVer.Final -> SemVer.Final(major, minor + 1, 0)
        is SemVer.ReleaseCandidate -> SemVer.Final(major, minor, 0)
        SemVer.Default -> SemVer.Final(0, 1, 0)
    }

    /**
     * Increments the major version of a [SemVer] version, resetting both minor and patch numbers to 0.
     * If the current version is a release candidate, finalizes the release candidate as a major version.
     *
     * @receiver The [SemVer] version to convert.
     * @return The [SemVer] version with the major number incremented and both minor and patch numbers reset to 0.
     */
    private fun SemVer.toMajor(): SemVer = when (this) {
        is SemVer.Final -> SemVer.Final(major + 1, 0, 0)
        is SemVer.ReleaseCandidate -> SemVer.Final(major, 0, 0)
        SemVer.Default -> SemVer.Final(1, 0, 0)
    }
}
