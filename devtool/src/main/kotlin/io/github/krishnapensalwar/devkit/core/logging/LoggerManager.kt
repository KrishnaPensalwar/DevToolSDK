package io.github.krishnapensalwar.devkit.core.logging

import android.content.Context
import io.github.krishnapensalwar.devkit.internal.database.LogDatabase
import io.github.krishnapensalwar.devkit.internal.database.NetworkDatabase
import io.github.krishnapensalwar.devkit.network.repository.NetworkRepository

internal object LoggerManager {
    private var repository: LogRepository? = null
    private var networkRepository: NetworkRepository? = null

    fun init(context: Context) {
        if (repository == null) {
            val database = LogDatabase.getDatabase(context)
            repository = LogRepository(database.logDao())
        }
        if (networkRepository == null) {
            val networkDb = NetworkDatabase.getDatabase(context)
            networkRepository = NetworkRepository(networkDb.networkDao())
        }
    }

    fun getRepository(): LogRepository {
        return repository ?: throw IllegalStateException("LoggerManager not initialized")
    }

    suspend fun clearCrashes() {
        repository?.clearAll()
    }

    fun getNetworkRepository(): NetworkRepository {
        return networkRepository ?: throw IllegalStateException("LoggerManager not initialized")
    }
}
