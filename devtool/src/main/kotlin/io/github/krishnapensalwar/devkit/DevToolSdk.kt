package io.github.krishnapensalwar.devkit

import android.app.Application
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import io.github.krishnapensalwar.devkit.database.DevToolDatabase
import io.github.krishnapensalwar.devkit.mock.MockManager
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

        // Initialize mock manager and cache manager
        MockManager.init(database!!, application)
        io.github.krishnapensalwar.devkit.cache.CacheManager.init(database!!)

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

    // ---------------------------------------------------------------------
    // Cache inspection and manipulation API (available when mocking is enabled)
    // ---------------------------------------------------------------------
    /** Retrieve all cached responses. */
    suspend fun getAllCachedResponses(): List<io.github.krishnapensalwar.devkit.database.CachedResponseEntity> =
        database?.cachedResponseDao()?.getAll() ?: emptyList()

    /** Update the body of a cached response identified by URL and method. */
    suspend fun updateCachedResponse(
        url: String,
        method: String,
        newBody: String
    ) {
        val dao = database?.cachedResponseDao() ?: return
        val existing = dao.get(url, method) ?: return
        val updated = existing.copy(body = newBody)
        dao.insert(updated)
    }

    /** Clear all cached responses. */
    suspend fun clearCache() {
        database?.cachedResponseDao()?.clearAll()
    }

}