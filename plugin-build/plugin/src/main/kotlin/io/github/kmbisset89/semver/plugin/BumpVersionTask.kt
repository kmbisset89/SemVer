package io.github.kmbisset89.semver.plugin

import io.github.kmbisset89.semver.plugin.logic.BumpLevel
import io.github.kmbisset89.semver.plugin.logic.DetermineCurrentVersion
import io.github.kmbisset89.semver.plugin.logic.DetermineNextVersionUseCase
import io.github.kmbisset89.semver.plugin.logic.PropertyResolver
import io.github.kmbisset89.semver.plugin.logic.SetVersionGitTagUseCase
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider
import org.gradle.api.DefaultTask
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option

/**
 * An abstract Gradle task for bumping project version according to semantic versioning,
 * based on specified bump level and git repository details. This task updates the project version
 * and tags the git repository accordingly.
 *
 * @property gitDirectory The directory of the Git repository. This path can be relative to the project
 * directory or an absolute path.
 * @property gitBranch The branch on which versioning operations are performed. Useful for projects
 * that use branch-specific versioning strategies.
 * @property gitUser The username to use for Git operations that require authentication.
 * @property gitPat The personal access token (PAT) for Git operations, providing an alternative
 * to password authentication.
 * @property considerLocalPropertiesFile Flag indicating whether to consider local properties file for
 * overriding certain task inputs, such as gitBranch.
 */
abstract class BumpVersionTask() : DefaultTask() {

    init {
        // Assign task description and categorize this task under Gradle's BUILD_GROUP.
        this@BumpVersionTask.description = "Bumps the project version according to semantic versioning."
        group = BasePlugin.BUILD_GROUP
    }

    // Define abstract properties that subclasses must provide, used for configuring the task.

    @get:Input
    @get:Option(
        option = "gitDirectory",
        description = "The directory of the git repository. Can be relative or absolute."
    )
    abstract val gitDirectory: Property<String>

    @get:Input
    @get:Option(option = "baseBranchName", description = "The branch to be used for the versioning")
    abstract val baseBranchName: Property<String>

    @get:Input
    @get:Option(option = "gitEmail", description = "The user to be used for the git operations")
    abstract val gitEmail: Property<String>

    @get:Input
    @get:Option(option = "gitPat", description = "The personal access token to be used for the git operations")
    abstract val gitPat: Property<String>

    @get:Input
    @get:Optional
    @get:Option(option = "considerLocalPropertiesFile", description = "Whether to consider the local properties file")
    abstract val considerLocalPropertiesFile: Property<Boolean>

    @get:Input
    @get:Optional
    @get:Option(option = "subProjectTag", description = "Optional sub-project/module tag prefix to scope versioning (e.g., 'api')")
    abstract val subProjectTag: Property<String>

    /**
     * Executes the version bumping task. Determines the current version, calculates the next version
     * based on the specified bump level, updates the project version, and tags the Git repository.
     */
    @TaskAction
    fun executeTask() {
        // Resolve properties, potentially considering local overrides.
        val propertyResolver = PropertyResolver(project, considerLocalPropertiesFile.orNull ?: false)

        // Determine the current project version based on Git tags.
        val currentVersion = DetermineCurrentVersion().determineCurrentVersion(
            gitDirectory.get(),
            propertyResolver.getStringProp("overrideBranch") ?: baseBranchName.get(),
            UsernamePasswordCredentialsProvider(gitEmail.get(), gitPat.get()),
            subProjectTag.orNull
            )

        val bumpLevel = propertyResolver.getStringProp("bumpLevel")?.let {
            BumpLevel.getLevel(it)
        } ?: BumpLevel.RELEASE_CANDIDATE

        println("Bumping version by $bumpLevel")

        // Calculate the next version based on the current version and the bump level.
        val newVersion = DetermineNextVersionUseCase().invoke(
            currentVersion,
            bumpLevel,
            propertyResolver.getRequiredBooleanProp("isFinal", false)
        )

        // Tag the Git repository with the new version, and apply necessary Git user and PAT for operations.
        SetVersionGitTagUseCase().invoke(
            gitDirectory.get(),
            gitEmail.get(),
            gitPat.get(),
            newVersion,
            propertyResolver.getStringProp("overrideBranch"),
            subProjectTag.orNull
        )

        // Update the Gradle project version to the new semantic version.
        project.version = newVersion.toString()
    }
}
