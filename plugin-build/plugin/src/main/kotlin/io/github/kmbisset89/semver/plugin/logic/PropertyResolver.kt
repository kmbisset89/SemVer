package io.github.kmbisset89.semver.plugin.logic

import org.gradle.api.Project
import java.util.Properties

/**
 * Resolves properties from various sources including project properties, local properties file, and environment variables.
 * This resolver allows access to properties required for project configuration, ensuring fallbacks and defaults where necessary.
 *
 * @property project The Gradle project from which project properties are retrieved.
 * @param considerLocalPropertiesFile Flag to consider or ignore the local.properties file for property resolution.
 * If `true`, the local.properties file will be loaded and considered during property resolution.
 * @constructor Creates a [PropertyResolver] instance capable of resolving properties from specified sources.
 */
class PropertyResolver(private val project: Project, considerLocalPropertiesFile: Boolean) : IPropertyResolver {
    private val localProperties : Properties? = if (considerLocalPropertiesFile) {
        val localProps = Properties()
        localProps.load(project.file("local.properties").inputStream())
        localProps
    } else {
        null
    }

    /**
     * Retrieves a string property by name, returning a default value if the property is not found.
     *
     * @param propertyName The name of the property to retrieve.
     * @param defaultValue The default value to return if the property is not found. Defaults to `null`.
     * @return The property value as a string or `defaultValue` if the property is not found.
     */
    override fun getStringProp(propertyName: String, defaultValue: String? ): String? {
        return getProp(propertyName, defaultValue) as String?
    }

    /**
     * Retrieves a required string property by name, throwing an exception if the property is not found.
     *
     * @param propertyName The name of the property to retrieve.
     * @return The property value as a string.
     * @throws IllegalStateException if the property is not found.
     */
    override fun getRequiredStringProp(propertyName: String): String {
        return (getProp(propertyName) ?: throw IllegalStateException("$propertyName is required")) as String
    }

    /**
     * Retrieves a required string property by name, returning a default value if the property is not found.
     *
     * @param propertyName The name of the property to retrieve.
     * @param defaultValue The default value to return if the property is not found.
     * @return The property value as a string or `defaultValue` if the property is not found.
     */
    override fun getRequiredStringProp(propertyName: String, defaultValue: String): String {
        return getProp(propertyName, defaultValue) as String
    }


    /**
     * Retrieves a required boolean property by name, returning a default value if the property is not found.
     *
     * @param propertyName The name of the property to retrieve.
     * @param defaultValue The default value to return if the property is not found.
     * @return The property value as a boolean or `defaultValue` if the property is not found.
     */
    override fun getRequiredBooleanProp(propertyName: String, defaultValue: Boolean): Boolean {
        return getProp(propertyName)?.toString()?.toBoolean() ?: defaultValue
    }

    /**
     * Retrieves a required integer property by name, throwing an exception if the property is not found.
     *
     * @param propertyName The name of the property to retrieve.
     * @return The property value as an integer.
     * @throws IllegalStateException if the property is not found.
     */
    override fun getRequiredIntegerProp(propertyName: String): Int {
        return getIntegerProp(propertyName, null) ?: throw IllegalStateException("$propertyName is required")
    }

    /**
     * Retrieves a required integer property by name, returning a default value if the property is not found.
     *
     * @param propertyName The name of the property to retrieve.
     * @param defaultValue The default value to return if the property is not found.
     * @return The property value as an integer or `defaultValue` if the property is not found.
     */
    override fun getRequiredIntegerProp(propertyName: String, defaultValue: Int): Int {
        return getIntegerProp(propertyName, defaultValue)!!
    }

    /**
     * Retrieves an integer property by name, returning a default value if the property is not found.
     *
     * @param propertyName The name of the property to retrieve.
     * @param defaultValue The default value to return if the property is not found. Defaults to `null`.
     * @return The property value as an integer or `defaultValue` if the property is not found.
     */
    override fun getIntegerProp(propertyName: String, defaultValue: Int?): Int? {
        val value = getProp(propertyName)
        return if (value == null) {
            defaultValue
        } else {
            Integer.parseInt(value.toString())
        }
    }

    /**
     * Retrieves a boolean property by name, returning a default value if the property is not found.
     *
     * @param propertyName The name of the property to retrieve.
     * @param defaultValue The default value to return if the property is not found. Defaults to `null`.
     * @return The property value as a boolean or `defaultValue` if the property is not found.
     */
    override fun getBooleanProp(propertyName: String, defaultValue: Boolean?): Boolean? {
        val value = getProp(propertyName)
        return if (value == null || value.toString().isEmpty()) {
            defaultValue
        } else {
            value.toString().toBoolean()
        }
    }

    /**
     * Retrieves a double property by name, returning a default value if the property is not found.
     *
     * @param propertyName The name of the property to retrieve.
     * @param defaultValue The default value to return if the property is not found. Defaults to `null`.
     * @return The property value as a double or `defaultValue` if the property is not found.
     */
    override fun getDoubleProp(propertyName: String, defaultValue: Double?): Double? {
        val value = getProp(propertyName)
        return if (value == null || value.toString().isEmpty()) {
            defaultValue
        } else {
            java.lang.Double.parseDouble(value.toString())
        }
    }

    /**
     * Retrieves a list of strings from a property by name, returning a default value if the property is not found.
     * The property value is expected to be a comma-separated list of strings.
     *
     * @param propertyName The name of the property to retrieve.
     * @param defaultValue The default list of strings to return if the property is not found. Defaults to `null`.
     * @return The property value as a list of strings or `defaultValue` if the property is not found.
     */
    @Suppress("UNCHECKED_CAST")
    override fun getStringListProp(propertyName: String, defaultValue: List<String>?): List<String>? {
        val value = getProp(propertyName)
        return if (value == null) {
            defaultValue
        } else {
            value as? List<String> ?: value.toString().split(",")
        }
    }

    /**
     * Retrieves a property value by its name, considering project properties, local properties, and environment variables in that order.
     * Returns a default value if the property is not found in any of the sources.
     *
     * @param propertyName The name of the property to retrieve.
     * @param defaultValue The default value to return if the property is not found. Defaults to `null`.
     * @return The property value as an [Any] or `defaultValue` if the property is not found.
     */
    private fun getProp(propertyName: String, defaultValue: Any? = null): Any? {
        return when {
            project.hasProperty(propertyName) -> project.property(propertyName)
            localProperties?.containsKey(propertyName) == true -> localProperties[propertyName]
            System.getenv().containsKey(propertyName) -> System.getenv(propertyName)
            else -> defaultValue
        }
    }
}
