package ai.adaskids.coastie.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import ai.adaskids.coastie.data.local.dao.HistoryDao
import ai.adaskids.coastie.data.local.entity.HistoryEntryEntity

@Database(
    entities = [HistoryEntryEntity::class],
    version = 1,
    exportSchema = false
)
abstract class CoastieDatabase : RoomDatabase() {
    abstract fun historyDao(): HistoryDao

    companion object {
        @Volatile private var INSTANCE: CoastieDatabase? = null

        fun get(context: Context): CoastieDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CoastieDatabase::class.java,
                    "coastie_demo.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
