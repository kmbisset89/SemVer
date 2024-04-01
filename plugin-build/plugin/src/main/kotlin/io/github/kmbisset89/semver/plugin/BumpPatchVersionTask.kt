package io.github.kmbisset89.semver.plugin

import io.github.kmbisset89.semver.plugin.logic.BumpLevel


/**
 * A Gradle task for incrementing the patch version of the project's current version.
 * Inherits from [AbstractBumpTask], with the bump level set to [BumpLevel.PATCH], indicating
 * that the task will increment the patch number according to semantic versioning principles.
 *
 * This task automates the process of updating the project's version to the next patch level,
 * which is typically done to release new bug fixes or minor improvements without affecting
 * the existing functionality or API compatibility.
 *
 * The new version is then applied to the project and tagged in the Git repository, adhering
 * to the configured Git settings provided by the abstract properties in [AbstractBumpTask].
 */
abstract class BumpPatchVersionTask : AbstractBumpTask(
    "Bumps the patch version of the project.",
    BumpLevel.PATCH
)
