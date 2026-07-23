package io.github.krishnapensalwar.devkit.core.logging

import io.github.krishnapensalwar.devkit.data.database.LogDao
import io.github.krishnapensalwar.devkit.data.database.LogEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class LogRepository(private val logDao: LogDao) {
    private val _logs = MutableStateFlow<List<DevLog>>(emptyList())
    val logs: StateFlow<List<DevLog>> = _logs.asStateFlow()

    private val repositoryScope = CoroutineScope(Dispatchers.IO)

    init {
        repositoryScope.launch {
            logDao.getAllLogs().collectLatest { entities ->
                _logs.value = entities.map { it.toDevLog() }
            }
        }
    }

    suspend fun add(log: DevLog) {
        logDao.insert(log.toEntity())
    }

    suspend fun clearAll() {
        logDao.clearAll()
    }

    private fun LogEntity.toDevLog() = DevLog(
        id = id,
        tag = tag,
        level = level,
        message = message,
        timestamp = timestamp
    )

    private fun DevLog.toEntity() = LogEntity(
        tag = tag,
        level = level,
        message = message,
        timestamp = timestamp
    )
}