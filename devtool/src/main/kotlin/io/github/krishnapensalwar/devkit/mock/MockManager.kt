package io.github.krishnapensalwar.devkit.mock

import android.app.Application
import android.content.Context
import io.github.krishnapensalwar.devkit.DevToolSdk
import io.github.krishnapensalwar.devkit.MockResponse
import io.github.krishnapensalwar.devkit.internal.database.DevToolDatabase
import io.github.krishnapensalwar.devkit.internal.database.MockDao
import io.github.krishnapensalwar.devkit.internal.database.MockEntity
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.http.ContentType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.Request
import okhttp3.Response
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import okhttp3.Protocol
import org.json.JSONObject

internal object MockManager {
    private lateinit var db: DevToolDatabase
    private var customResolver: ((HttpRequestBuilder) -> MockResponse?)? = null
    private var mockingEnabled: Boolean = false
    private val scope = CoroutineScope(Dispatchers.IO)

    fun init(database: DevToolDatabase, context: Context) {
        db = database
        // DevToolSdk.initialize(context as Application) // Circular dependency if called here
    }

    fun getDbOrNull(): DevToolDatabase? {
        return if (::db.isInitialized) db else null
    }

    fun setMockingEnabled(enabled: Boolean) {
        mockingEnabled = enabled
    }

    fun isMockingEnabled(): Boolean = mockingEnabled

    fun setCustomResolver(resolver: (HttpRequestBuilder) -> MockResponse?) {
        customResolver = resolver
    }

    /** Resolve a mock response for the given OkHttp request. */
    fun resolveOkHttp(request: Request): Response? {
        if (!mockingEnabled) return null
        val database = getDbOrNull() ?: return null

        val url = request.url.toString()
        val method = request.method

        return runBlocking(Dispatchers.IO) {
            // 1. Try finding in mock_responses database table
            val mockEntity = database.mockDao().findMock(url, method)
            if (mockEntity != null && mockEntity.enabled) {
                val responseBodyString = mockEntity.responseBody
                val builder = Response.Builder()
                    .request(request)
                    .protocol(Protocol.HTTP_1_1)
                    .code(200) // Default status code for mock responses
                    .message("Mocked OK")
                    .body(responseBodyString.toResponseBody("application/json".toMediaTypeOrNull()))
                    .addHeader("X-Mock-Source", "MockEntity")
                
                mockEntity.headers?.let { headersJson ->
                    try {
                        val jsonObject = JSONObject(headersJson)
                        jsonObject.keys().forEach { key ->
                            builder.addHeader(key, jsonObject.getString(key))
                        }
                    } catch (e: Exception) {
                        // Ignore header parsing errors
                    }
                }
                if (mockEntity.headers.isNullOrEmpty()) {
                    builder.addHeader("Content-Type", "application/json")
                }
                return@runBlocking builder.build()
            }
            
            // 2. Try finding in cached_responses database table (auto-cache mocking)
            val cachedEntity = database.cachedResponseDao().get(url, method)
            if (cachedEntity != null) {
                val builder = Response.Builder()
                    .request(request)
                    .protocol(Protocol.HTTP_1_1)
                    .code(cachedEntity.status)
                    .message("Cached Response")
                    .body(cachedEntity.body.toResponseBody("application/json".toMediaTypeOrNull()))
                    .addHeader("X-Mock-Source", "CacheManager")
                
                try {
                    val json = JSONObject(cachedEntity.headersJson)
                    json.keys().forEach { key ->
                        val value = json.getString(key)
                        value.split(",").forEach { v ->
                            builder.addHeader(key, v)
                        }
                    }
                } catch (e: Exception) {
                    builder.addHeader("Content-Type", "application/json")
                }
                return@runBlocking builder.build()
            }
            
            null
        }
    }

    /** Resolve a mock response for the given request. */
//    suspend fun resolve(request: HttpRequestBuilder): MockResponse? {
//        if (!mockingEnabled) return null
//
//        customResolver?.invoke(request)?.let { return it }
//
//        val url = request.url.buildString()
//        val method = request.method.value
//
//        val entity = db.mockDao().findMock(url, method).first()
//
//        return entity?.let {
//            MockResponse(body = it.responseBody)
//        }
//    }
}