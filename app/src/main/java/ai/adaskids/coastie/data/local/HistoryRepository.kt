package ai.adaskids.coastie.data.local

import ai.adaskids.coastie.data.local.dao.HistoryDao
import ai.adaskids.coastie.data.local.entity.HistoryEntryEntity
import kotlinx.coroutines.flow.Flow

class HistoryRepository(
    private val dao: HistoryDao
) {
    fun observeAll(): Flow<List<HistoryEntryEntity>> = dao.observeAll()

    suspend fun insert(entry: HistoryEntryEntity) {
        dao.insert(entry)
        // keep demo tidy
        dao.trimToMostRecent(keep = 100)
    }

    suspend fun clear() = dao.clearAll()

    suspend fun deleteById(id: Long) = dao.deleteById(id)
}
