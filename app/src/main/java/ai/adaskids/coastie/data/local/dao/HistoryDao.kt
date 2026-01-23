// file: data/local/dao/HistoryDao.kt
package com.coastal.coastie.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.coastal.coastie.data.local.entity.HistoryEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {
    @Insert
    suspend fun insert(entry: HistoryEntryEntity)

    @Query("SELECT * FROM history_entries ORDER BY createdAtEpochMs DESC")
    fun observeAll(): Flow<List<HistoryEntryEntity>>

    @Query("DELETE FROM history_entries")
    suspend fun clearAll()
}
