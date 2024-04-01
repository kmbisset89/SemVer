package io.github.kmbisset89.semver.plugin.logic

/**
 * Defines an interface for resolving property values from various sources.
 * This interface abstracts the process of fetching configuration properties,
 * allowing for easy access to different types of properties with support for default values.
 *
 * Functions are provided to retrieve properties as various data types including String, Boolean, Int, Double,
 * and lists of Strings. Each function includes versions that require the property to exist (`getRequired*`) and
 * ones that allow for a default value if the property is not found.
 */
interface IPropertyResolver {

    /**
     * Retrieves a property value as a String, with an optional default value.
     *
     * @param propertyName The name of the property to retrieve.
     * @param defaultValue An optional default value to return if the property is not found.
     * @return The String value of the property, or the default value if not found.
     */
    fun getStringProp(propertyName: String, defaultValue: String? = null): String?

    /**
     * Retrieves a required property value as a String.
     *
     * @param propertyName The name of the property to retrieve.
     * @return The String value of the property.
     * @throws IllegalStateException if the property is not found.
     */
    fun getRequiredStringProp(propertyName: String): String

    /**
     * Retrieves a required property value as a String, with a default value if not found.
     *
     * @param propertyName The name of the property to retrieve.
     * @param defaultValue The default value to return if the property is not found.
     * @return The String value of the property or the default value if not found.
     */
    fun getRequiredStringProp(propertyName: String, defaultValue: String): String

    /**
     * Retrieves a required property value as a Boolean, with a default value if not found.
     *
     * @param propertyName The name of the property to retrieve.
     * @param defaultValue The default value to use if the property is not found.
     * @return The Boolean value of the property or the default value if not found.
     */
    fun getRequiredBooleanProp(propertyName: String, defaultValue: Boolean): Boolean

    /**
     * Retrieves a required property value as an Int.
     *
     * @param propertyName The name of the property to retrieve.
     * @return The Int value of the property.
     * @throws IllegalStateException if the property is not found.
     */
    fun getRequiredIntegerProp(propertyName: String): Int

    /**
     * Retrieves a required property value as an Int, with a default value if not found.
     *
     * @param propertyName The name of the property to retrieve.
     * @param defaultValue The default value to use if the property is not found.
     * @return The Int value of the property or the default value if not found.
     */
    fun getRequiredIntegerProp(propertyName: String, defaultValue: Int): Int

    /**
     * Retrieves a property value as an Int, with an optional default value.
     *
     * @param propertyName The name of the property to retrieve.
     * @param defaultValue An optional default value to use if the property is not found.
     * @return The Int value of the property or the default value if not found.
     */
    fun getIntegerProp(propertyName: String, defaultValue: Int? = null): Int?

    /**
     * Retrieves a property value as a Double, with an optional default value.
     *
     * @param propertyName The name of the property to retrieve.
     * @param defaultValue An optional default value to use if the property is not found.
     * @return The Double value of the property or the default value if not found.
     */
    fun getDoubleProp(propertyName: String, defaultValue: Double? = null): Double?

    /**
     * Retrieves a property value as a Boolean, with an optional default value.
     *
     * @param propertyName The name of the property to retrieve.
     * @param defaultValue An optional default value to use if the property is not found.
     * @return The Boolean value of the property or the default value if not found.
     */
    fun getBooleanProp(propertyName: String, defaultValue: Boolean? = null): Boolean?

    /**
     * Retrieves a property value as a list of Strings, with an optional default value.
     *
     * @param propertyName The name of the property to retrieve.
     * @param defaultValue An optional default list of strings to use if the property is not found.
     * @return A list of String values of the property or the default value if not found.
     */
    fun getStringListProp(propertyName: String, defaultValue: List<String>? = null): List<String>?
}
