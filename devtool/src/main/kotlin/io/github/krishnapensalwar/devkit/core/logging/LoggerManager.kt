package io.github.krishnapensalwar.devkit.core.logging

import android.content.Context
import io.github.krishnapensalwar.devkit.data.database.LogDatabase
import io.github.krishnapensalwar.devkit.network.database.NetworkDatabase
import io.github.krishnapensalwar.devkit.network.repository.NetworkRepository

object LoggerManager {
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

    fun getNetworkRepository(): NetworkRepository {
        return networkRepository ?: throw IllegalStateException("LoggerManager not initialized")
    }
}