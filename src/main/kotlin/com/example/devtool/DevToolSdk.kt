package com.example.devtool

import android.app.Application
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.example.devtool.database.DevToolDatabase
import com.example.devtool.mock.MockManager
import io.ktor.client.HttpClient
import io.ktor.client.request.HttpRequestBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

private const val DATASTORE_NAME = "devtool_prefs"

private val Application.devToolDataStore by preferencesDataStore(
    name = DATASTORE_NAME
)

private val MOCKING_ENABLED_KEY = booleanPreferencesKey("mocking_enabled")

object DevToolSdk {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private var clientRef: HttpClient? = null
    private var appContext: Application? = null
    private var database: DevToolDatabase? = null

    fun register(client: HttpClient) {
        clientRef = client
    }

    private var currentConfig: DevToolConfig? = null

    internal fun bind(config: DevToolConfig, client: HttpClient) {
        // Store config for later state updates
        currentConfig = config
        // No‑op for backward compatibility
    }

    fun initialize(application: Application) {
        appContext = application

        database = Room.databaseBuilder(
            application,
            DevToolDatabase::class.java,
            "devtool-db"
        )
            .fallbackToDestructiveMigration()
            .build()

        MockManager.init(database!!, application)

        setMockingEnabled(true)
    }

    fun setMockingEnabled(enabled: Boolean) {
        appContext?.let { application ->
            scope.launch {
                application.devToolDataStore.edit { prefs ->
                    prefs[MOCKING_ENABLED_KEY] = enabled
                }
            }
        }

        MockManager.setMockingEnabled(enabled)
        // Update plugin config if bound
        currentConfig?.mockingEnabled = enabled
    }

    fun isMockingEnabled(): Boolean =
        MockManager.isMockingEnabled()

    fun setMockResolver(
        resolver: (HttpRequestBuilder) -> MockResponse?
    ) {
        MockManager.setCustomResolver(resolver)
        // Update plugin config if bound
        currentConfig?.mockResolver = resolver
    }

    fun clientOrNull(): HttpClient? = clientRef
}