package com.looker.starrystore.sync.common

import com.looker.starrystore.domain.model.Fingerprint
import com.looker.starrystore.domain.model.check
import com.looker.starrystore.domain.model.fingerprint
import com.looker.starrystore.network.validation.invalid
import com.looker.starrystore.sync.utils.certificate
import com.looker.starrystore.sync.utils.codeSigner
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.util.jar.JarEntry

class IndexJarValidator(
    private val dispatcher: CoroutineDispatcher
) : com.looker.starrystore.sync.IndexValidator {
    override suspend fun validate(
        jarEntry: JarEntry,
        expectedFingerprint: Fingerprint?
    ): Fingerprint = withContext(dispatcher) {
        val fingerprint = try {
            jarEntry
                .codeSigner
                .certificate
                .fingerprint()
        } catch (e: IllegalStateException) {
            invalid(e.message ?: "Unknown Exception")
        } catch (e: IllegalArgumentException) {
            invalid(e.message ?: "Error creating Fingerprint object")
        }
        if (expectedFingerprint == null) {
            fingerprint
        } else {
            if (expectedFingerprint.check(fingerprint)) {
                expectedFingerprint
            } else {
                invalid(
                    "Expected Fingerprint: ${expectedFingerprint}, " +
                        "Acquired Fingerprint: $fingerprint"
                )
            }
        }
    }
}
