package io.github.krishnapensalwar.devkit.internal.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
internal interface LogDao {
    @Query("SELECT * FROM logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<LogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: LogEntity): Long

    @Query("DELETE FROM logs")
    suspend fun clearAll()
}
