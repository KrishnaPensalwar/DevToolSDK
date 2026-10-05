package io.github.krishnapensalwar.devkit.internal.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
internal interface MockScenarioDao {
    @Query("SELECT * FROM mock_api_config WHERE identityKey = :identityKey LIMIT 1")
    suspend fun getConfig(identityKey: String): MockApiConfigEntity?

    @Query("SELECT * FROM mock_api_config WHERE identityKey = :identityKey LIMIT 1")
    fun observeConfig(identityKey: String): Flow<MockApiConfigEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertConfig(config: MockApiConfigEntity)

    @Query("UPDATE mock_api_config SET activeScenarioKey = NULL")
    suspend fun clearAllActiveScenarios()

    @Query("SELECT * FROM mock_custom_scenarios WHERE identityKey = :identityKey ORDER BY id DESC")
    fun observeCustom(identityKey: String): Flow<List<MockCustomScenarioEntity>>

    @Query("SELECT * FROM mock_custom_scenarios WHERE identityKey = :identityKey ORDER BY id DESC")
    suspend fun listCustom(identityKey: String): List<MockCustomScenarioEntity>

    @Query("SELECT * FROM mock_custom_scenarios WHERE id = :id LIMIT 1")
    suspend fun getCustom(id: Long): MockCustomScenarioEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustom(entity: MockCustomScenarioEntity): Long

    @Update
    suspend fun updateCustom(entity: MockCustomScenarioEntity)

    @Query("DELETE FROM mock_custom_scenarios WHERE id = :id")
    suspend fun deleteCustom(id: Long)
}
