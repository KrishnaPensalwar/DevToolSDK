package io.github.krishnapensalwar.devkit.mock

import android.app.Application
import android.content.Context
import io.github.krishnapensalwar.devkit.DevToolSdk
import io.github.krishnapensalwar.devkit.MockResponse
import io.github.krishnapensalwar.devkit.database.DevToolDatabase
import io.github.krishnapensalwar.devkit.database.MockDao
import io.github.krishnapensalwar.devkit.database.MockEntity
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.http.ContentType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

object MockManager {
    private lateinit var db: DevToolDatabase
    private var customResolver: ((HttpRequestBuilder) -> MockResponse?)? = null
    private var mockingEnabled: Boolean = true
    private val scope = CoroutineScope(Dispatchers.IO)

    fun init(database: DevToolDatabase, context: Context) {
        db = database
        // DevToolSdk.initialize(context as Application) // Circular dependency if called here
    }

    fun setMockingEnabled(enabled: Boolean) {
        mockingEnabled = enabled
    }

    fun isMockingEnabled(): Boolean = mockingEnabled

    fun setCustomResolver(resolver: (HttpRequestBuilder) -> MockResponse?) {
        customResolver = resolver
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