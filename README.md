<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android-brightgreen?style=for-the-badge&logo=android" alt="Platform" />
  <img src="https://img.shields.io/badge/Language-Kotlin-purple?style=for-the-badge&logo=kotlin" alt="Language" />
  <img src="https://img.shields.io/badge/UI-Jetpack%20Compose-blue?style=for-the-badge&logo=jetpackcompose" alt="UI" />
  <img src="https://img.shields.io/badge/Min%20SDK-21-orange?style=for-the-badge" alt="Min SDK" />
  <img src="https://img.shields.io/badge/License-Apache%202.0-red?style=for-the-badge" alt="License" />
</p>

<h1 align="center">🛠️ DevKit SDK</h1>

<p align="center">
  <strong>A powerful, plug-and-play Android developer toolkit for debugging, network inspection, crash reporting, storage inspection, and API mocking — all from a beautiful in-app dashboard.</strong>
</p>

<p align="center">
  <a href="#-features">Features</a> •
  <a href="#-screenshots">Screenshots</a> •
  <a href="#-installation">Installation</a> •
  <a href="#-quick-start">Quick Start</a> •
  <a href="#-usage">Usage</a> •
  <a href="#-customization">Customization</a> •
  <a href="#-faq">FAQ</a> •
  <a href="#-troubleshooting">Troubleshooting</a> •
  <a href="#-changelog">Changelog</a> •
  <a href="#-license">License</a>
</p>

---

## 📖 What is DevKit?

**DevKit** is an Android SDK that gives developers a full-featured, in-app debugging dashboard — no external tools required. Drop it into any Android project and instantly get:

- 🔍 **Network Inspector** — Monitor every HTTP request and response in real-time
- 🐛 **Crash Reporter** — Capture uncaught exceptions with full stack traces
- 🗄️ **Storage Inspector** — Browse SharedPreferences key-value pairs live
- 📊 **Network Analytics** — Visualize API performance with endpoint-level stats
- 📱 **Device Info** — View device model, OS version, app version, and build type
- 🎭 **API Mocking** — Mock API responses from cache or custom rules, without restarting the app
- 💾 **Response Caching** — Automatically cache API responses and edit them on-device
- 🔘 **Floating Debug Button** — Draggable FAB that opens the dashboard from any screen

DevKit integrates with both **OkHttp** and **Ktor** HTTP clients and provides a gorgeous Material 3 Compose UI out of the box.

---

## ✨ Features

| Feature | Description |
|---|---|
| **Network Inspector** | Real-time list of all HTTP calls with method, status code, duration, request/response headers & body, cURL export |
| **Network Analytics** | Aggregated endpoint stats — total calls, avg/min/max latency, success rate, response size |
| **Crash Reporting** | Automatic capture of uncaught exceptions with thread name, message, and full stack trace |
| **Storage Inspector** | Browse and expand all SharedPreferences files with key-value pairs |
| **Device Info** | App version, package name, build type, manufacturer, model, Android version, SDK level |
| **API Mocking** | Toggle mocking on/off at runtime; serve responses from cache or custom mock resolvers |
| **Response Cache Editor** | View, edit, and save cached API response bodies directly on-device |
| **cURL Export** | Copy any network call as a ready-to-use cURL command |
| **Floating Debug Button** | Draggable FAB auto-injected into every Activity; opens the dashboard |
| **OkHttp Interceptor** | Drop-in `Interceptor` for OkHttp-based networking |
| **Ktor Plugin** | First-class Ktor `HttpClient` plugin with request/response hooks |
| **Material 3 UI** | Beautiful dark-themed dashboard built entirely with Jetpack Compose |
| **Sensitive Header Redaction** | Configurable set of header names to redact (e.g., `Cookie`, `X-Api-Key`) |

---

## Screenshots

View all DevTool SDK screenshots here:

[📸 View Screenshots](screenshots)
---

## 📦 Installation

### Gradle (Maven Central)

Add the dependency to your app module's `build.gradle.kts`:

