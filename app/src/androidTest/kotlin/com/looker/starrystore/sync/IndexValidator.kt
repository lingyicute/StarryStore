package com.looker.starrystore.sync

import com.looker.starrystore.domain.model.Fingerprint
import java.util.jar.JarEntry

val FakeIndexValidator = object : IndexValidator {
    override suspend fun validate(
        jarEntry: JarEntry,
        expectedFingerprint: Fingerprint?
    ): Fingerprint {
        return expectedFingerprint ?: Fingerprint("0".repeat(64))
    }
}
