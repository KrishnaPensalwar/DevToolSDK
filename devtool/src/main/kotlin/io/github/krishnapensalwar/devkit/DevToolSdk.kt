package io.github.krishnapensalwar.devkit

import android.app.Application
import android.content.Context
import androidx.room.Room
import io.github.krishnapensalwar.devkit.internal.database.CachedResponseEntity
import io.github.krishnapensalwar.devkit.internal.database.DevToolDatabase
import io.github.krishnapensalwar.devkit.mock.MockManager
import io.github.krishnapensalwar.devkit.mock.identity.RequestIdentity
import io.github.krishnapensalwar.devkit.mock.scenario.MockScenarioRepository
import io.ktor.client.HttpClient
import io.ktor.client.request.HttpRequestBuilder
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

private const val PREFS_NAME = "devtool_prefs"
private const val KEY_MOCKING_ENABLED = "mocking_enabled"

/**
 * Core manager object for DevTool SDK operations, database access, and mocking configuration.
 */
internal object DevToolSdk {

    private var clientRef: HttpClient? = null
    private var appContext: Application? = null
    private var database: DevToolDatabase? = null

    /**
     * Registers an [HttpClient] instance with the SDK.
     *
     * @param client The Ktor HttpClient instance.
     */
    fun register(client: HttpClient) {
        clientRef = client
    }

    private var currentConfig: KtorDevToolConfig? = null

    internal fun bind(config: KtorDevToolConfig, client: HttpClient) {
        // Store config for later state updates
        currentConfig = config
        // Sync config with current mocking state
        config.mockingEnabled = isMockingEnabled()
    }

    /**
     * Initializes internal database, mock manager, and cache manager.
     * Called automatically by [DevTool.init].
     *
     * @param application The Android Application instance.
     */
    internal fun initialize(application: Application) {
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
        io.github.krishnapensalwar.devkit.mock.scenario.MockScenarioRepository.init(database!!)

        val prefs = application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val isEnabled = prefs.getBoolean(KEY_MOCKING_ENABLED, false)
        setMockingEnabled(isEnabled && io.github.krishnapensalwar.devkit.mock.MockSafety.debugBuild)
    }

    /**
     * Enables or disables global network traffic mocking at runtime.
     *
     * @param enabled `true` to enable mocking; `false` to disable.
     */
    @OptIn(DelicateCoroutinesApi::class)
    fun setMockingEnabled(enabled: Boolean) {
        val safeEnabled = enabled && io.github.krishnapensalwar.devkit.mock.MockSafety.debugBuild
        appContext?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            ?.edit()
            ?.putBoolean(KEY_MOCKING_ENABLED, safeEnabled)
            ?.apply()

        MockManager.setMockingEnabled(safeEnabled)
        currentConfig?.mockingEnabled = safeEnabled
        if (!safeEnabled) {
            GlobalScope.launch(Dispatchers.IO) {
                MockScenarioRepository.clearAllActiveScenarios()
            }
        }
    }

    /**
     * Returns whether network mocking is currently enabled.
     *
     * @return `true` if mocking is enabled, `false` otherwise.
     */
    fun isMockingEnabled(): Boolean =
        MockManager.isMockingEnabled()

    val mockingEnabledFlow: StateFlow<Boolean>
        get() = MockManager.mockingEnabledFlow

    /**
     * Sets a custom mock resolver lambda for Ktor network requests.
     *
     * @param resolver Lambda returning a [MockResponse] for a request builder, or `null` to fallback.
     */
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
    /**
     * Retrieves all cached responses stored in the SDK database.
     *
     * @return List of [CachedResponseEntity] instances.
     */
    suspend fun getAllCachedResponses(): List<CachedResponseEntity> =
        database?.cachedResponseDao()?.getAll() ?: emptyList()

    /**
     * Updates the response body of a cached endpoint identified by URL and HTTP method.
     *
     * @param url The full URL string of the target request.
     * @param method The HTTP method (e.g. GET, POST).
     * @param newBody The updated JSON or text response body.
     * @param newStatus Optional updated HTTP status code. When null the existing status is kept.
     */
    suspend fun updateCachedResponse(
        url: String,
        method: String,
        newBody: String,
        newStatus: Int? = null,
        requestBody: String? = null,
        identityKey: String? = null
    ) = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        val dao = database?.cachedResponseDao() ?: return@withContext
        val parsed = RequestIdentity.parse(url, method, requestBody)
        val existing = identityKey?.takeIf { it.isNotBlank() }?.let { dao.getByIdentityKey(it) }
            ?: dao.getByIdentityKey(parsed.identityKey)
        val key = existing?.identityKey ?: parsed.identityKey
        val status = newStatus ?: existing?.status ?: 200
        val headersJson = existing?.headersJson ?: "{}"
        val displayName = existing?.displayName?.ifBlank { null } ?: parsed.displayName
        dao.insert(
            CachedResponseEntity(
                identityKey = key,
                url = existing?.url ?: parsed.url,
                method = existing?.method ?: parsed.method,
                status = status,
                headersJson = headersJson,
                body = newBody,
                displayName = displayName,
                protocol = existing?.protocol ?: parsed.protocol.name
            )
        )
        MockScenarioRepository.saveOverrideForKey(
            identityKey = key,
            url = existing?.url ?: parsed.url,
            method = existing?.method ?: parsed.method,
            status = status,
            headersJson = headersJson,
            body = newBody,
            displayName = displayName
        )
    }

    /**
     * Clears all cached network responses from the SDK database.
     */
    suspend fun clearCache() {
        database?.cachedResponseDao()?.clearAll()
    }

}