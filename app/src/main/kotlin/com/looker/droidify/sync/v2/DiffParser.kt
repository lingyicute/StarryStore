package com.looker.starrystore.sync.v2

import com.looker.starrystore.domain.model.Fingerprint
import com.looker.starrystore.domain.model.Repo
import com.looker.starrystore.sync.Parser
import com.looker.starrystore.sync.v2.model.IndexV2Diff
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.File

class DiffParser(
    private val dispatcher: CoroutineDispatcher,
    private val json: Json,
) : Parser<IndexV2Diff> {

    override suspend fun parse(
        file: File,
        repo: Repo
    ): Pair<Fingerprint, IndexV2Diff> = withContext(dispatcher) {
        requireNotNull(repo.fingerprint) {
            "Fingerprint should not be null when parsing diff"
        } to json.decodeFromString(file.readBytes().decodeToString())
    }
}
