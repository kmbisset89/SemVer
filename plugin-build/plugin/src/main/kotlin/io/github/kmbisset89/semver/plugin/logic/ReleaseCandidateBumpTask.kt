package io.github.kmbisset89.semver.plugin.logic

import io.github.kmbisset89.semver.plugin.AbstractBumpTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.options.Option

abstract class ReleaseCandidateBumpTask :
    AbstractBumpTask("Bumps the version to the next release candidate", BumpLevel.RELEASE_CANDIDATE) {
}
