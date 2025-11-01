package io.github.kmbisset89.semver.plugin.logic

import kotlinx.datetime.Clock
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.Ref
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.revwalk.RevWalk
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.gradle.api.Project
import java.io.File

/**
 * Use case for retrieving or generating the current version of a project based on its Git repository state,
 * considering the semantic version provided and the branch type. It generates version strings with additional
 * identifiers (such as alpha, beta, hotfix, or a timestamp) based on the branch type, the presence of uncommitted changes,
 * and whether the last commit was tagged.
 */
class GetOrCreateCurrentVersionUseCase {

    /**
     * @param semVer The base semantic version to use for generating the version string.
     * @param gitFilePath The file path to the Git repository. If null, a default version string is generated.
     * @param baseBranchName The name of the base or main branch in the repository.
     * @param repositoryFactory A factory function for creating a [Repository] instance. Defaults to creating a repository
     * from the provided git file path.
     * @param gitFactory A factory function for creating a [Git] instance from a [Repository]. Defaults to creating a [Git]
     * instance from the provided repository.
     * @return A version string based on the current Git repository state and the provided semantic version.
     */
    operator fun invoke(
        semVer: SemVer,
        gitFilePath: String?,
        baseBranchName: String?,
        project: Project,
        headCommit: String? = null,
        branchName: String? = null,
        betaBranchPrefixes: List<String>? = null,
        betaIncrementStrategy: String? = null,
        subProjectTag: String? = null,
        repositoryFactory: (String) -> Repository = {
            FileRepositoryBuilder().setGitDir(File("$it${File.separator}.git")).readEnvironment().findGitDir().build()
        },
        gitFactory: (Repository) -> Git = { Git(it) },
    ): String {
        val now = Clock.System.now()
        val timeStampString = now.toEpochMilliseconds().toString().substring(7)

        if (gitFilePath == null) {
            project.logger.error("Git file path is not provided. Generating a default version string.")
            // Return a default version if the git file path is not provided
            return "${semVer.major}.${semVer.minor}.${semVer.patch}-alpha.$timeStampString"
        }

        val repository = repositoryFactory(gitFilePath)
        val branchReadableName = branchName ?: repository.branch
        val shortBranchName = branchReadableName.removePrefix("refs/heads/")
        project.logger.quiet("Current branch: $shortBranchName")

        val configuredBetaPrefixes = betaBranchPrefixes ?: listOf("feature/", "feat/", "bugfix/", "fix/")

        val branchType = when {
            shortBranchName.startsWith("release/") -> TypeOfBranch.RELEASE
            shortBranchName.startsWith("task/") -> TypeOfBranch.DEFAULT
            baseBranchName?.let { shortBranchName == it } ?: false -> TypeOfBranch.MAIN
            configuredBetaPrefixes.any { prefix -> shortBranchName.startsWith(prefix) } -> TypeOfBranch.FEATURE
            else -> TypeOfBranch.DEFAULT
        }

        val git = gitFactory(repository)

        // Check for uncommitted changes
        val hasUncommittedChanges = git.status().call().isClean.not()

        // Check if the last commit was tagged
        return when {
            branchType == TypeOfBranch.MAIN && CheckIfLastCommitIsTagged().invoke(
                gitFilePath,
                headCommit,
            ) -> semVer.toString()

            branchType == TypeOfBranch.MAIN -> "${semVer.major}.${semVer.minor}.${semVer.patch}-$timeStampString"
            branchType == TypeOfBranch.RELEASE && !hasUncommittedChanges -> semVer.toString()
            branchType == TypeOfBranch.RELEASE && hasUncommittedChanges -> "${semVer.major}.${semVer.minor}.${semVer.patch + 1}-hotfix.$timeStampString"
            branchType == TypeOfBranch.FEATURE || branchType == TypeOfBranch.BUGFIX -> {
                val useSequential = betaIncrementStrategy.equals("SEQUENTIAL", ignoreCase = true)
                if (useSequential) {
                    val next = nextBetaNumber(git, semVer, subProjectTag)
                    "${semVer.major}.${semVer.minor}.${semVer.patch}-beta.$next"
                } else {
                    "${semVer.major}.${semVer.minor}.${semVer.patch}-beta.$timeStampString"
                }
            }
            else -> "${semVer.major}.${semVer.minor}.${semVer.patch}-alpha.$timeStampString"
        }
    }

    private enum class TypeOfBranch {
        MAIN, FEATURE, RELEASE, BUGFIX, DEFAULT
    }

    private fun nextBetaNumber(git: Git, semVer: SemVer, subProjectTag: String?): Int {
        val tags = git.tagList().call()
        val localNames = tags.map { it.name.substringAfterLast("/") }

        val filtered = localNames.filter { localName ->
            if (subProjectTag.isNullOrBlank()) true else (
                localName.startsWith("$subProjectTag-") || localName.endsWith("-$subProjectTag")
            )
        }

        val regex = SemVerConstants.betaRegex

        val maxForBase = filtered.mapNotNull { name ->
            val m = regex.find(name)?.groupValues
            if (m != null && m.size >= 5) {
                val major = m[1].toInt()
                val minor = m[2].toInt()
                val patch = m[3].toInt()
                val n = m[4].toInt()
                if (major == semVer.major && minor == semVer.minor && patch == semVer.patch) n else null
            } else null
        }.maxOrNull() ?: 0

        return maxForBase + 1
    }
}
