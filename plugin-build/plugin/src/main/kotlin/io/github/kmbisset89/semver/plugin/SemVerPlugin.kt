package io.github.kmbisset89.semver.plugin

import io.github.kmbisset89.semver.plugin.logic.DetermineCurrentVersion
import io.github.kmbisset89.semver.plugin.logic.GetOrCreateCurrentVersionUseCase
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider
import org.gradle.api.Plugin
import org.gradle.api.Project

const val EXTENSION_NAME = "semVerConfig"
const val BUMP_TASK_NAME = "bumpVersion"

abstract class SemVerPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val extension = createExtension(project)
        configureVersioning(project, extension)
        registerTasks(project, extension)
    }

    private fun createExtension(project: Project): SemVerExtension {
        return project.extensions.create(EXTENSION_NAME, SemVerExtension::class.java, project)
    }

    private fun configureVersioning(project: Project, extension: SemVerExtension) {
        project.afterEvaluate {
            project.version = resolveVersion(project, extension)
        }
    }

    private fun registerTasks(project: Project, extension: SemVerExtension) {
        project.tasks.register(BUMP_TASK_NAME, BumpVersionTask::class.java) {
            it.gitDirectory.set(extension.gitDirectory)
            it.baseBranchName.set(extension.baseBranchName)
            it.gitEmail.set(extension.gitEmail)
            it.gitPat.set(extension.gitPat)
            it.considerLocalPropertiesFile.set(extension.considerLocalPropertiesFile)
        }
    }

    private fun resolveVersion(project: Project, extension: SemVerExtension): String {
        return GetOrCreateCurrentVersionUseCase().invoke(
            DetermineCurrentVersion().determineCurrentVersion(
                extension.gitDirectory.orNull,
                extension.baseBranchName.orNull,
                UsernamePasswordCredentialsProvider(extension.gitEmail.orNull, extension.gitPat.orNull),
            ),
            gitFilePath = extension.gitDirectory.orNull,
            baseBranchName = extension.baseBranchName.orNull,
            project = project,
            headCommit = project.findProperty("headCommit") as? String?,
            branchName = project.findProperty("branchName") as? String?
        )
    }
}
