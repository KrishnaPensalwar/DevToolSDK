package io.github.krishnapensalwar.devkit.core.logging

import android.content.Context
import io.github.krishnapensalwar.devkit.internal.database.DevToolDatabase
import io.github.krishnapensalwar.devkit.network.repository.NetworkRepository

internal object LoggerManager {
    private var repository: LogRepository? = null
    private var networkRepository: NetworkRepository? = null

    fun init(context: Context) {
        val database = DevToolDatabase.getDatabase(context)
        if (repository == null) {
            repository = LogRepository(database.crashDao())
        }
        if (networkRepository == null) {
            networkRepository = NetworkRepository(database.networkDao())
        }
    }

    fun getRepository(): LogRepository {
        return repository ?: throw IllegalStateException("LoggerManager not initialized")
    }

    fun getNetworkRepository(): NetworkRepository {
        return networkRepository ?: throw IllegalStateException("LoggerManager not initialized")
    }
}
