package io.github.kmbisset89.semver.plugin.logic

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.errors.GitAPIException
import org.eclipse.jgit.transport.CredentialsProvider
import org.eclipse.jgit.transport.RemoteConfig
import org.eclipse.jgit.transport.TagOpt
import org.slf4j.LoggerFactory

/**
 * Ensures local tag refs include those from remotes. [Git.tagList] only sees local refs;
 * shallow clones and CI checkouts often omit tags unless explicitly fetched.
 *
 * Azure DevOps remotes with spaces in the repo or project name are fetched with a
 * rewritten URL, then with native git if JGit still cannot connect.
 */
internal object RemoteTagFetcher {

    private val logger = LoggerFactory.getLogger(RemoteTagFetcher::class.java)

    fun fetchAllTags(git: Git, credentialsProvider: CredentialsProvider?) {
        val remotes = try {
            git.remoteList().call()
        } catch (e: GitAPIException) {
            logger.debug("semver: could not list git remotes: {}", e.message)
            return
        }
        if (remotes.isEmpty()) return

        for (remote in remotes) {
            if (fetchFromRemote(git, remote, credentialsProvider)) continue
            logger.warn(
                "semver: failed to fetch tags from remote '{}'. Using locally known tags only.",
                remote.name
            )
        }
    }

    private fun fetchFromRemote(
        git: Git,
        remote: RemoteConfig,
        credentialsProvider: CredentialsProvider?,
    ): Boolean {
        if (fetchWithJgit(git, remote.name, credentialsProvider)) return true

        val rewritten = GitRemoteUrlTransformer.remoteFetchTarget(remote)
        if (rewritten != remote.name && fetchWithJgit(git, rewritten, credentialsProvider)) {
            logger.info(
                "semver: fetched tags from remote '{}' using a space-safe URL.",
                remote.name
            )
            return true
        }

        if (GitCliTagSynchronizer.fetchTags(git.repository, remote.name)) {
            logger.info(
                "semver: fetched tags from remote '{}' using native git.",
                remote.name
            )
            return true
        }
        return false
    }

    private fun fetchWithJgit(
        git: Git,
        remote: String,
        credentialsProvider: CredentialsProvider?,
    ): Boolean {
        return try {
            val cmd = git.fetch()
                .setRemote(remote)
                .setTagOpt(TagOpt.FETCH_TAGS)
            if (credentialsProvider != null) {
                cmd.setCredentialsProvider(credentialsProvider)
            }
            cmd.call()
            true
        } catch (e: Exception) {
            logger.debug("semver: JGit tag fetch from '{}' failed: {}", remote, e.message)
            false
        }
    }
}
