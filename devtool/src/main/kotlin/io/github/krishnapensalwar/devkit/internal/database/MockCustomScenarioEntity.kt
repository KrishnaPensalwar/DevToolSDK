package io.github.krishnapensalwar.devkit.internal.database

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "mock_custom_scenarios",
    indices = [Index(value = ["identityKey"])]
)
internal data class MockCustomScenarioEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val identityKey: String,
    val url: String,
    val method: String,
    val name: String,
    val description: String = "",
    val statusCode: Int = 200,
    val body: String = "",
    val headersJson: String = "{}",
    val delayMs: Long = 0
)
