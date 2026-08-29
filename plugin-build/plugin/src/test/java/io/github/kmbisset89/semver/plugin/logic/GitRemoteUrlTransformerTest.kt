package io.github.kmbisset89.semver.plugin.logic

import org.eclipse.jgit.transport.URIish
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.platform.suite.api.Suite

@Suite
class GitRemoteUrlTransformerTest {

    @Test
    @DisplayName("Leaves already-encoded Azure repo names unchanged")
    fun leavesEncodedAzureUrlUnchanged() {
        val uri = URIish("https://TactWare@dev.azure.com/TactWare/QRG/_git/Knowledge%20Pack%20Creator")

        assertFalse(GitRemoteUrlTransformer.needsRewrite(uri))
        assertEquals(
            "https://TactWare@dev.azure.com/TactWare/QRG/_git/Knowledge%20Pack%20Creator",
            GitRemoteUrlTransformer.forJgitTransport(uri)
        )
    }

    @Test
    @DisplayName("Encodes literal spaces in an Azure repo path once")
    fun encodesLiteralSpacesOnce() {
        val uri = URIish("https://TactWare@dev.azure.com/TactWare/QRG/_git/Knowledge Pack Creator")

        assertTrue(GitRemoteUrlTransformer.needsRewrite(uri))
        assertEquals(
            "https://TactWare@dev.azure.com/TactWare/QRG/_git/Knowledge%20Pack%20Creator",
            GitRemoteUrlTransformer.forJgitTransport(uri)
        )
    }

    @Test
    @DisplayName("Collapses double-encoded spaces back to a single %20")
    fun collapsesDoubleEncodedSpaces() {
        val uri = URIish("https://dev.azure.com/TactWare/QRG/_git/Knowledge%2520Pack%2520Creator")

        assertTrue(GitRemoteUrlTransformer.needsRewrite(uri))
        assertEquals(
            "https://dev.azure.com/TactWare/QRG/_git/Knowledge%20Pack%20Creator",
            GitRemoteUrlTransformer.forJgitTransport(uri)
        )
    }

    @Test
    @DisplayName("Does not rewrite remotes without spaces")
    fun leavesPlainHttpsRemoteUnchanged() {
        val uri = URIish("https://github.com/kmbisset89/SemVer.git")

        assertFalse(GitRemoteUrlTransformer.needsRewrite(uri))
        assertEquals(
            "https://github.com/kmbisset89/SemVer.git",
            GitRemoteUrlTransformer.forJgitTransport(uri)
        )
    }

    @Test
    @DisplayName("Does not encode spaces in local file remotes")
    fun leavesFileRemoteWithSpacesUnchanged() {
        val uri = URIish("/tmp/Knowledge Pack Creator.git")

        assertFalse(GitRemoteUrlTransformer.needsRewrite(uri))
        assertEquals(
            "/tmp/Knowledge Pack Creator.git",
            GitRemoteUrlTransformer.forJgitTransport(uri)
        )
    }

    @Test
    @DisplayName("encodePathSpacesOnce is idempotent for %20")
    fun encodePathSpacesOnceIsIdempotent() {
        val encoded = "/TactWare/QRG/_git/Knowledge%20Pack%20Creator"
        assertEquals(encoded, GitRemoteUrlTransformer.encodePathSpacesOnce(encoded))
        assertEquals(
            encoded,
            GitRemoteUrlTransformer.encodePathSpacesOnce("/TactWare/QRG/_git/Knowledge Pack Creator")
        )
        assertEquals(
            encoded,
            GitRemoteUrlTransformer.encodePathSpacesOnce("/TactWare/QRG/_git/Knowledge%2520Pack%2520Creator")
        )
    }
}
