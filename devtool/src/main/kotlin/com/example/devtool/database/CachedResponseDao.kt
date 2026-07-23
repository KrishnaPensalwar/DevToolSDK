package com.example.devtool.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

/** DAO for cached network responses */
@Dao
interface CachedResponseDao {
    @Query("SELECT * FROM cached_responses WHERE url = :url AND method = :method LIMIT 1")
    suspend fun get(url: String, method: String): CachedResponseEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: CachedResponseEntity)

    @Query("SELECT * FROM cached_responses")
    suspend fun getAll(): List<CachedResponseEntity>

    @Query("DELETE FROM cached_responses")
    suspend fun clearAll()
}
