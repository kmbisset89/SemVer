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

    fun filePath(path: String) { filePath.set(path) }
    fun srcDir(path: String) { srcDirs.add(path) }
}
