package cz.mroczis.netmonster.sample.storage

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Cell::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cellDao(): CellDao

    companion object {
        private var INSTANCE: AppDatabase? = null
        fun getDatabase(context: Context): AppDatabase {
            if (INSTANCE == null) {
                synchronized(this) {
                    INSTANCE =
                        Room.databaseBuilder(context, AppDatabase::class.java, "app-database")
                            .build()
                }
            }
            return INSTANCE!!
        }
    }
}