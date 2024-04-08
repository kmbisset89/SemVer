package io.github.kmbisset89.semver.plugin.logic

import kotlinx.datetime.Clock
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.Ref
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.revwalk.RevWalk
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
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
        repositoryFactory: (String) -> Repository = {
            FileRepositoryBuilder().setGitDir(File("$it${File.separator}.git")).readEnvironment().findGitDir().build()
        },
        gitFactory: (Repository) -> Git = { Git(it) },
    ): String {
        val now = Clock.System.now()
        val timeStampString = now.toEpochMilliseconds().toString().substring(7)

        if (gitFilePath == null) {
            // Return a default version if the git file path is not provided
            return "${semVer.major}.${semVer.minor}.${semVer.patch}-alpha.$timeStampString"
        }

        val repository = repositoryFactory(gitFilePath)
        val branchName = repository.branch

        val branchType = when {
            branchName == baseBranchName -> TypeOfBranch.MAIN
            branchName.startsWith("release/") -> TypeOfBranch.RELEASE
            branchName.startsWith("feature/") -> TypeOfBranch.FEATURE
            branchName.startsWith("bugfix/") -> TypeOfBranch.BUGFIX
            else -> TypeOfBranch.DEFAULT
        }

        val git = gitFactory(repository)

        // Check for uncommitted changes
        val hasUncommittedChanges = git.status().call().isClean.not()

        // Check if the last commit was tagged
        val tags = git.tagList().call()

        return when {
            branchType == TypeOfBranch.MAIN && checkIfLastCommitIsTagged(tags, repository) -> semVer.toString()
            branchType == TypeOfBranch.MAIN -> "${semVer.major}.${semVer.minor}.${semVer.patch}-$timeStampString"
            branchType == TypeOfBranch.RELEASE && !hasUncommittedChanges -> semVer.toString()
            branchType == TypeOfBranch.RELEASE && hasUncommittedChanges -> "${semVer.major}.${semVer.minor}.${semVer.patch + 1}-hotfix.$timeStampString"
            branchType == TypeOfBranch.FEATURE || branchType == TypeOfBranch.BUGFIX -> "${semVer.major}.${semVer.minor}.${semVer.patch}-beta.$timeStampString"
            else -> "${semVer.major}.${semVer.minor}.${semVer.patch}-alpha.$timeStampString"
        }
    }

    private fun checkIfLastCommitIsTagged(tags: List<Ref>, repository: Repository): Boolean {
        RevWalk(repository).use { revWalk ->
            // Resolve the HEAD commit
            val headCommitId = repository.resolve("HEAD")?.let { revWalk.parseCommit(it).id }

            return tags.any { tag ->
                // Resolve the commit that the tag points to. This can be direct (lightweight tag) or indirect (annotated tag)
                val commitId = if (tag.peeledObjectId != null) {
                    revWalk.parseCommit(tag.peeledObjectId)?.id
                } else {
                    revWalk.parseCommit(tag.objectId)?.id
                }

                // Compare the HEAD commit's ID with the tag's commit ID
                headCommitId?.name == commitId?.name
            }
        }
    }

    private enum class TypeOfBranch {
        MAIN, FEATURE, RELEASE, BUGFIX, DEFAULT
    }
}
