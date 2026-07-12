package com.example.devtool.core.logging

import android.content.Context
import com.example.devtool.data.database.LogDatabase

object LoggerManager {
    private var repository: LogRepository? = null

    fun init(context: Context) {
        if (repository == null) {
            val database = LogDatabase.getDatabase(context)
            repository = LogRepository(database.logDao())
        }
    }

    fun getRepository(): LogRepository {
        return repository ?: throw IllegalStateException("LoggerManager not initialized")
    }
}
