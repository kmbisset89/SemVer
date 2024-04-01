package io.github.kmbisset89.semver.plugin

import io.github.kmbisset89.semver.plugin.logic.BumpLevel



abstract class BumpMinorVersionTask : AbstractBumpTask(
    "Bumps the minor version of the project.",
    BumpLevel.MINOR
)
