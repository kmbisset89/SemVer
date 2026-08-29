package io.github.kmbisset89.semver.plugin.logic

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.Ref
import org.eclipse.jgit.transport.URIish
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.platform.suite.api.Suite
import java.nio.file.Files

@Suite
class RemoteTagFetcherTest {

    @Test
    @DisplayName("Fetches tags from a remote whose directory name contains spaces")
    fun fetchesTagsFromRemoteWithSpacesInPath() {
        val parent = Files.createTempDirectory("semver-space-remote").toFile()
        parent.deleteOnExit()

        val publisherDir = parent.resolve("publisher")
        val publisher = Git.init().setDirectory(publisherDir).call()
        publisherDir.resolve("README.md").writeText("init")
        publisher.add().addFilepattern("README.md").call()
        publisher.commit().setMessage("init").call()
        publisher.tag().setName("v3.2.1").call()

        val bareDir = parent.resolve("Knowledge Pack Creator.git")
        Git.init().setDirectory(bareDir).setBare(true).call().close()
        publisher.remoteAdd().setName("origin").setUri(URIish(bareDir.absolutePath)).call()
        publisher.push().setPushTags().setPushAll().call()
        publisher.close()

        val consumerDir = parent.resolve("consumer")
        val consumer = Git.cloneRepository()
            .setURI(bareDir.absolutePath)
            .setDirectory(consumerDir)
            .setCloneAllBranches(true)
            .call()
        consumer.tagDelete().setTags("v3.2.1").call()
        assertTrue(consumer.tagList().call().none { tagName(it) == "v3.2.1" })

        RemoteTagFetcher.fetchAllTags(consumer, null)

        assertTrue(
            consumer.tagList().call().any { tagName(it) == "v3.2.1" },
            "Expected v3.2.1 to be fetched from the remote with spaces in its path"
        )
        consumer.close()
    }

    private fun tagName(ref: Ref): String = ref.name.substringAfterLast("/")
}
