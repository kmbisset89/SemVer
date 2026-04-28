package io.github.kmbisset89.semver.plugin

import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import javax.inject.Inject


@Suppress("UnnecessaryAbstractClass")
abstract class SemVerExtension constructor(project: Project) {
    @Inject
    private val objects = project.objects

    val gitDirectory: Property<String> = objects.property(String::class.java)
    val baseBranchName: Property<String> = objects.property(String::class.java)
    val gitEmail: Property<String> = objects.property(String::class.java)
    val gitPat: Property<String> = objects.property(String::class.java)
    val considerLocalPropertiesFile: Property<Boolean> = objects.property(Boolean::class.java)

    // Optional tag identifier to scope versioning to a single sub-project when running tasks explicitly for it.
    // For module-specific versioning via the DSL below, prefer configuring entries in subModules.
    val subProjectTag: Property<String> = objects.property(String::class.java)

    // Configurable list of branch name prefixes that should be treated as beta builds.
    // Defaults to common patterns: feature/, feat/, bugfix/, fix/
    val betaBranchPrefixes: ListProperty<String> = objects.listProperty(String::class.java)

    // Strategy for generating beta identifiers: "TIMESTAMP" (default) or "SEQUENTIAL"
    val betaIncrementStrategy: Property<String> = objects.property(String::class.java)

    /**
     * When non-blank, this exact string is used for [org.gradle.api.Project.getVersion] and [SemVerRootExtension.version]
     * instead of resolving from Git. If set from `gradle.properties`, `-PfixedVersion`, or `local.properties` (via
     * [considerLocalPropertiesFile]), the plugin applies it during application so other plugins see the version during
     * configuration. Values set only in the DSL are applied in `afterEvaluate` with the computed dynamic version.
     */
    val fixedVersion: Property<String> = objects.property(String::class.java)

    // Container of sub-module configurations. Each element's name is the module tag used in Git tags (suffix).
    val subModules: NamedDomainObjectContainer<SubModuleConfig> =
        project.container(SubModuleConfig::class.java)

    fun subModule(name: String, configureAction: Action<SubModuleConfig>) {
        subModules.maybeCreate(name).apply(configureAction::execute)
    }
}

abstract class SubModuleConfig @Inject constructor(val name: String, project: Project) {
    // Human-controlled tag identifier used at the end of tags: vX.Y.Z-<name>
    val tag: Property<String> = project.objects.property(String::class.java).convention(name)

    // Root file-system path of this module relative to repository root (or absolute)
    val filePath: Property<String> = project.objects.property(String::class.java)

    // One or more source directories used to determine whether the module changed
    val srcDirs: ListProperty<String> = project.objects.listProperty(String::class.java)

    // Computed flag indicating whether the module's version should change (exposed for onlyIf)
    val versionChanged: Property<Boolean> = project.objects.property(Boolean::class.java)

    fun filePath(path: String) {
        filePath.set(path)
    }

    fun srcDir(path: String) {
        srcDirs.add(path)
    }
}

/**
 * Root-level extension to expose resolved versions for the global project and for configured sub-modules.
 * Accessible as `rootProject.extensions.getByName("semver")` or via Kotlin accessors when named `semver`.
 */
open class SemVerRootExtension @Inject constructor(
    private val project: Project,
    private val config: SemVerExtension,
) {
    /** Returns the resolved global/root version string (e.g., vX.Y.Z or with qualifiers depending on branch). */
    fun version(): String = compute(null)

    /** Returns the resolved version string for a given sub-module tag (e.g., tag "api" -> vX.Y.Z-api baseline). */
    fun moduleVersion(tag: String): String = compute(tag)

    /** Returns whether the module identified by [tag] was detected as changed. */
    fun moduleVersionChanged(tag: String): Boolean {
        val ep = project.extensions.extraProperties
        val key = "semver.module.$tag.versionChanged"
        return (if (ep.has(key)) ep.get(key) else null) as? Boolean ?: false
    }

    private fun compute(tag: String?): String {
        return resolveSemVerVersionForProject(config, project, tag)
    }
}
