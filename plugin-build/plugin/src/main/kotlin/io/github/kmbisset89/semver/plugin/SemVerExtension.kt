package io.github.kmbisset89.semver.plugin

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

    // Optional prefix to scope tags to a specific sub-project/module (e.g., "api").
    // When set, only tags starting with "<subProjectTag>-" will be considered and new tags will include this prefix.
    val subProjectTag: Property<String> = objects.property(String::class.java)

    // Configurable list of branch name prefixes that should be treated as beta builds.
    // Defaults to common patterns: feature/, feat/, bugfix/, fix/
    val betaBranchPrefixes: ListProperty<String> = objects.listProperty(String::class.java)

    // Strategy for generating beta identifiers: "TIMESTAMP" (default) or "SEQUENTIAL"
    val betaIncrementStrategy: Property<String> = objects.property(String::class.java)
}
