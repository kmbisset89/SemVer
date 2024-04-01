package io.github.kmbisset89.semver.plugin.logic

interface IPropertyResolver {

    fun getStringProp(propertyName: String, defaultValue: String? = null): String?

    fun getRequiredStringProp(propertyName: String): String

    fun getRequiredStringProp(propertyName: String, defaultValue: String): String

    fun getRequiredBooleanProp(propertyName: String, defaultValue: Boolean): Boolean

    fun getRequiredIntegerProp(propertyName: String): Int

    fun getRequiredIntegerProp(propertyName: String, defaultValue: Int): Int

    fun getIntegerProp(propertyName: String, defaultValue: Int? = null): Int?

    fun getDoubleProp(propertyName: String, defaultValue: Double? = null): Double?

    fun getBooleanProp(propertyName: String, defaultValue: Boolean? = null): Boolean?

    fun getStringListProp(propertyName: String, defaultValue: List<String>? = null): List<String>?
}
