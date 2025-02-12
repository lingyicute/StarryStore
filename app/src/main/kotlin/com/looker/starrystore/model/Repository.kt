package com.looker.starrystore.model

import java.net.URL

data class Repository(
    var id: Long,
    val address: String,
    val mirrors: List<String>,
    val name: String,
    val description: String,
    val version: Int,
    val enabled: Boolean,
    val fingerprint: String,
    val lastModified: String,
    val entityTag: String,
    val updated: Long,
    val timestamp: Long,
    val authentication: String
) {

    fun edit(address: String, fingerprint: String, authentication: String): Repository {
        val isAddressChanged = this.address != address
        val isFingerprintChanged = this.fingerprint != fingerprint
        val shouldForceUpdate = isAddressChanged || isFingerprintChanged
        return copy(
            address = address,
            fingerprint = fingerprint,
            lastModified = if (shouldForceUpdate) "" else lastModified,
            entityTag = if (shouldForceUpdate) "" else entityTag,
            authentication = authentication
        )
    }

    fun update(
        mirrors: List<String>,
        name: String,
        description: String,
        version: Int,
        lastModified: String,
        entityTag: String,
        timestamp: Long
    ): Repository {
        return copy(
            mirrors = mirrors,
            name = name,
            description = description,
            version = if (version >= 0) version else this.version,
            lastModified = lastModified,
            entityTag = entityTag,
            updated = System.currentTimeMillis(),
            timestamp = timestamp
        )
    }

    fun enable(enabled: Boolean): Repository {
        return copy(enabled = enabled, lastModified = "", entityTag = "")
    }

    @Suppress("SpellCheckingInspection")
    companion object {

        fun newRepository(
            address: String,
            fingerprint: String,
            authentication: String
        ): Repository {
            val name = try {
                URL(address).let { "${it.host}${it.path}" }
            } catch (e: Exception) {
                address
            }
            return defaultRepository(address, name, "", 0, true, fingerprint, authentication)
        }

        private fun defaultRepository(
            address: String,
            name: String,
            description: String,
            version: Int = 21,
            enabled: Boolean = false,
            fingerprint: String,
            authentication: String = ""
        ): Repository {
            return Repository(
                -1, address, emptyList(), name, description, version, enabled,
                fingerprint, "", "", 0L, 0L, authentication
            )
        }

        val defaultRepositories = listOf(
            defaultRepository(
                address = "https://lingyicute.github.io/StarryStoreStatics/001/repo",
                name = "StarryStore Official",
                description = "lingyicute's official repository for all her free open source apps, also available on GitHub.",
                enabled = true,
                fingerprint = "E6FB98BF225A07BF155C46D924F374457DCC50277652D57E453D524E00658512"
            ),
        )

        val newlyAdded = listOf<Repository>(
        )
    }
}
