package io.github.kmbisset89.semver.plugin.logic

import org.eclipse.jgit.transport.RemoteConfig
import org.eclipse.jgit.transport.URIish
import java.net.URISyntaxException

/**
 * Rewrites HTTP(S) Git remotes so JGit can open Azure DevOps repositories whose
 * project or repo names contain spaces.
 *
 * Azure stores those names as `%20`. JGit's HTTP transport builds `java.net.URL`
 * from [URIish.toString], which uses the raw path. Literal spaces make that URL
 * illegal; a second encode turns `%20` into `%2520`, which Azure resolves as a
 * different (missing) repository. Fetch then fails and versioning falls back to
 * whatever tags happen to be local — often stale in CI.
 */
internal object GitRemoteUrlTransformer {

    fun remoteFetchTarget(remote: RemoteConfig): String {
        val uri = remote.urIs.firstOrNull() ?: return remote.name
        return if (needsRewrite(uri)) forJgitTransport(uri) else remote.name
    }

    fun forJgitTransport(uri: URIish): String {
        if (!isHttp(uri)) return uri.toString()
        val rawPath = uri.rawPath ?: uri.path ?: return uri.toString()
        val rewritten = encodePathSpacesOnce(rawPath)
        if (rewritten == rawPath) return uri.toString()
        return try {
            uri.setRawPath(rewritten).toString()
        } catch (_: URISyntaxException) {
            uri.toString()
        }
    }

    fun needsRewrite(uri: URIish): Boolean {
        if (!isHttp(uri)) return false
        val rawPath = uri.rawPath ?: uri.path ?: return false
        return rawPath.contains(' ') || rawPath.contains("%2520", ignoreCase = true)
    }

    internal fun encodePathSpacesOnce(path: String): String =
        path.replace("%2520", "%20", ignoreCase = true).replace(" ", "%20")

    private fun isHttp(uri: URIish): Boolean {
        val scheme = uri.scheme ?: return false
        return scheme.equals("http", ignoreCase = true) || scheme.equals("https", ignoreCase = true)
    }
}
