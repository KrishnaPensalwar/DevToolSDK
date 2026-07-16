package com.example.devtool.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MockDao {
    @Query("SELECT * FROM mock_responses WHERE enabled = 1")
    fun getEnabledMocks(): Flow<List<MockEntity>>

    @Query("SELECT * FROM mock_responses WHERE url = :url AND method = :method LIMIT 1")
    suspend fun findMock(url: String, method: String): MockEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(mock: MockEntity): Long

    @Update
    suspend fun update(mock: MockEntity)

    @Query("DELETE FROM mock_responses WHERE id = :id")
    suspend fun deleteById(id: Long)
}