```kotlin
dependencies {
    // Use debugImplementation so DevKit is stripped from release builds
    debugImplementation("io.github.krishnapensalwar:devkit:1.0.0")
}
```

Or if using Groovy (`build.gradle`):

```groovy
dependencies {
    debugImplementation 'io.github.krishnapensalwar:devkit:1.0.0'
}
```

Make sure `mavenCentral()` is in your repositories:

```kotlin
// settings.gradle.kts
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}
```

### Local attach (recommended — avoids AGP version clashes)

Do **not** import this repo as a Gradle module / `include(":devtool")` / `includeBuild(...)`.
That compiles DevKit with **its** Android Gradle Plugin next to **your app's** AGP and triggers:

`The Android Gradle plugin supports only one version at a time` / multiple AGP versions.

Publish the AAR to your machine, then depend on it like any other library:

```bash
# in DevToolSDK
./gradlew :devtool:publishToMavenLocal
```

```kotlin
// your app settings.gradle.kts
dependencyResolutionManagement {
    repositories {
        mavenLocal()
        google()
        mavenCentral()
    }
}
```

```kotlin
// your app module build.gradle.kts
debugImplementation("io.github.krishnapensalwar:devkit:1.0.0")
```

Your app keeps its own AGP / Gradle / Kotlin versions. Gradle will resolve library versions to the highest requested (yours or DevKit's).

### Requirements

### Consumer app (using the AAR)

| Requirement | Minimum | Notes |
| :--- | :--- | :--- |
| **JDK** | `17` | |
| **Gradle** | `8.2` | Gradle 7.x is not supported |
| **Android Gradle Plugin** | `8.2+` | Any AGP 8.2 or newer is fine — DevKit does not pin your AGP |
| **Compile SDK** | `34` | |
| **Min SDK** | `21` | |
| **Kotlin** | `1.9.24+` | |

### Critical libraries (transitive — Gradle may upgrade them)

| Library | DevKit uses | Purpose |
| :--- | :--- | :--- |
| **Jetpack Compose BOM** | `2024.06.00` | Dashboard UI |
| **Navigation Compose** | `2.7.7` | Dashboard navigation |
| **Room** | `2.6.1` | Network / crash / cache storage |
| **Ktor Client** | `3.0.3` | Ktor plugin API |
| **OkHttp** | `4.12.0` | OkHttp interceptor API |

---

## 🚀 Quick Start

### 1. Initialize the SDK

Call `DevTool.init()` in your `Application` class:

```kotlin
import io.github.krishnapensalwar.devkit.DevTool

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Initialize with default config
        DevTool.init(this)
    }
}
```

**That's it!** A floating debug button (🔘) will appear on every screen. Tap it to open the DevKit dashboard.

### 2. Add the Network Interceptor

#### For OkHttp

```kotlin
import io.github.krishnapensalwar.devkit.network.interceptor.DevToolNetworkInterceptor

val okHttpClient = OkHttpClient.Builder()
    .addInterceptor(DevToolNetworkInterceptor())
    .build()
```

#### For Ktor

```kotlin
import io.github.krishnapensalwar.devkit.withDevTool

val httpClient = HttpClient(Android) {
    withDevTool {
        // Optional: configure mocking
        mockingEnabled = true
    }
}
```

---

## 📖 Usage

### Network Inspection

Once the interceptor is installed, every HTTP call is automatically captured and displayed in the **Network** tab with:

- **Method & URL** with color-coded status badges
- **Duration** in milliseconds
- **Request Headers & Body** (with sensitive headers redacted)
- **Response Headers & Body** (formatted JSON)
- **cURL Export** — copy the request as a cURL command

### Crash Reporting

Crashes are automatically captured when `isCrashReportingEnabled` is `true` (default). View them in the **Crashes** tab with:

- Thread name where the crash occurred
- Exception message and full stack trace
- Timestamp

### API Mocking

DevKit supports powerful runtime API mocking:

```kotlin
// Enable mocking globally
DevToolSdk.setMockingEnabled(true)

// Disable mocking
DevToolSdk.setMockingEnabled(false)

// Check mocking status
val isEnabled = DevToolSdk.isMockingEnabled()
```

#### Ktor — Enable/Disable Mocking on an HttpClient

```kotlin
import io.github.krishnapensalwar.devkit.enableMocking
import io.github.krishnapensalwar.devkit.disableMocking

// Enable mocking with a custom resolver
httpClient.enableMocking { request ->
    if (request.url.encodedPath.contains("/users")) {
        MockResponse(body = """[{"id": 1, "name": "Mock User"}]""")
    } else null
}

// Disable mocking
httpClient.disableMocking()
```

#### Custom Mock Resolver

```kotlin
DevToolSdk.setMockResolver { request ->
    when {
        request.url.encodedPath.contains("/api/users") -> 
            MockResponse(
                status = HttpStatusCode.OK,
                body = """{"users": []}"""
            )
        else -> null  // Fall through to cache or real network
    }
}
```

#### How Mocking Resolution Works

When mocking is enabled, requests are resolved in this order:

1. **Custom Mock Resolver** — Your programmatic mock function
2. **Mock Database** — Mocks saved via the dashboard UI
3. **Cached Responses** — Previously cached real API responses
4. **Default Mock** — A generic JSON response (Ktor plugin only)

If no mock is found in OkHttp mode, the request proceeds to the real network.

### Response Cache Management

```kotlin
// Get all cached responses
val cached = DevToolSdk.getAllCachedResponses()

// Update a cached response body
DevToolSdk.updateCachedResponse(
    url = "https://api.example.com/users",
    method = "GET",
    newBody = """{"users": [{"id": 1, "name": "Updated"}]}"""
)

// Clear all cached responses
DevToolSdk.clearCache()
```

You can also view and edit cached responses directly from the dashboard's **Cache** screen with a built-in JSON editor.

### Storage Inspector

Browse all SharedPreferences files in the **Storage** tab. Each file is expandable, showing all key-value pairs with their types.

### Device Info

The **Device Info** tab shows:

- **App Information**: Version name, package name, build type (Debug/Release)
- **Device Details**: Manufacturer, model, Android version, SDK level

### Network Analytics

The **Analytics** tab provides aggregated stats per endpoint:

- Total call count
- Average / Min / Max response time
- Average response size
- Success rate percentage
- Last called timestamp

---

## 🎨 Customization

### DevtoolConfig

Pass a `DevtoolConfig` to `DevTool.init()` to toggle features:

```kotlin
import io.github.krishnapensalwar.devkit.core.DevtoolConfig

DevTool.init(
    context = this,
    config = DevtoolConfig(
        isCrashReportingEnabled = true,        // Capture uncaught exceptions
        isNetworkMonitoringEnabled = true,      // Enable network inspection & mocking
        isFloatingButtonEnabled = true,         // Show draggable FAB on every screen
        sensitiveHeaders = setOf(               // Headers to redact in the UI
            "Authorization",
            "Cookie",
            "X-Api-Key",
            "X-Auth-Token"
        )
    )
)
```

#### Config Options

| Option | Type | Default | Description |
|---|---|---|---|
| `isCrashReportingEnabled` | `Boolean` | `true` | Enable automatic crash capture |
| `isNetworkMonitoringEnabled` | `Boolean` | `true` | Enable network inspection, analytics, and mocking |
| `isFloatingButtonEnabled` | `Boolean` | `true` | Show the floating debug button overlay |
| `sensitiveHeaders` | `Set<String>` | `{"Cookie", "X-Api-Key"}` | Header names to redact in network detail views |

### Ktor DevToolPlugin Config

When using the Ktor plugin, you get additional hooks:

```kotlin
val client = HttpClient(Android) {
    withDevTool {
        mockingEnabled = true
        
        // Modify every outgoing request
        requestModifier = { request ->
            request.headers.append("X-Debug", "true")
        }
        
        // Observe every response
        responseObserver = { response ->
            Log.d("DevKit", "Response: ${response.status}")
        }
        
        // Record request-response pairs
        recorder = { request, response ->
            // Custom recording logic
        }
        
        // Custom mock resolver
        mockResolver = { request ->
            null // Return MockResponse or null
        }
    }
}
```

### Opening the Dashboard Programmatically

You can launch the dashboard without the floating button:

```kotlin
import io.github.krishnapensalwar.devkit.ui.dashboard.DashboardActivity

startActivity(DashboardActivity.newIntent(context))
```

---

## ❓ FAQ

<details>
<summary><strong>Can I use DevKit in production?</strong></summary>

We recommend using `debugImplementation` so DevKit is automatically excluded from release builds. This ensures zero overhead in production. If you need it in staging builds, use a custom build type.
</details>

<details>
<summary><strong>Does DevKit work with Retrofit?</strong></summary>

Yes! Retrofit uses OkHttp under the hood. Simply add the `DevToolNetworkInterceptor` to your OkHttp client, and Retrofit calls will be captured automatically.

```kotlin
val okHttpClient = OkHttpClient.Builder()
    .addInterceptor(DevToolNetworkInterceptor())
    .build()

val retrofit = Retrofit.Builder()
    .baseUrl("https://api.example.com/")
    .client(okHttpClient)
    .build()
```
</details>

<details>
<summary><strong>Can I use both OkHttp and Ktor interceptors?</strong></summary>

Yes. Each interceptor works independently. Network calls from both clients will appear in the same dashboard.
</details>

<details>
<summary><strong>Does the floating button appear on all Activities?</strong></summary>

Yes, when `isFloatingButtonEnabled` is `true`, the FAB is automatically injected into every `Activity` via `ActivityLifecycleCallbacks`. It is excluded from the DevKit dashboard activity itself to avoid recursion.
</details>

<details>
<summary><strong>How does response caching work?</strong></summary>

When mocking is enabled, successful network responses (OkHttp) are automatically cached in a local Room database. These cached responses can be served as mock responses for subsequent requests to the same endpoint, or edited via the dashboard's cache screen.
</details>

<details>
<summary><strong>What happens to the data when the app is killed?</strong></summary>

All data (network calls, crash logs, cached responses, mock configurations) is persisted in a local Room database. Data survives app restarts and is available the next time you open the dashboard.
</details>

<details>
<summary><strong>What's the minimum Android version?</strong></summary>

DevKit supports **API 21** (Android 5.0 Lollipop) and above.
</details>

---

## 🔧 Troubleshooting

### Multiple Android Gradle plugin versions / AGP clash

This happens if the DevKit **source** is added as a module (`include`, `includeBuild`, or Android Studio “Import Module”). The SDK and the app then each apply a different AGP.

Use a published AAR instead:

```bash
./gradlew :devtool:publishToMavenLocal
```

Then `mavenLocal()` + `debugImplementation("io.github.krishnapensalwar:devkit:1.0.0")` in the app. Do not include this repo in the app’s Gradle project.

### Floating button not appearing

- Ensure `isFloatingButtonEnabled` is set to `true` in your `DevtoolConfig`.
- Make sure `DevTool.init()` is called in your `Application.onCreate()`, **not** in an Activity.
- Check that your theme extends a MaterialComponents or Material3 theme.

### Network calls not showing up

- Verify that `DevToolNetworkInterceptor()` is added to your OkHttp client, or `withDevTool {}` is installed on your Ktor client.
- Make sure `isNetworkMonitoringEnabled` is `true` (default).
- Confirm `DevTool.init()` is called **before** any network requests are made.

### Crash reports not captured

- Ensure `isCrashReportingEnabled` is `true` (default).
- DevKit sets a custom `UncaughtExceptionHandler`. If another library also sets one, the last one wins. Initialize DevKit last, or after other crash reporters.

### Mock responses not being served

- Check that mocking is enabled: `DevToolSdk.isMockingEnabled()` should return `true`.
- For OkHttp: The interceptor checks the mock database and cache. Ensure the URL and HTTP method match exactly.
- For Ktor: The plugin checks your custom resolver first, then the cache, then falls back to a default mock.

### `IllegalStateException: LoggerManager not initialized`

- This means `DevTool.init()` was not called before accessing the logging system. Ensure initialization happens in `Application.onCreate()`.

### ProGuard / R8 issues

DevKit ships with consumer ProGuard rules. If you encounter issues, add:

```proguard
-keep class io.github.krishnapensalwar.devkit.** { *; }
-keep class io.github.krishnapensalwar.devkit.database.** { *; }
```

---

## 📋 Changelog

### v1.0.0 (Initial Release)

- ✅ Network Inspector with real-time HTTP call monitoring
- ✅ Network Analytics with per-endpoint aggregated stats
- ✅ Crash Reporting with automatic uncaught exception capture
- ✅ Storage Inspector for SharedPreferences
- ✅ Device Info panel
- ✅ API Mocking with runtime toggle (OkHttp + Ktor)
- ✅ Response cache with on-device JSON editor
- ✅ cURL command export
- ✅ Floating debug button with drag support
- ✅ OkHttp Interceptor support
- ✅ Ktor HttpClient Plugin support
- ✅ Material 3 dark-themed Compose dashboard
- ✅ Sensitive header redaction
- ✅ Room-based persistence for all data

---

## 🏗️ Architecture

```
io.github.krishnapensalwar.devkit
├── DevTool.kt                  # Main entry point — init()
├── DevToolSdk.kt               # Singleton SDK manager (mocking, cache APIs)
├── DevToolPlugin.kt            # Ktor client plugin + MockResponse model
├── DevToolExtensions.kt        # Extension functions (withDevTool, enableMocking)
├── DevToolOverviewScreen.kt    # Dashboard overview composable
│
├── core/
│   ├── DevtoolConfig.kt        # Configuration data class
│   ├── collector/
│   │   ├── CrashCollector.kt   # UncaughtExceptionHandler
│   │   └── PerformanceCollector.kt
│   └── logging/
│       ├── DevLog.kt           # Log data model
│       ├── LogLevel.kt         # VERBOSE, DEBUG, ... CRASH, NETWORK
│       ├── LogRepository.kt    # Room-backed log storage
│       └── LoggerManager.kt    # Logger + NetworkRepo initialization
│
├── network/
│   ├── interceptor/
│   │   └── DevToolNetworkInterceptor.kt  # OkHttp Interceptor
│   ├── model/                   # NetworkCall data model
│   ├── parser/                  # Request/response parsers
│   ├── repository/              # Network call Room repository
│   ├── database/                # Network Room database
│   ├── AnalyticsCalculator.kt   # Endpoint stats aggregation
│   └── CurlGenerator.kt        # cURL command generation
│
├── mock/
│   └── MockManager.kt          # Mock resolution (DB + cache)
│
├── cache/
│   └── CacheManager.kt         # Response caching logic
│
├── database/
│   ├── DevToolDatabase.kt      # Main Room database
│   ├── MockDao.kt / MockEntity.kt
│   ├── CachedResponseDao.kt / CachedResponseEntity.kt
│   └── NetworkDao.kt / NetworkEntity.kt
│
├── data/
│   └── database/                # Log database
│
└── ui/
    ├── floating/
    │   └── FloatingButtonManager.kt   # Auto-injected draggable FAB
    ├── dashboard/
    │   ├── DashboardActivity.kt       # Entry Activity
    │   ├── DashboardScreen.kt         # Navigation drawer + NavHost
    │   ├── DashboardHomeScreen.kt     # Home with mocking toggle & metrics
    │   ├── network/                   # Network list, detail, analytics, editor
    │   ├── crash/                     # Crash log viewer
    │   ├── device/                    # Device info screen
    │   └── storage/                   # SharedPreferences browser
    ├── navigation/
    │   └── Destinations.kt            # Type-safe navigation
    ├── components/                    # Reusable UI components
    └── theme/                         # Material 3 dark theme
```

---

## 🤝 Contributing

Contributions are welcome! Please:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

---

## 📄 License

```
Copyright 2026 Krishna Pensalwar

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

---

<p align="center">
  Made with ❤️ by <a href="https://github.com/KrishnaPensalwar">Krishna Pensalwar</a>
</p>
