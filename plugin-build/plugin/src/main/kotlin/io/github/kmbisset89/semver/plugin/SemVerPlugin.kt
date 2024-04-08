package io.github.kmbisset89.semver.plugin

import io.github.kmbisset89.semver.plugin.logic.DetermineCurrentVersion
import io.github.kmbisset89.semver.plugin.logic.GetOrCreateCurrentVersionUseCase
import org.gradle.api.Plugin
import org.gradle.api.Project

const val EXTENSION_NAME = "semVerConfig"
const val BUMP_TASK_NAME = "bumpVersion"

abstract class SemVerPlugin : Plugin<Project> {
    override fun apply(project: Project) {

        val extension = project.extensions.create(EXTENSION_NAME, SemVerExtension::class.java, project)

        project.afterEvaluate {
            project.version = GetOrCreateCurrentVersionUseCase().invoke(
                DetermineCurrentVersion().determineCurrentVersion(
                    extension.gitDirectory.orNull,
                    extension.baseBranchName.orNull
                ),
                gitFilePath =  extension.gitDirectory.orNull,
                baseBranchName = extension.baseBranchName.orNull
            )
        }

        val releaseCandidateVersionTask =
            project.tasks.register(BUMP_TASK_NAME, BumpVersionTask::class.java) {
                it.gitDirectory.set(extension.gitDirectory)
                it.baseBranchName.set(extension.baseBranchName)
                it.gitEmail.set(extension.gitEmail)
                it.gitPat.set(extension.gitPat)
                it.considerLocalPropertiesFile.set(extension.considerLocalPropertiesFile)
            }
    }
}
