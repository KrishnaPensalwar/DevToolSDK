package io.github.krishnapensalwar.devkit.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [MockEntity::class, NetworkEntity::class, CachedResponseEntity::class], version = 2, exportSchema = false)
internal abstract class DevToolDatabase : RoomDatabase() {
    abstract fun mockDao(): MockDao
    abstract fun networkDao(): NetworkDao
    abstract fun cachedResponseDao(): CachedResponseDao
}