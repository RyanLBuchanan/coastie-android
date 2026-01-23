// file: data/local/entity/HistoryEntryEntity.kt
package com.coastal.coastie.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "history_entries")
data class HistoryEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val scenarioId: String,
    val scenarioTitle: String,
    val prompt: String,
    val response: String,
    val createdAtEpochMs: Long,

    // attachment metadata only (no bytes, no uri)
    val attachmentName: String? = null,
    val attachmentMime: String? = null,
    val attachmentSizeBytes: Long? = null
)
