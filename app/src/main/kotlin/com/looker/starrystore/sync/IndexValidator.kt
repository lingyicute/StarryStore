package com.looker.starrystore.sync

import com.looker.starrystore.domain.model.Fingerprint
import com.looker.starrystore.network.validation.ValidationException
import java.util.jar.JarEntry

interface IndexValidator {

    @Throws(ValidationException::class)
    suspend fun validate(
        jarEntry: JarEntry,
        expectedFingerprint: Fingerprint?,
    ): Fingerprint

}
