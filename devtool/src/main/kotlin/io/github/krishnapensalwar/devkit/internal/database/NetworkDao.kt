package io.github.krishnapensalwar.devkit.internal.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
internal interface NetworkDao {
    @Query("SELECT * FROM network_calls ORDER BY timestamp DESC")
    fun getAllCalls(): Flow<List<NetworkEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(call: NetworkEntity): Long

    @Query("DELETE FROM network_calls")
    suspend fun deleteAll()

    @Query("SELECT * FROM network_calls WHERE id = :id")
    suspend fun getCallById(id: Long): NetworkEntity?
}
