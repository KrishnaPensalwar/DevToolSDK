package io.github.krishnapensalwar.devkit.network.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface NetworkDao {
    @Query("SELECT * FROM network_calls ORDER BY timestamp DESC")
    fun getAllCalls(): Flow<List<NetworkEntity>>

    @Insert
    suspend fun insert(call: NetworkEntity)

    @Query("DELETE FROM network_calls")
    suspend fun deleteAll()

    @Query("SELECT * FROM network_calls WHERE url LIKE '%' || :query || '%' OR requestBody LIKE '%' || :query || '%' OR responseBody LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun searchCalls(query: String): Flow<List<NetworkEntity>>
}