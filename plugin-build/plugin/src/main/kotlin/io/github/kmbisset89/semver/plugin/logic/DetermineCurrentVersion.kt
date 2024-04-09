package io.github.kmbisset89.semver.plugin.logic

import io.github.kmbisset89.semver.plugin.logic.SemVerConstants.semVerRegex
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.errors.GitAPIException
import org.eclipse.jgit.lib.ObjectId
import org.eclipse.jgit.lib.Ref
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.revwalk.RevWalk
import org.eclipse.jgit.revwalk.filter.RevFilter
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider
import java.io.File

/**
 * Utility class to determine the current version of a software project based on git tags.
 * It analyzes git tags to find the highest semantic version (SemVer) that has been merged into a specific branch.
 *
 * @constructor Creates an instance to determine the current version.
 */
class DetermineCurrentVersion {

    /**
     * Determines the current version of the project based on git tags.
     *
     * @param gitFilePath The file path to the git repository.
     * @param branchName The name of the branch for which to determine the current version.
     * @param repositoryFactory A factory function to create a [Repository] instance. Defaults to a function that initializes a repository using the provided [gitFilePath].
     * @param gitFactory A factory function to create a [Git] instance. Defaults to a function that creates a [Git] instance from a [Repository].
     * @param revWalkFactory A factory function to create a [RevWalk] instance. Defaults to a function that creates a [RevWalk] instance from a [Repository].
     * @return The current version as a [SemVer] instance. If no version can be determined, returns [SemVer.Default].
     */
    fun determineCurrentVersion(
        gitFilePath: String?,
        branchName: String?,
        credentialsProvider: UsernamePasswordCredentialsProvider,
        repositoryFactory: (String) -> Repository = {
            FileRepositoryBuilder().setGitDir(File("$it${File.separator}.git")).readEnvironment().findGitDir().build()
        },
        gitFactory: (Repository) -> Git = { Git(it) },
        revWalkFactory: (Repository) -> RevWalk = { RevWalk(it) }
    ): SemVer {
        if (gitFilePath == null || branchName == null) return SemVer.Default

        val repository = repositoryFactory(gitFilePath)
        val git = gitFactory(repository)

        var branchRef = repository.findRef(branchName)
        if (branchRef == null) {
            // The branch is not found, attempt to fetch from remote and try again
            try {
                git.fetch().setCredentialsProvider(credentialsProvider).setRemote("origin")
                    .setRefSpecs("+refs/heads/$branchName:refs/remotes/origin/$branchName").call()
                // After fetch, try to find the branch again
                branchRef = repository.findRef(branchName)
            } catch (e: GitAPIException) {
                e.printStackTrace()
                return SemVer.Default // Return default if fetch fails or branch still not found
            }
        }

        // If branchRef is still null after fetch, return default
        if (branchRef == null) return SemVer.Default

        val branchObjectId: ObjectId =
            repository.resolve(branchName) ?: return SemVer.Default // Return default if cannot resolve branch name

        val revWalk = revWalkFactory(repository).apply {
            this.revFilter = RevFilter.MERGE_BASE
        }

        val branchCommit: RevCommit =
            revWalk.parseCommit(branchObjectId) ?: return SemVer.Default // Return default if cannot parse commit

        val tags: List<Ref> = git.tagList().call()

        val sortedTags = tags.mapNotNull { tag ->
            val tagCommit = revWalk.parseCommit(tag.objectId)
            if (revWalk.isMergedInto(tagCommit, branchCommit)) {
                semVerRegex.find(tag.name.substringAfterLast("/"))?.let { matchResult ->
                    val (major, minor, patch, _, rc) = matchResult.destructured
                    val semVer = rc.toIntOrNull()?.let {
                        SemVer.ReleaseCandidate(major.toInt(), minor.toInt(), patch.toInt(), it)
                    } ?: SemVer.Final(major.toInt(), minor.toInt(), patch.toInt())
                    semVer to tag
                }
            } else null
        }.sortedWith(compareByDescending<Pair<SemVer, Ref>> { it.first.major }
            .thenByDescending { it.first.minor }
            .thenByDescending { it.first.patch }
            .thenByDescending { (it.first as? SemVer.ReleaseCandidate)?.releaseCandidateNumber ?: 0 }
        ).map { it.first }

        return sortedTags.firstOrNull() ?: SemVer.Default
    }
}
