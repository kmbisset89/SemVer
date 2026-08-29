package io.github.kmbisset89.semver.plugin.logic

import org.eclipse.jgit.lib.Repository
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit

/**
 * Fetches or pushes tags with the native Git CLI. Used when JGit cannot talk to a
 * remote — typical for Azure DevOps HTTPS URLs that contain `%20` — because the
 * CLI already has a working `origin` from the CI checkout (including
 * `http.extraheader` credentials).
 */
internal object GitCliTagSynchronizer {

    private val logger = LoggerFactory.getLogger(GitCliTagSynchronizer::class.java)
    private const val TIMEOUT_SECONDS = 60L

    fun fetchTags(repository: Repository, remoteName: String): Boolean {
        return run(repository, listOf("git", "fetch", remoteName, "--tags"))
    }

    fun pushTags(repository: Repository): Boolean {
        return run(repository, listOf("git", "push", "--tags"))
    }

    private fun run(repository: Repository, command: List<String>): Boolean {
        val workingDir = repository.workTree ?: repository.directory
        if (workingDir == null || !workingDir.exists()) {
            logger.warn("semver: cannot run '{}': git working directory is missing.", command.joinToString(" "))
            return false
        }
        return try {
            val processBuilder = ProcessBuilder(command)
                .directory(workingDir)
                .redirectErrorStream(true)
            processBuilder.environment()["GIT_TERMINAL_PROMPT"] = "0"
            val process = processBuilder.start()
            val finished = process.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            if (!finished) {
                process.destroyForcibly()
                logger.warn("semver: '{}' timed out after {}s.", command.joinToString(" "), TIMEOUT_SECONDS)
                return false
            }
            val output = process.inputStream.bufferedReader().readText().trim()
            val ok = process.exitValue() == 0
            if (!ok) {
                logger.warn("semver: '{}' failed: {}", command.joinToString(" "), output)
            }
            ok
        } catch (e: Exception) {
            logger.warn("semver: could not start '{}': {}", command.joinToString(" "), e.message)
            false
        }
    }
}
