package io.github.krishnapensalwar.devkit.internal.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mock_responses")
internal data class MockEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val url: String,
    val method: String,
    val responseBody: String,
    val headers: String? = null,
    val enabled: Boolean = true
)
