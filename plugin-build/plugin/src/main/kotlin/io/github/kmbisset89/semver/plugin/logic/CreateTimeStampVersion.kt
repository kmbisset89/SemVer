package io.github.kmbisset89.semver.plugin.logic

import kotlinx.datetime.Clock

/**
 * A class responsible for creating a timestamped version string from a given semantic version ([SemVer]).
 * This class appends a timestamp to the semantic version, differentiating between beta and alpha versions.
 *
 * @constructor Creates an instance of the class.
 */
class CreateTimeStampVersion {

    /**
     * Creates a timestamped version string based on the given [SemVer] instance and whether it's considered a beta version.
     *
     * @param semVer The semantic version from which to create the timestamped version string.
     * @param isBeta A flag indicating whether the version is a beta version. If `false`, the version is considered an alpha version. Defaults to `false`.
     * @return A string representing the timestamped version. It appends '-beta' followed by a substring of the current epoch milliseconds for beta versions,
     * or '-alpha' followed by a substring of the current epoch milliseconds for alpha versions.
     */
    operator fun invoke(semVer: SemVer, isBeta: Boolean = false): String {
        val now = Clock.System.now()
        // Obtain a substring of the current epoch milliseconds to use as a unique identifier for the version.
        // The substring starts from the 7th character to ensure it's a manageable length while maintaining uniqueness.
        val timeStampString = now.toEpochMilliseconds().toString().substring(7)

        // Return the formatted version string, appending '-beta' or '-alpha' along with the timestamp substring
        // based on the isBeta flag.
        return when(isBeta){
            true -> "${semVer.major}.${semVer.minor}.${semVer.patch}-beta$timeStampString"
            false -> "${semVer.major}.${semVer.minor}.${semVer.patch}-alpha.$timeStampString"
        }
    }
}
