package com.looker.starrystore.sync.v1

import com.looker.starrystore.domain.model.Fingerprint
import com.looker.starrystore.domain.model.Repo
import com.looker.starrystore.sync.IndexValidator
import com.looker.starrystore.sync.Parser
import com.looker.starrystore.sync.utils.toJarFile
import com.looker.starrystore.sync.v1.model.IndexV1
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.File

class V1Parser(
    private val dispatcher: CoroutineDispatcher,
    private val json: Json,
    private val validator: IndexValidator,
) : Parser<IndexV1> {

    override suspend fun parse(
        file: File,
        repo: Repo,
    ): Pair<Fingerprint, IndexV1> = withContext(dispatcher) {
        val jar = file.toJarFile()
        val entry = jar.getJarEntry("index-v1.json")
        val indexString = jar.getInputStream(entry).use {
            it.readBytes().decodeToString()
        }
        validator.validate(entry, repo.fingerprint) to json.decodeFromString(indexString)
    }
}
