package io.github.krishnapensalwar.devkit.internal.database

import androidx.room.*

@Dao
internal interface CachedResponseDao {
    @Query("SELECT * FROM cached_responses WHERE url = :url AND method = :method LIMIT 1")
    suspend fun get(url: String, method: String): CachedResponseEntity?

    @Query("SELECT * FROM cached_responses")
    suspend fun getAll(): List<CachedResponseEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: CachedResponseEntity)

    @Query("DELETE FROM cached_responses")
    suspend fun clearAll()
}
