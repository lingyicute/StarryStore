package com.looker.starrystore.sync

import com.looker.starrystore.domain.model.Fingerprint
import com.looker.starrystore.domain.model.Repo
import java.io.File

interface Parser<out T> {

    suspend fun parse(
        file: File,
        repo: Repo,
    ): Pair<Fingerprint, T>

}
