package io.github.krishnapensalwar.devkit.internal.database

import androidx.room.Entity

@Entity(tableName = "mock_api_config", primaryKeys = ["url", "method"])
internal data class MockApiConfigEntity(
    val url: String,
    val method: String,
    val enabled: Boolean = true,
    val activeScenarioKey: String? = null,
    val slowDelayMs: Long = 1_000L
)
