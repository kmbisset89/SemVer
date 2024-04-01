package io.github.kmbisset89.semver.plugin.logic

/**
 * Represents versions of a software product in accordance with Semantic Versioning (SemVer),
 * including both final releases and release candidates.
 *
 * The version number follows the pattern MAJOR.MINOR.PATCH, where:
 * - MAJOR version changes introduce incompatible API changes,
 * - MINOR version changes add functionality in a backward-compatible manner,
 * - PATCH version changes make backward-compatible bug fixes.
 *
 * This sealed class also supports versioning for release candidates, indicated by a "-rc.X" suffix,
 * where X is the release candidate number.
 */
sealed class SemVer {
    abstract val major: Int
    abstract val minor: Int
    abstract val patch: Int

    /**
     * Represents a final release version, strictly adhering to the MAJOR.MINOR.PATCH pattern.
     */
    data class Final(
        override val major: Int,
        override val minor: Int,
        override val patch: Int,
    ) : SemVer() {
        override fun toString(): String = "$major.$minor.$patch"
    }

    /**
     * Represents a release candidate version, appending the "-rc.X" suffix to the standard version format,
     * where X is the release candidate number. Release candidates precede final releases and are used for final testing.
     */
    data class ReleaseCandidate(
        override val major: Int,
        override val minor: Int,
        override val patch: Int,
        val releaseCandidateNumber: Int,
    ) : SemVer() {
        override fun toString(): String = "$major.$minor.$patch-rc.$releaseCandidateNumber"
    }

    data object Default : SemVer() {
        override val major: Int = 0
        override val minor: Int = 0
        override val patch: Int = 0
    }
}

