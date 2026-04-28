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
        // Create a typed helper extension on every project for type-safe accessors in Kotlin DSL
        project.extensions.create("semver", SemVerRootExtension::class.java, project, extension)
        applyHardVersionIfPresent(project, extension)
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

        resolver.getStringProp("fixedVersion", null)
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?.let { extension.fixedVersion.set(it) }

        return extension
    }

    private fun configureVersioning(project: Project, extension: SemVerExtension) {
        project.afterEvaluate {
            project.version = resolveSemVerVersionForProject(extension, project, extension.subProjectTag.orNull)

            // Compute versionChanged flags for configured sub-modules
            if (!extension.subModules.isEmpty()) {
                val gitDir = extension.gitDirectory.orNull ?: project.rootDir.absolutePath
                extension.subModules.forEach { module ->
                    val tag = module.tag.orNull ?: module.name
                    val paths = buildList {
                        module.filePath.orNull?.let { add(it) }
                        addAll(module.srcDirs.orNull ?: emptyList())
                    }.ifEmpty { listOfNotNull(module.filePath.orNull) }

                    val changed = try {
                        io.github.kmbisset89.semver.plugin.logic.ChangeDetectionUseCase().invoke(
                            gitDir,
                            tag,
                            paths
                        )
                    } catch (e: Exception) {
                        project.logger.warn("semver: change detection failed for module '${module.name}': ${e.message}")
                        false
                    }

                    module.versionChanged.set(changed)

                    // Expose as extra properties for convenient onlyIf usage
                    project.extensions.extraProperties.set("semver.module.$tag.versionChanged", changed)
                }

                // If a subProjectTag is set for this project, also expose a generic flag
                extension.subProjectTag.orNull?.let { tag ->
                    val changed = extension.subModules.findByName(tag)?.versionChanged?.orNull ?: false
                    project.extensions.extraProperties.set("semver.versionChanged", changed)
                }
            }
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
            it.fixedVersion.set(extension.fixedVersion)
        }
    }

    /**
     * When [SemVerExtension.fixedVersion] is already populated (for example from `gradle.properties` or `-PfixedVersion`),
     * set [Project.getVersion] immediately so other plugins that read the version during configuration see the final value.
     */
    private fun applyHardVersionIfPresent(project: Project, extension: SemVerExtension) {
        extension.fixedVersion.orNull?.trim()?.takeIf { it.isNotEmpty() }?.let { project.version = it }
    }
}

internal fun resolveSemVerVersionForProject(
    extension: SemVerExtension,
    project: Project,
    subProjectTag: String?,
): String {
    extension.fixedVersion.orNull?.trim()?.takeIf { it.isNotEmpty() }?.let { return it }
    return GetOrCreateCurrentVersionUseCase().invoke(
        DetermineCurrentVersion().determineCurrentVersion(
            extension.gitDirectory.orNull,
            extension.baseBranchName.orNull,
            UsernamePasswordCredentialsProvider(extension.gitEmail.orNull, extension.gitPat.orNull),
            subProjectTag
        ),
        gitFilePath = extension.gitDirectory.orNull,
        baseBranchName = extension.baseBranchName.orNull,
        project = project,
        headCommit = project.findProperty("headCommit") as? String?,
        branchName = project.findProperty("branchName") as? String?,
        betaBranchPrefixes = extension.betaBranchPrefixes.orNull,
        betaIncrementStrategy = extension.betaIncrementStrategy.orNull,
        subProjectTag = subProjectTag
    )
}
