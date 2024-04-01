package io.github.kmbisset89.semver.plugin

import io.github.kmbisset89.semver.plugin.logic.CreateTimeStampVersion
import io.github.kmbisset89.semver.plugin.logic.DetermineCurrentVersion
import org.gradle.api.Plugin
import org.gradle.api.Project

const val EXTENSION_NAME = "simVerConfig"
const val BUMP_RELEASE_CANDIDATE_TASK_NAME = "bumpReleaseCandidate"
const val BUMP_PATCH_TASK_NAME = "bumpPatch"
const val BUMP_MINOR_TASK_NAME = "bumpMinor"
const val BUMP_MAJOR_TASK_NAME = "bumpMajor"

abstract class SemVerPlugin : Plugin<Project> {
    override fun apply(project: Project) {

        val extension = project.extensions.create(EXTENSION_NAME, SemVerExtension::class.java, project)

        project.version = CreateTimeStampVersion().invoke(
            DetermineCurrentVersion().determineCurrentVersion(
                extension.gitDirectory.get(),
                extension.baseBranchName.get()
            ),
            (project.property("isBeta") as? Boolean) ?: (false)
        )

        // Add a task that uses configuration from the extension object
        val releaseCandidateVersionTask = project.tasks.register(BUMP_RELEASE_CANDIDATE_TASK_NAME, BumpReleaseCandidateVersionTask::class.java) {

            }

        val patchVersionTask = project.tasks.register(BUMP_PATCH_TASK_NAME, BumpPatchVersionTask::class.java) {

            }

        val minorVersionTask = project.tasks.register(BUMP_MINOR_TASK_NAME, BumpMinorVersionTask::class.java) {

            }

        val majorVersionTask = project.tasks.register(BUMP_MAJOR_TASK_NAME, BumpMajorVersionTask::class.java) {

            }
        

    }
}
