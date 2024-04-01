package io.github.kmbisset89.semver.plugin

import io.github.kmbisset89.semver.plugin.logic.CreateTimeStampVersion
import io.github.kmbisset89.semver.plugin.logic.DetermineCurrentVersion
import org.gradle.api.Plugin
import org.gradle.api.Project

const val EXTENSION_NAME = "simVerConfig"
const val TASK_NAME = "templateExample"

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
        val task = project.tasks.register(TASK_NAME, SemVerBumpTask::class.java) {

        }
    }
}
