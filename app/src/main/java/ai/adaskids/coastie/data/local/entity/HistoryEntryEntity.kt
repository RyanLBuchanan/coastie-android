package ai.adaskids.coastie.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "history_entries")
data class HistoryEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,

    // optional context
    val scenarioTitle: String? = null,

    // core fields
    val prompt: String,
    val response: String,
    val createdAtEpochMs: Long,

    // attachment metadata only (no bytes)
    val attachmentName: String? = null,
    val attachmentMimeType: String? = null,
    val attachmentSizeBytes: Long? = null
)
