package io.github.kmbisset89.semver.plugin.logic

object SemVerConstants {

    val semVerRegex = """v?(\d+)\.(\d+)\.(\d+)(-rc\.(\d+))?""".toRegex()

}
