package io.github.kmbisset89.semver.plugin.logic

/**
 * Enumerates the different levels at which a software version number can be incremented.
 *
 * @property MAJOR Represents an increment of the major version number, indicating incompatible API changes.
 * @property MINOR Represents an increment of the minor version number, indicating addition of functionality in a backward-compatible manner.
 * @property PATCH Represents an increment of the patch version number, indicating backward-compatible bug fixes.
 * @property RELEASE_CANDIDATE Represents an increment of a version number for a release candidate, indicating a version that is potentially final unless significant bugs emerge.
 */
enum class BumpLevel {
    MAJOR,
    MINOR,
    PATCH,
    RELEASE_CANDIDATE
}
