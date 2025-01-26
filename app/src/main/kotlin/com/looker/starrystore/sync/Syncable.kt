package com.looker.starrystore.sync

import com.looker.starrystore.domain.model.Fingerprint
import com.looker.starrystore.domain.model.Repo

/**
 * Expected Architecture: [https://excalidraw.com/#json=JqpGunWTJONjq-ecDNiPg,j9t0X4coeNvIG7B33GTq6A]
 *
 * Current Issue: When downloading entry.jar we need to re-call the synchronizer,
 * which this arch doesn't allow.
 */
interface Syncable<T> {

    val parser: Parser<T>

    suspend fun sync(
        repo: Repo,
    ): Pair<Fingerprint, com.looker.starrystore.sync.v2.model.IndexV2?>

}
