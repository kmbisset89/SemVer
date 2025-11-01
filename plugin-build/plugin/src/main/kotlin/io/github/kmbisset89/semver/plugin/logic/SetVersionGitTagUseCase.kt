package io.github.kmbisset89.semver.plugin.logic

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.revwalk.RevWalk
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider
import java.io.File

/**
 * Use case for setting a version tag in a Git repository. This class provides functionality to create a new Git tag
 * based on the specified semantic version ([SemVer]) and, optionally, a branch name. The tag is then pushed to the remote
 * repository using the provided Git username and personal access token (PAT).
 */
class SetVersionGitTagUseCase {

    /**
     * @param gitPath The file system path to the local Git repository.
     * @param gitUser The Git username used for authentication.
     * @param gitPat The Git personal access token (PAT) used for authentication.
     * @param version The semantic version ([SemVer]) to use for tagging.
     * @param overrideBranch An optional branch name to prefix the tag with. If provided, the tag will be formatted as
     * "${overrideBranch}-v${version}". If null, the tag will be formatted as "v${version}".
     * @param repositoryFactory A lambda function to create a [Repository] instance from the gitPath. By default, it uses
     * [FileRepositoryBuilder] to locate and build a [Repository] instance.
     * @param gitFactory A lambda function to create a [Git] instance from a [Repository]. By default, it constructs a new
     * [Git] instance.
     */
    operator fun invoke(
        gitPath: String,
        gitUser: String,
        gitPat: String,
        version: SemVer,
        overrideBranch: String?,
        subProjectTag: String? = null,
        repositoryFactory: (String) -> Repository = {
            FileRepositoryBuilder().setGitDir(File("$it${File.separator}.git")).readEnvironment().findGitDir().build()
        },
        gitFactory: (Repository) -> Git = { Git(it) },
    ) {
        // Construct the repository instance using the provided path and factory function.
        val repository = repositoryFactory(gitPath)
        // Construct the Git instance using the provided repository and factory function.
        val git = gitFactory(repository)

        // Check if there are any uncommitted changes in the repository.
        if (areChangesPresent(git)) {
            throw IllegalStateException("Cannot create a tag with uncommitted changes")
        }

        // Prepare the credentials for pushing the tag to the remote repository.
        val credProvider = UsernamePasswordCredentialsProvider(gitUser, gitPat)
        // Determine the tag name based on the version and optional prefixes.
        // New scheme: when a module tag (subProjectTag) is provided, use suffix style: vX.Y.Z-<moduleTag>
        // Otherwise, keep plain global tag or optional branch prefix when provided.
        val tagName = when {
            !subProjectTag.isNullOrBlank() -> "v$version-$subProjectTag"
            !overrideBranch.isNullOrBlank() -> "${overrideBranch}-v$version"
            else -> "v$version"
        }
        // Create the tag in the local repository.
        git.tag().setName(tagName).call()

        // Push the tag to the remote repository using the provided credentials.
        git.push().setCredentialsProvider(credProvider).setPushTags().call()
    }


    private fun areChangesPresent(git: Git): Boolean {
        val status = git.status().call()

        // Check if there are any uncommitted changes
        return status.added.isNotEmpty() ||
            status.changed.isNotEmpty() ||
            status.modified.isNotEmpty() ||
            status.removed.isNotEmpty()
    }
}
