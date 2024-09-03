package com.looker.sync.fdroid.v2

import com.looker.core.domain.model.Fingerprint
import com.looker.core.domain.model.Repo
import com.looker.network.Downloader
import com.looker.sync.fdroid.Parser
import com.looker.sync.fdroid.Syncable
import com.looker.sync.fdroid.common.IndexJarValidator
import com.looker.sync.fdroid.common.JsonParser
import com.looker.sync.fdroid.common.downloadIndex
import com.looker.sync.fdroid.v2.model.Entry
import com.looker.sync.fdroid.v2.model.IndexV2
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class EntrySyncable(
    private val downloader: Downloader,
    private val dispatcher: CoroutineDispatcher,
) : Syncable<Entry> {
    override val parser: Parser<Entry>
        get() = EntryParser(
            dispatcher = dispatcher,
            json = JsonParser.parser,
            validator = IndexJarValidator(dispatcher),
        )

    private val indexParser: Parser<IndexV2> = V2Parser(
        dispatcher = dispatcher,
        json = JsonParser.parser,
    )

    override suspend fun sync(repo: Repo): Pair<Fingerprint, IndexV2?> =
        withContext(Dispatchers.IO) {
            val jar = downloader.downloadIndex(
                context = context,
                repo = repo,
                url = repo.address.removeSuffix("/") + "/$ENTRY_V2_NAME",
                fileName = ENTRY_V2_NAME
            )
            val (fingerprint, entry) = parser.parse(jar, repo)
            val index = entry.getDiff(repo.versionInfo.timestamp)
                ?: return@withContext fingerprint to null
            val indexPath = repo.address.removeSuffix("/") + index.name
            val (_, indexV2) = indexParser.parse(indexFile, repo)
                // example https://apt.izzysoft.de/fdroid/repo/index-v2.json
                val newIndexFile = downloader.downloadIndex(
                    context = context,
                    repo = repo,
                    url = indexPath,
                    fileName = INDEX_V2_NAME,
                )
            fingerprint to indexV2
        }
}
