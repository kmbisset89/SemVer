package io.github.kmbisset89.semver.plugin.logic

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.revwalk.RevWalk
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.gradle.api.logging.Logger
import org.slf4j.LoggerFactory
import java.io.File

class CheckIfLastCommitIsTagged {

    private val logger = LoggerFactory.getLogger(CheckIfLastCommitIsTagged::class.java)
    operator fun invoke(
        gitFilePath: String,
        lastCommitHash: String ? = null,
        repositoryFactory: (String) -> Repository = {
            FileRepositoryBuilder().setGitDir(File("$it${File.separator}.git")).readEnvironment().findGitDir().build()
        },
        gitFactory: (Repository) -> Git = { Git(it) }
    ) : Boolean{
        val repository = repositoryFactory(gitFilePath)
        val git = gitFactory(repository)

        val tags = git.tagList().call()

        RevWalk(repository).use { revWalk ->
            // Resolve the commit to check: use latestCommitHash if provided, otherwise default to HEAD
            val commitToCheckId = if (lastCommitHash != null) {
                repository.resolve(lastCommitHash)?.let { revWalk.parseCommit(it)?.id }
            } else {
                repository.resolve("HEAD")?.let { revWalk.parseCommit(it)?.id }
            }

            logger.info(
                "Checking if the last commit is tagged with commit ID: ${commitToCheckId?.name ?: "HEAD"}"
            )

            return tags.any { tag ->
                // Resolve the commit that the tag points to. This can be direct (lightweight tag) or indirect (annotated tag)
                val commitId = if (tag.peeledObjectId != null) {
                    revWalk.parseCommit(tag.peeledObjectId)?.id
                } else {
                    revWalk.parseCommit(tag.objectId)?.id
                }

                logger.info(
                    "Checking tag: ${tag.name} with commit ID: ${commitId?.name ?: "null"}"
                )

                (commitId?.let {
                    if (commitToCheckId?.name == null && commitId.name == null) {
                        return false
                    }
                    // Compare the specified commit's ID (or HEAD if not specified) with the tag's commit ID
                    commitToCheckId?.name == commitId.name
                } ?: false).also {
                    if (it) {
                        logger.info("Last commit is tagged with tag: ${tag.name}")
                    }
                }
            }
        }

    }
}
