package io.github.kmbisset89.semver.plugin

import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class FixedVersionPluginTest {

    @Test
    fun `fixedVersion from project property is applied during plugin application`() {
        val project = ProjectBuilder.builder().build()
        project.extensions.extraProperties.set("fixedVersion", "2.0.0-custom")
        project.pluginManager.apply("io.github.kmbisset89.semver.plugin")

        assertEquals("2.0.0-custom", project.version.toString())

        val semver = project.extensions.getByName("semver") as SemVerRootExtension
        assertEquals("2.0.0-custom", semver.version())
    }

    @Test
    fun `resolveSemVerVersionForProject returns fixed when extension has fixedVersion`() {
        val project = ProjectBuilder.builder().build()
        val ext = project.extensions.create(EXTENSION_NAME, SemVerExtension::class.java, project)
        ext.fixedVersion.set("9.8.7")

        assertEquals("9.8.7", resolveSemVerVersionForProject(ext, project, null))
    }
}
