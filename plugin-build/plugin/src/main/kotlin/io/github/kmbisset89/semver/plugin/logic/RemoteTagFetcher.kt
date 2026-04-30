package io.github.kmbisset89.semver.plugin.logic

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.errors.GitAPIException
import org.eclipse.jgit.transport.CredentialsProvider
import org.eclipse.jgit.transport.TagOpt
import org.slf4j.LoggerFactory

/**
 * Ensures local tag refs include those from remotes. [Git.tagList] only sees local refs;
 * shallow clones and CI checkouts often omit tags unless explicitly fetched.
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
            try {
                val cmd = git.fetch()
                    .setRemote(remote.name)
                    .setTagOpt(TagOpt.FETCH_TAGS)
                if (credentialsProvider != null) {
                    cmd.setCredentialsProvider(credentialsProvider)
                }
                cmd.call()
            } catch (e: GitAPIException) {
                logger.warn(
                    "semver: failed to fetch tags from remote '{}': {}. Using locally known tags only.",
                    remote.name,
                    e.message
                )
            }
        }
    }
}
