package io.github.krishnapensalwar.devkit.core.logging

import io.github.krishnapensalwar.devkit.internal.database.CrashDao
import io.github.krishnapensalwar.devkit.internal.database.CrashEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

internal class LogRepository(private val crashDao: CrashDao) {
    private val _logs = MutableStateFlow<List<DevLog>>(emptyList())
    val logs: StateFlow<List<DevLog>> = _logs.asStateFlow()

    private val repositoryScope = CoroutineScope(Dispatchers.IO)

    init {
        repositoryScope.launch {
            crashDao.deleteNonCrashes()
            crashDao.getAllCrashes().collectLatest { entities ->
                _logs.value = entities.map { it.toDevLog() }
            }
        }
    }

    suspend fun add(log: DevLog) {
        if (log.level != LogLevel.CRASH) return
        crashDao.insert(log.toEntity())
    }

    suspend fun clearAll() {
        crashDao.clearAll()
    }

    private fun CrashEntity.toDevLog() = DevLog(
        id = id,
        tag = tag,
        level = level,
        message = message,
        timestamp = timestamp
    )

    private fun DevLog.toEntity() = CrashEntity(
        tag = tag,
        level = level,
        message = message,
        timestamp = timestamp
    )
}