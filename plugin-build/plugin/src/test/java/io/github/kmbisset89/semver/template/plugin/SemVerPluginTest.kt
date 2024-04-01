package io.github.kmbisset89.semver.template.plugin

import io.github.kmbisset89.semver.plugin.SemVerBumpTask
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class SemVerPluginTest {
    @Test
    fun `plugin is applied correctly to the project`() {
        val project = ProjectBuilder.builder().build()
        project.pluginManager.apply("com.ncorti.kotlin.gradle.template.plugin")

        assert(project.tasks.getByName("templateExample") is SemVerBumpTask)
    }

    @Test
    fun `extension templateExampleConfig is created correctly`() {
        val project = ProjectBuilder.builder().build()
        project.pluginManager.apply("com.ncorti.kotlin.gradle.template.plugin")

        assertNotNull(project.extensions.getByName("templateExampleConfig"))
    }

    @Test
    fun `parameters are passed correctly from extension to task`() {
        val project = ProjectBuilder.builder().build()
        project.pluginManager.apply("com.ncorti.kotlin.gradle.template.plugin")

    }
}
