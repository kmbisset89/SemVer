package io.github.kmbisset89.semver.plugin

import io.github.kmbisset89.semver.plugin.logic.DetermineCurrentVersion
import io.github.kmbisset89.semver.plugin.logic.GetOrCreateCurrentVersionUseCase
import io.github.kmbisset89.semver.plugin.logic.PropertyResolver
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
        val extension = project.extensions.create(EXTENSION_NAME, SemVerExtension::class.java, project)

        // Default to using a local.properties file if one is present
        extension.considerLocalPropertiesFile.convention(project.file("local.properties").exists())

        // Resolve any initial values from available properties
        val resolver = PropertyResolver(project, extension.considerLocalPropertiesFile.get())

        extension.gitDirectory.convention(resolver.getStringProp("gitDir", project.rootDir.absolutePath))
        extension.baseBranchName.convention(resolver.getStringProp("baseBranchName", "main"))
        extension.gitEmail.convention(resolver.getStringProp("gitEmail", ""))
        extension.gitPat.convention(resolver.getStringProp("gitPat", ""))
        extension.subProjectTag.convention(resolver.getStringProp("subProjectTag", ""))
        extension.betaBranchPrefixes.convention(
            resolver.getStringListProp(
                "betaBranchPrefixes",
                listOf("feature/", "feat/", "bugfix/", "fix/")
            ) ?: listOf("feature/", "feat/", "bugfix/", "fix/")
        )
        extension.betaIncrementStrategy.convention(resolver.getStringProp("betaIncrementStrategy", "TIMESTAMP"))

        return extension
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
            it.subProjectTag.set(extension.subProjectTag)
        }
    }

    private fun resolveVersion(project: Project, extension: SemVerExtension): String {
        return GetOrCreateCurrentVersionUseCase().invoke(
            DetermineCurrentVersion().determineCurrentVersion(
                extension.gitDirectory.orNull,
                extension.baseBranchName.orNull,
                UsernamePasswordCredentialsProvider(extension.gitEmail.orNull, extension.gitPat.orNull),
                extension.subProjectTag.orNull
            ),
            gitFilePath = extension.gitDirectory.orNull,
            baseBranchName = extension.baseBranchName.orNull,
            project = project,
            headCommit = project.findProperty("headCommit") as? String?,
            branchName = project.findProperty("branchName") as? String?,
            betaBranchPrefixes = extension.betaBranchPrefixes.orNull,
            betaIncrementStrategy = extension.betaIncrementStrategy.orNull,
            subProjectTag = extension.subProjectTag.orNull
        )
    }
}
