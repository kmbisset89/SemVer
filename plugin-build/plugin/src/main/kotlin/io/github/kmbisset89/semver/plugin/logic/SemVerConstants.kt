package io.github.kmbisset89.semver.plugin.logic

object SemVerConstants {

    val semVerRegex = """v?(\d+)\.(\d+)\.(\d+)(-rc\.(\d+))?""".toRegex()
    // Flexible beta regex that matches tags with optional prefixes before a -v or v segment
    // Examples: "v1.2.3-beta.4", "api-main-v1.2.3-beta.4"
    val betaRegex = ".*?v?(\\d+)\\.(\\d+)\\.(\\d+)-beta\\.(\\d+)".toRegex()

}
