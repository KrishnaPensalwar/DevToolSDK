package io.github.krishnapensalwar.devkit.network.database

import android.content.Context
import androidx.annotation.RestrictTo
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
@Database(entities = [NetworkEntity::class], version = 1, exportSchema = false)
internal abstract class NetworkDatabase : RoomDatabase() {
    abstract fun networkDao(): NetworkDao

    companion object {
        @Volatile
        private var INSTANCE: NetworkDatabase? = null

        fun getDatabase(context: Context): NetworkDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NetworkDatabase::class.java,
                    "devtool_network_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}