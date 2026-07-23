package io.github.krishnapensalwar.devkit.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mock_responses")
data class MockEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val url: String,
    val method: String,
    val responseBody: String,
    val headers: String?,
    val enabled: Boolean = true
)