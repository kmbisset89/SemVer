package io.github.kmbisset89.semver.plugin

import io.github.kmbisset89.semver.plugin.logic.BumpLevel

/**
 * A task that increments the project's version to the next release candidate.
 * It derives from [AbstractBumpTask], specifying the bump level as [BumpLevel.RELEASE_CANDIDATE].
 * This task is responsible for identifying the current project version, calculating the next
 * release candidate version, and applying this new version both as a tag in the project's Git repository
 * and as the project's new version.
 *
 * Usage of this task within a build script allows for automated incrementing of release candidate versions,
 * facilitating a continuous delivery workflow where pre-release versions are generated and tested
 * before final release.
 */
abstract class BumpReleaseCandidateVersionTask : AbstractBumpTask(
    "Bumps the version to the next release candidate version",
    BumpLevel.RELEASE_CANDIDATE
)
