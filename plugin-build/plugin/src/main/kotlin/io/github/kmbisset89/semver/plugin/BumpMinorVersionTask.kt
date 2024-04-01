package io.github.kmbisset89.semver.plugin

import io.github.kmbisset89.semver.plugin.logic.BumpLevel


/**
 * A Gradle task designed to increase the minor version of the project's current semantic version.
 * This task extends [AbstractBumpTask], with the bump level explicitly set to [BumpLevel.MINOR].
 * Incrementing the minor version is indicative of adding new functionality in a backward-compatible
 * manner or making substantial improvements and enhancements that do not alter the project's existing
 * API in a breaking way.
 *
 * Upon execution, this task identifies the current project version, calculates the next minor version,
 * and updates the project version accordingly. It also handles the creation of a corresponding Git tag
 * to reflect the new version in the project's version control history, using the Git configuration provided
 * by the abstract properties in [AbstractBumpTask].
 *
 * The minor version bump is crucial for maintaining a clear and meaningful version history, signaling
 * to consumers of the project that new features or significant changes have been introduced, while ensuring
 * compatibility with previous versions.
 */

abstract class BumpMinorVersionTask : AbstractBumpTask(
    "Bumps the minor version of the project.",
    BumpLevel.MINOR
)
