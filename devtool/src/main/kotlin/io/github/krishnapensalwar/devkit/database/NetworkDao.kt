package io.github.krishnapensalwar.devkit.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface NetworkDao {
    @Query("SELECT * FROM network_logs ORDER BY timestamp DESC")
    fun getAllNetworkLogs(): Flow<List<NetworkEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(network: NetworkEntity): Long

    @Query("DELETE FROM network_logs WHERE id = :id")
    suspend fun deleteById(id: Long)
}