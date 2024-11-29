package io.github.kmbisset89.semver.plugin.logic

/**
 * Determines the next semantic version based on the current version and the specified bump level.
 * It supports incrementing major, minor, and patch versions, as well as managing release candidate versions.
 * This use case ensures the semantic versioning rules are adhered to while facilitating version control
 * in software development.
 */
class DetermineNextVersionUseCase {

    /**
     * Invokes the use case to calculate the next version.
     *
     * @param currentVersion The current [SemVer] version from which to calculate the next version.
     * @param bumpLevel The [BumpLevel] indicating how to increment the current version.
     * @param isFinal Flag indicating whether the current version is final or a release candidate.
     * @return The next [SemVer] version after applying the bump level to the current version.
     */
    operator fun invoke(currentVersion: SemVer, bumpLevel: BumpLevel, isFinal: Boolean): SemVer = when (bumpLevel) {
        BumpLevel.RELEASE_CANDIDATE -> currentVersion.toReleaseCandidate(isFinal)
        BumpLevel.PATCH -> currentVersion.toPatch(isFinal)
        BumpLevel.MINOR -> currentVersion.toMinor(isFinal)
        BumpLevel.MAJOR -> currentVersion.toMajor(isFinal)
    }.also {
        println("Bumped version from $currentVersion to $it and isFinal: $isFinal")
    }

    /**
     * Converts the current version to a release candidate version. If the current version is already a
     * release candidate, the release candidate number is incremented. Otherwise, the patch version is
     * incremented and a new release candidate number is set to 1.
     *
     * @return A new [SemVer] instance representing the next release candidate version.
     */
    private fun SemVer.toReleaseCandidate(isFinal: Boolean): SemVer = when (this) {
        is SemVer.ReleaseCandidate -> if (isFinal) {
            SemVer.Final(major, minor, patch)
        } else {
            SemVer.ReleaseCandidate(major, minor, patch, releaseCandidateNumber + 1)
        }

        is SemVer.Final -> SemVer.ReleaseCandidate(major, minor, patch + 1, 1)
        SemVer.Default -> SemVer.ReleaseCandidate(0, 0, 0, 1)
    }

    /**
     * Increments the patch version of the current version. If the current version is a release candidate
     * and not final, it creates a new release candidate with the patch version incremented.
     *
     * @param isFinal Indicates if the bump should result in a final version or a new release candidate.
     * @return A new [SemVer] instance with the patch version incremented.
     */
    private fun SemVer.toPatch(isFinal: Boolean): SemVer = when {
        this is SemVer.ReleaseCandidate && !isFinal -> SemVer.ReleaseCandidate(major, minor, patch + 1, 1)
        else -> SemVer.Final(major, minor, patch + 1)
    }

    /**
     * Increments the minor version of the current version. If the current version is a release candidate
     * and not final, it creates a new release candidate with the minor version incremented.
     *
     * @param isFinal Indicates if the bump should result in a final version or a new release candidate.
     * @return A new [SemVer] instance with the minor version incremented.
     */
    private fun SemVer.toMinor(isFinal: Boolean): SemVer = when {
        this is SemVer.ReleaseCandidate && !isFinal -> SemVer.ReleaseCandidate(major, minor + 1, 0, 1)
        else -> SemVer.Final(major, minor + 1, 0)
    }


    /**
     * Increments the major version of the current version. If the current version is a release candidate
     * and not final, it creates a new release candidate with the major version incremented.
     *
     * @param isFinal Indicates if the bump should result in a final version or a new release candidate.
     * @return A new [SemVer] instance with the major version incremented.
     */
    private fun SemVer.toMajor(isFinal: Boolean): SemVer = when {
        this is SemVer.ReleaseCandidate && !isFinal -> SemVer.ReleaseCandidate(major + 1, 0, 0, 1)
        else -> SemVer.Final(major + 1, 0, 0)
    }
}
