package com.looker.starrystore.sync.v1

import android.content.Context
import com.looker.starrystore.domain.model.Fingerprint
import com.looker.starrystore.domain.model.Repo
import com.looker.starrystore.sync.Parser
import com.looker.starrystore.sync.Syncable
import com.looker.starrystore.sync.common.INDEX_V1_NAME
import com.looker.starrystore.sync.common.IndexJarValidator
import com.looker.starrystore.sync.common.JsonParser
import com.looker.starrystore.sync.common.downloadIndex
import com.looker.starrystore.sync.common.toV2
import com.looker.starrystore.sync.v1.model.IndexV1
import com.looker.starrystore.sync.v2.model.IndexV2
import com.looker.starrystore.network.Downloader
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class V1Syncable(
    private val context: Context,
    private val downloader: Downloader,
    private val dispatcher: CoroutineDispatcher,
) : Syncable<IndexV1> {
    override val parser: Parser<IndexV1>
        get() = V1Parser(
            dispatcher = dispatcher,
            json = JsonParser,
            validator = IndexJarValidator(dispatcher),
        )

    override suspend fun sync(repo: Repo): Pair<Fingerprint, IndexV2> =
        withContext(dispatcher) {
            val jar = downloader.downloadIndex(
                context = context,
                repo = repo,
                url = repo.address.removeSuffix("/") + "/$INDEX_V1_NAME",
                fileName = INDEX_V1_NAME,
            )
            val (fingerprint, indexV1) = parser.parse(jar, repo)
            jar.delete()
            fingerprint to indexV1.toV2()
        }
}
