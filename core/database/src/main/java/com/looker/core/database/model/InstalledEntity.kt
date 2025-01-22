package com.looker.core.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "installed")
data class InstalledEntity(
    val packageName: String,
    val versionCode: Long,
    val signature: String,
    @PrimaryKey(autoGenerate = true)
    val id: Long = -1L,
)
