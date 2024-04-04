package io.github.kmbisset89.semver.plugin

import org.gradle.api.Project
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
}
