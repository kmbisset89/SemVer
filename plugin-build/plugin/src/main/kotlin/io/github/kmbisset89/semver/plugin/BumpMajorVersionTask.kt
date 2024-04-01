package io.github.kmbisset89.semver.plugin

import io.github.kmbisset89.semver.plugin.logic.BumpLevel


/**
 * A Gradle task designed to increase the major version of the project's current semantic version.
 * This task extends [AbstractBumpTask], with the bump level explicitly set to [BumpLevel.MAJOR].
 * Incrementing the major version is indicative of making breaking changes to the project's API or
 * introducing significant new features that are not backward-compatible with previous versions.
 *
 * Upon execution, this task identifies the current project version, calculates the next major version,
 * and updates the project version accordingly. It also handles the creation of a corresponding Git tag
 * to reflect the new version in the project's version control history, using the Git configuration provided
 * by the abstract properties in [AbstractBumpTask].
 *
 * The major version bump is essential for signaling to consumers of the project that significant changes
 * have been made, which may require modifications to their existing codebase to accommodate the new version.
 */
abstract class BumpMajorVersionTask : AbstractBumpTask(
    "Bumps the major version of the project.",
    BumpLevel.MAJOR
)
