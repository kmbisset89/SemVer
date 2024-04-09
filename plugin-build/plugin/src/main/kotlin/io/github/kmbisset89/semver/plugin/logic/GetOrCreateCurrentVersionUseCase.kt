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
        project.logger.quiet("Current branch: $branchName")

        val branchType = when {
            baseBranchName?.let { branchReadableName.contains(it) } ?: false -> TypeOfBranch.MAIN
            branchReadableName.contains("release/") -> TypeOfBranch.RELEASE
            branchReadableName.contains("feature/") -> TypeOfBranch.FEATURE
            branchReadableName.contains("bugfix/") -> TypeOfBranch.BUGFIX
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
            branchType == TypeOfBranch.FEATURE || branchType == TypeOfBranch.BUGFIX -> "${semVer.major}.${semVer.minor}.${semVer.patch}-beta.$timeStampString"
            else -> "${semVer.major}.${semVer.minor}.${semVer.patch}-alpha.$timeStampString"
        }
    }

    private enum class TypeOfBranch {
        MAIN, FEATURE, RELEASE, BUGFIX, DEFAULT
    }
}
