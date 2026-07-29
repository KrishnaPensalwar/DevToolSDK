package io.github.krishnapensalwar.devkit.internal.database

import androidx.annotation.RestrictTo
import androidx.room.Database
import androidx.room.RoomDatabase

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
@Database(entities = [MockEntity::class, NetworkEntity::class, CachedResponseEntity::class], version = 2, exportSchema = false)
internal abstract class DevToolDatabase : RoomDatabase() {
    abstract fun mockDao(): MockDao
    abstract fun networkDao(): NetworkDao
    abstract fun cachedResponseDao(): CachedResponseDao
}
