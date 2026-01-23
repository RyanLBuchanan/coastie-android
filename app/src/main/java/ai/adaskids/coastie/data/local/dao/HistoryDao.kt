package ai.adaskids.coastie.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ai.adaskids.coastie.data.local.entity.HistoryEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {

    @Query("SELECT * FROM history_entries ORDER BY createdAtEpochMs DESC")
    fun observeAll(): Flow<List<HistoryEntryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: HistoryEntryEntity)

    @Query("DELETE FROM history_entries")
    suspend fun clearAll()

    @Query("DELETE FROM history_entries WHERE id = :id")
    suspend fun deleteById(id: Long)

    // Optional: cap size for demo cleanliness (keep most recent 100)
    @Query("""
        DELETE FROM history_entries
        WHERE id NOT IN (
            SELECT id FROM history_entries ORDER BY createdAtEpochMs DESC LIMIT :keep
        )
    """)
    suspend fun trimToMostRecent(keep: Int)
}
