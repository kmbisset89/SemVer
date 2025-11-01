package io.github.kmbisset89.semver.plugin.logic

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.diff.DiffEntry
import org.eclipse.jgit.lib.ObjectId
import org.eclipse.jgit.lib.Ref
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.revwalk.RevWalk
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import java.io.File

/**
 * Computes whether given paths have changed since the last tag for a module
 * identified by a tag suffix (e.g., vX.Y.Z-<moduleTag>). If no such tag exists,
 * falls back to the latest global tag (any tag containing a SemVer), as the baseline.
 */
class ChangeDetectionUseCase {

    data class Baseline(val semVer: SemVer, val ref: Ref)

    operator fun invoke(
        gitRoot: String,
        moduleTagSuffix: String?,
        pathsToCheck: List<String>,
        repositoryFactory: (String) -> Repository = {
            FileRepositoryBuilder().setGitDir(File("$it${File.separator}.git")).readEnvironment().findGitDir().build()
        }
    ): Boolean {
        val repository = repositoryFactory(gitRoot)
        val git = Git(repository)
        val revWalk = RevWalk(repository)

        try {
            val baseline = findBaseline(git, revWalk, moduleTagSuffix)
            val headCommit = repository.resolve("HEAD") ?: return pathsToCheck.isNotEmpty()

            return hasPathChangesSince(git, revWalk, baseline?.ref, headCommit, pathsToCheck)
        } finally {
            revWalk.dispose()
            repository.close()
        }
    }

    private fun findBaseline(git: Git, revWalk: RevWalk, moduleTagSuffix: String?): Baseline? {
        val tags = git.tagList().call()

        val candidates = tags.mapNotNull { tag ->
            val localName = tag.name.substringAfterLast("/")
            val m = SemVerConstants.semVerRegex.find(localName) ?: return@mapNotNull null
            // Filter by suffix when provided; otherwise accept all SemVer tags
            if (!moduleTagSuffix.isNullOrBlank()) {
                if (!localName.endsWith("-$moduleTagSuffix")) return@mapNotNull null
            }
            makeSemVer(m, tag)
        }

        val considered = if (candidates.isEmpty() && !moduleTagSuffix.isNullOrBlank()) {
            // Fallback to latest global SemVer tag if none found for the module
            tags.mapNotNull { tag ->
                val localName = tag.name.substringAfterLast("/")
                val m = SemVerConstants.semVerRegex.find(localName) ?: return@mapNotNull null
                makeSemVer(m, tag)
            }
        } else candidates

        val sorted = considered.sortedWith(compareByDescending<Pair<SemVer, Ref>> { it.first.major }
            .thenByDescending { it.first.minor }
            .thenByDescending { it.first.patch }
            .thenByDescending { (it.first as? SemVer.ReleaseCandidate)?.releaseCandidateNumber ?: Int.MAX_VALUE })

        return sorted.firstOrNull()?.let { Baseline(it.first, it.second) }
    }

    private fun hasPathChangesSince(
        git: Git,
        revWalk: RevWalk,
        baselineRef: Ref?,
        head: ObjectId,
        pathsToCheck: List<String>
    ): Boolean {
        if (pathsToCheck.isEmpty()) return false

        val headCommit = revWalk.parseCommit(head)
        val headTree = headCommit.tree

        val oldTreeId: ObjectId? = when {
            baselineRef == null -> null
            baselineRef.peeledObjectId != null -> baselineRef.peeledObjectId
            else -> baselineRef.objectId
        }

        val oldTree = oldTreeId?.let { revWalk.parseCommit(it).tree }

        val diffs: List<DiffEntry> = git.diff().apply {
            setShowNameAndStatusOnly(true)
            if (oldTree != null) setOldTree(
                RevTreeIterator(
                    revWalk,
                    oldTree
                )
            ) else setOldTree(org.eclipse.jgit.treewalk.EmptyTreeIterator())
            setNewTree(RevTreeIterator(revWalk, headTree))
        }.call()

        return diffs.any { entry ->
            val path = when (entry.changeType) {
                DiffEntry.ChangeType.ADD, DiffEntry.ChangeType.COPY, DiffEntry.ChangeType.MODIFY, DiffEntry.ChangeType.RENAME -> entry.newPath
                DiffEntry.ChangeType.DELETE -> entry.oldPath
                else -> entry.newPath
            }
            pathsToCheck.any { check -> normalizePath(path).startsWith(normalizePath(check)) }
        }
    }

    private fun makeSemVer(matchResult: MatchResult, tag: Ref): Pair<SemVer, Ref> {
        val (major, minor, patch, _, rc) = matchResult.destructured
        val semVer = rc.toIntOrNull()?.let {
            SemVer.ReleaseCandidate(major.toInt(), minor.toInt(), patch.toInt(), it)
        } ?: SemVer.Final(major.toInt(), minor.toInt(), patch.toInt())
        return semVer to tag
    }

    private fun normalizePath(p: String): String = p.replace('\\', '/').trimStart('/')
}

/**
 * Lightweight iterator for JGit DiffCommand requiring AbstractTreeIterator; wraps a parsed RevTree.
 */
internal class RevTreeIterator(revWalk: RevWalk, commitTree: org.eclipse.jgit.lib.AnyObjectId) :
    org.eclipse.jgit.treewalk.CanonicalTreeParser() {
    init {
        val reader = revWalk.objectReader
        reset(reader, commitTree)
    }
}


