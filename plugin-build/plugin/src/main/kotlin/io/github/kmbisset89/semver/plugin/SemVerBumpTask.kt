package io.github.kmbisset89.semver.plugin

import io.github.kmbisset89.semver.plugin.logic.PropertyResolver
import org.gradle.api.DefaultTask
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option

abstract class SemVerBumpTask : DefaultTask() {
    init {
        description = "Bumps the version of the project according to the SemVer specification."

        // Don't forget to set the group here.
        group = BasePlugin.BUILD_GROUP
    }

    @get:Input
    @get:Option(
        option = "gitDirectory",
        description = "The directory of the git repository. Can be relative or absolute."
    )
    abstract val gitDirectory: Property<String>

    @get:Input
    @get:Option(option = "gitBranch", description = "The branch to be used for the versioning")
    abstract val gitBranch: Property<String>

    @get:Input
    @get:Option(option = "gitUser", description = "The user to be used for the git operations")
    abstract val gitUser: Property<String>

    @get:Input
    @get:Option(option = "gitPat", description = "The personal access token to be used for the git operations")
    abstract val gitPat: Property<String>

    @get:Input
    @get:Optional
    @get:Option(option = "considerLocalPropertiesFile", description = "Whether to consider the local properties file")
    abstract val considerLocalPropertiesFile: Property<Boolean>

    @TaskAction
    fun bumpVersions() {
        val propertyResolver = PropertyResolver(project, considerLocalPropertiesFile.orNull ?: false)

    }
}
