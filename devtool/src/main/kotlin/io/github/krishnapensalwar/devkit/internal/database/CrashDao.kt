package io.github.krishnapensalwar.devkit.internal.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
internal interface CrashDao {
    @Query("SELECT * FROM crashes WHERE level = 'CRASH' ORDER BY timestamp DESC")
    fun getAllCrashes(): Flow<List<CrashEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(crash: CrashEntity): Long

    @Query("DELETE FROM crashes")
    suspend fun clearAll()

    @Query("DELETE FROM crashes WHERE level != 'CRASH'")
    suspend fun deleteNonCrashes()
}
