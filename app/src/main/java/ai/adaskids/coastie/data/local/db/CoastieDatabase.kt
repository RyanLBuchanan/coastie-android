// file: data/local/db/CoastieDatabase.kt
package com.coastal.coastie.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.coastal.coastie.data.local.dao.HistoryDao
import com.coastal.coastie.data.local.entity.HistoryEntryEntity

@Database(
    entities = [HistoryEntryEntity::class],
    version = 1,
    exportSchema = false
)
abstract class CoastieDatabase : RoomDatabase() {
    abstract fun historyDao(): HistoryDao
}
