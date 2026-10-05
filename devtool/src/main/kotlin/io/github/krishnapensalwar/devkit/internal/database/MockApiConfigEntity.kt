package io.github.krishnapensalwar.devkit.internal.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mock_api_config")
internal data class MockApiConfigEntity(
    @PrimaryKey val identityKey: String,
    val url: String,
    val method: String,
    val protocol: String = "REST",
    val gqlOperationType: String? = null,
    val gqlOperationName: String? = null,
    val enabled: Boolean = true,
    val activeScenarioKey: String? = null,
    val slowDelayMs: Long = 1_000L,
    val snapshotBody: String? = null,
    val snapshotStatus: Int = 200,
    val snapshotHeadersJson: String? = null,
    val useCachedBody: Boolean = true
)
