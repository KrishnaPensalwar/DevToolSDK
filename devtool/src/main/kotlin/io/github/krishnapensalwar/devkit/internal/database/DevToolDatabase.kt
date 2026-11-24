package io.github.krishnapensalwar.devkit.internal.database

import android.content.Context
import androidx.annotation.RestrictTo
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import io.github.krishnapensalwar.devkit.network.database.NetworkDao

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
@Database(
    entities = [
        NetworkEntity::class,
        CachedResponseEntity::class,
        MockApiConfigEntity::class,
        MockCustomScenarioEntity::class,
        CrashEntity::class
    ],
    version = 8,
    exportSchema = false
)
@TypeConverters(Converters::class)
internal abstract class DevToolDatabase : RoomDatabase() {
    abstract fun networkDao(): NetworkDao
    abstract fun cachedResponseDao(): CachedResponseDao
    abstract fun mockScenarioDao(): MockScenarioDao
    abstract fun crashDao(): CrashDao

    companion object {
        @Volatile
        private var INSTANCE: DevToolDatabase? = null

        fun getDatabase(context: Context): DevToolDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DevToolDatabase::class.java,
                    "devtool-db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
