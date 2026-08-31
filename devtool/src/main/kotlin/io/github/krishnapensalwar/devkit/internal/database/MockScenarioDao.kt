package io.github.krishnapensalwar.devkit.internal.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
internal interface MockScenarioDao {
    @Query("SELECT * FROM mock_api_config WHERE url = :url AND method = :method LIMIT 1")
    suspend fun getConfig(url: String, method: String): MockApiConfigEntity?

    @Query("SELECT * FROM mock_api_config WHERE url = :url AND method = :method LIMIT 1")
    fun observeConfig(url: String, method: String): Flow<MockApiConfigEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertConfig(config: MockApiConfigEntity)

    @Query("SELECT * FROM mock_custom_scenarios WHERE url = :url AND method = :method ORDER BY id DESC")
    fun observeCustom(url: String, method: String): Flow<List<MockCustomScenarioEntity>>

    @Query("SELECT * FROM mock_custom_scenarios WHERE url = :url AND method = :method ORDER BY id DESC")
    suspend fun listCustom(url: String, method: String): List<MockCustomScenarioEntity>

    @Query("SELECT * FROM mock_custom_scenarios WHERE id = :id LIMIT 1")
    suspend fun getCustom(id: Long): MockCustomScenarioEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustom(entity: MockCustomScenarioEntity): Long

    @Update
    suspend fun updateCustom(entity: MockCustomScenarioEntity)

    @Query("DELETE FROM mock_custom_scenarios WHERE id = :id")
    suspend fun deleteCustom(id: Long)
}
