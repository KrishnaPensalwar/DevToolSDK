# DevTool SDK — API Mock Scenarios

This document describes the scenario-based network mocking system: which files exist, what the main functions do, where state is stored, and what happens for each developer action.

App code does not need extra APIs beyond existing DevTool setup (`DevTool.init` + `DevToolNetworkInterceptor` on OkHttp, and/or `DevToolPlugin` on Ktor).

---

## 1. What the feature is

For each intercepted API (`url` + HTTP `method`), a developer can pick **one active scenario**. When **global mock** is ON and **API-level mock** is ON, the next request to that endpoint is short-circuited (no real network) and served according to that scenario.

Built-in scenarios live in code (not in the database). Custom scenarios and “which one is active” persist in Room across app restarts.

Typical path:

```text
DevTool dashboard → Network → tap a call → Scenarios → pick a scenario
→ app fires the same API again → interceptor returns the mock
```

---

## 2. Storage

Database file: **`devtool-db`** (`DevToolDatabase`, version **3**). Destructive migration is enabled (`fallbackToDestructiveMigration()`).

### 2.1 SharedPreferences — global mock switch

| Location | Keys |
|----------|------|
| `devtool_prefs` | `mocking_enabled` (`Boolean`) |

Written by `DevToolSdk.setMockingEnabled()`. Read on `DevToolSdk.initialize()`.

Even if prefs say `true`, mocking is **forced off** when the host app is not `FLAG_DEBUGGABLE` (`MockSafety`).

### 2.2 Table `mock_api_config` — per-API state

Entity: `MockApiConfigEntity`. Primary key: `(url, method)`.

| Column | Meaning |
|--------|---------|
| `url` | Full request URL string (same as OkHttp `request.url.toString()`) |
| `method` | HTTP method (`GET`, `POST`, …) |
| `enabled` | API-level mock ON/OFF. If `false`, this URL hits the live server even when global mock is ON |
| `activeScenarioKey` | Which scenario is active, or `null` |
| `slowDelayMs` | Delay used when `SLOW_RESPONSE` is active (default `1000`) |

**Scenario keys**

- Built-in: enum name, e.g. `SUCCESS`, `HTTP_500`, `TIMEOUT`, `SLOW_RESPONSE`
- Custom: `custom:<roomId>` e.g. `custom:7`
- Legacy fallback (not stored here): engine may still serve `legacy` / `cache` if no key is set

Only **one** `activeScenarioKey` per `(url, method)`. Tapping the same scenario again sets the key to `null` (deactivate).

### 2.3 Table `mock_custom_scenarios` — developer-defined scenarios

Entity: `MockCustomScenarioEntity`. Auto-id primary key. Indexed on `(url, method)`.

| Column | Meaning |
|--------|---------|
| `id` | Room id → key `custom:{id}` |
| `url`, `method` | Endpoint this custom belongs to |
| `name`, `description` | UI labels |
| `statusCode` | HTTP status to return |
| `body` | Response body string |
| `headersJson` | JSON object of header name → value |
| `delayMs` | Sleep before returning |

Built-ins **cannot** be deleted (they are not rows). Delete only hits this table.

### 2.4 Existing tables still used as **captured body** source

| Table | Role for scenarios |
|-------|-------------------|
| `cached_responses` | Last real/saved body + status + headers. First choice for Success / Empty / Slow |
| `mock_responses` | Older “saved mock” rows. Second choice if cache miss |
| `network_calls` | Traffic log for the Network UI (not the mock source of truth) |

Opening the Scenarios screen also **writes the current call’s body** into `cached_responses` so Success/Empty/Slow have a snapshot even if cache was empty.

---

## 3. File map and major functions

### 3.1 Domain / engine (no Android UI)

| File | Responsibility |
|------|----------------|
| `mock/scenario/MockScenarioModels.kt` | Types: `MockScenarioType`, `MockScenarioGroup`, `MockFailureKind`, `MockPlan`, `CustomScenarioData`, `CapturedResponse`. Helpers `customKey()`, `parseCustomId()` |
| `mock/scenario/MockScenarioCatalog.kt` | Static list of built-ins, group labels, delay presets (`500, 1000, 2000, 3000, 5000` ms), HTTP status labels |
| `mock/scenario/MockScenarioEngine.kt` | Turns a **key + captured body + delay + custom row** into a `MockPlan`. Pure logic, unit-tested |

**`MockScenarioEngine.resolve(key, captured, slowDelayMs, custom)`**  
If `key` is `custom:N`, uses `custom`. Else `MockScenarioType.valueOf(key)`. Unknown key → `null`.

**`MockScenarioEngine.resolveType(...)`** — mapping:

| Type | `MockPlan` result |
|------|-------------------|
| `SUCCESS` | 200, captured body (or `"{}"`) |
| `EMPTY_RESPONSE` | 200, `[]` if captured body starts with `[`, else `{}` |
| `EMPTY_BODY` | 200, blank body |
| `HTTP_400` … `HTTP_429`, `HTTP_500` … `HTTP_504` | That status + JSON `{"error":"<label>","status":N,"mocked":true}` |
| `TIMEOUT` / `NO_INTERNET` / `CONNECTION_FAILURE` | `statusCode = 0`, `failure` set (not an HTTP response) |
| `SLOW_RESPONSE` | Same as success body, `delayMs = slowDelayMs` |
| `CUSTOM` | Name, status, body, headers, delay from custom row |

**`activateExclusive(currentKey, selectedKey)`**  
Same key twice → `null` (off). Different key → selected key (only one active).

### 3.2 Persistence

| File | Responsibility |
|------|----------------|
| `internal/database/MockApiConfigEntity.kt` | Room row for per-API config |
| `internal/database/MockCustomScenarioEntity.kt` | Room row for custom scenarios |
| `internal/database/MockScenarioDao.kt` | Queries: get/observe/upsert config; list/insert/update/delete custom |
| `internal/database/DevToolDatabase.kt` | Registers new entities, `mockScenarioDao()`, version 3 |
| `mock/scenario/MockScenarioRepository.kt` | App-facing persist API used by UI and `MockManager` |

**Repository highlights**

- `observe(url, method)` — Flow of `MockApiUiState` (config + custom list) for Compose
- `setApiEnabled` — API-level switch
- `activateScenario` — exclusive toggle + forces `enabled = true`
- `setSlowDelay` — writes `slowDelayMs`
- `insertCustom` / `updateCustom` / `deleteCustom` — custom CRUD; delete clears active key if it pointed at that custom

### 3.3 Runtime mock pipeline

| File | Responsibility |
|------|----------------|
| `mock/MockSafety.kt` | `debugBuild` from `ApplicationInfo.FLAG_DEBUGGABLE`. `allowMocking = debug && global` |
| `mock/MockManager.kt` | Init DB + safety + repository. `decide(url, method)` → `MockDecision`. Builds OkHttp `Response` or `IOException` |
| `network/interceptor/DevToolNetworkInterceptor.kt` | OkHttp: sleep, return mock, or throw; log `NetworkCall` |
| `DevToolPlugin.kt` | Ktor `on(Send)`: same `decide()`, `delay()`, throw or `MockResponse` |
| `DevToolSdk.kt` | `initialize()`, persist global flag, init managers |
| `cache/CacheManager.kt` | Read/write `cached_responses` |

**`MockManager.decide(url, method)` order**

1. Global mock off or not debug → `PassThrough` (real network)
2. Config exists and `enabled == false` → `PassThrough`
3. `activeScenarioKey` set → `MockScenarioEngine.resolve` → `Serve(plan)`
4. Else enabled `mock_responses` row → `Serve` legacy 200
5. Else `cached_responses` row → `Serve` cached status/body
6. Else `Missing`

**`MockPlan.toOkHttp`** adds:

- `X-Mock-Source: Scenario`
- `X-Mock-Scenario: <display name>`
- `X-Mock-Key: <key>`

Network list/detail use these headers for the **MOCKED** badge.

**`MockPlan.toException`**

| Failure | Exception |
|---------|-----------|
| TIMEOUT | `SocketTimeoutException` |
| NO_INTERNET | `UnknownHostException` |
| CONNECTION_FAILURE | `ConnectException` |

### 3.4 UI

| File | Responsibility |
|------|----------------|
| `ui/dashboard/network/NetworkListScreen.kt` | List traffic; **MOCK** chip if mock headers present |
| `ui/dashboard/network/NetworkDetailScreen.kt` | Banner: MOCKED / scenario / status. **Scenarios** + **Override** |
| `ui/dashboard/network/MockScenarioScreen.kt` | Grouped scenario list, API toggle, slow delay chips, custom create/edit/delete |
| `ui/navigation/Destinations.kt` | `Destination.MockScenarios` + `navigate("mock_scenarios")` |
| `ui/dashboard/DashboardNavGraph.kt` | Composable route reads savedStateHandle (`mock_url`, `mock_method`, …) |
| `DevToolOverviewScreen.kt` | Global “Mock Network Traffic” switch |

### 3.5 Tests

| File | Covers |
|------|--------|
| `MockScenarioEngineTest.kt` | Success/empty/4xx/5xx/network/slow/custom/exclusive toggle |
| `MockScenarioCatalogTest.kt` | Built-in codes and keys |
| `MockFailureMappingTest.kt` | Exception types |

---

## 4. End-to-end data flow

### 4.1 SDK start

```text
DevTool.init(context)
  → DevToolSdk.initialize(application)
      → Room "devtool-db"
      → MockManager.init → MockSafety.init + MockScenarioRepository.init
      → CacheManager.init
      → read prefs mocking_enabled
      → setMockingEnabled(prefs && debugBuild)
```

### 4.2 Developer selects a scenario (UI)

```text
NetworkDetailScreen "Scenarios"
  → navigateTo(MockScenarios(url, method, body, status, headersJson))
  → MockScenarioScreen LaunchedEffect
      → CacheManager.saveWithHeadersJson(...)   // snapshot into cached_responses
  → tap row "500 Internal Server Error"
      → MockScenarioRepository.activateScenario(url, method, "HTTP_500")
          → upsert mock_api_config:
              enabled = true
              activeScenarioKey = "HTTP_500"   (or null if it was already HTTP_500)
```

No HTTP happens yet. Next **app** request to that exact `url`+`method` is mocked.

### 4.3 App makes a request (OkHttp)

```text
OkHttp → DevToolNetworkInterceptor.intercept
  → if MockManager.isMockingEnabled()
       MockManager.resolve(request)
         → decide(url, method)
         → engine builds MockPlan
         → Http: Thread.sleep(delayMs); return synthetic Response
         → Failure: persist NetworkCall with exception; throw IOException
         → PassThrough: chain.proceed (live)
         → Missing: synthetic 404 JSON + X-Mock-Source: Missing
  → parse + LoggerManager.addCall (network_calls)
  → return to app
```

Ktor path is the same decision, then `kotlinx.coroutines.delay` and either throw or `buildMockCall`.

### 4.4 Decision flowchart

```text
                    ┌─ not debug OR global OFF ──────────────► live network
Request ── decide ──┤
                    ├─ API enabled == false ─────────────────► live network
                    ├─ activeScenarioKey set ────────────────► Serve that scenario
                    ├─ mock_responses enabled row ───────────► Serve legacy 200
                    ├─ cached_responses row ─────────────────► Serve cache
                    └─ else ─────────────────────────────────► Missing (404 mock)
```

---

## 5. Action catalog — what happens, where data goes

### 5.1 Turn global mock ON

**UI:** Home / Overview → “Mock Network Traffic”.

**Writes:** `devtool_prefs.mocking_enabled = true` (only if debug). Memory: `MockManager.mockingEnabled`.

**Next requests:** `decide()` runs. Without a scenario/cache/legacy mock, **every** unmatched URL gets the Missing 404 JSON (legacy behavior).

### 5.2 Turn global mock OFF

Prefs `false`. `decide()` always `PassThrough`. Scenarios stay in Room; they apply again when global is ON.

### 5.3 Turn API-level mock OFF (on Scenarios screen)

**Writes:** `mock_api_config.enabled = false` for that url+method.

**Next requests:** `PassThrough` even if global is ON and a scenario is selected. Key is kept so turning the switch back on restores the same scenario.

### 5.4 Select a built-in from the list

**Example:** tap `500 Internal Server Error` for `GET https://api.example.com/v1/payment`.

**Writes:** one row in `mock_api_config`:

```text
url = https://api.example.com/v1/payment
method = GET
enabled = true
activeScenarioKey = HTTP_500
slowDelayMs = (unchanged, default 1000)
```

**Does not write** a custom row. Body is generated at request time (`defaultErrorBody(500)`), not stored.

**Next GET to that exact URL:** interceptor never calls the server. App receives HTTP 500, body:

```json
{"error":"Internal Server Error","status":500,"mocked":true}
```

Headers include `X-Mock-Scenario: 500 Internal Server Error`. Network list shows **MOCK**.

Tap the same row again → `activeScenarioKey = null` (mock off for scenarios; cache/legacy/Missing rules apply).

### 5.5 Select Success / Empty Response / Empty Body

These need a **captured** body from `cached_responses` or `mock_responses`.

**When Scenarios screen opens**, current traffic body is saved to cache. Then:

| Scenario | Stored key | At request time |
|----------|------------|-----------------|
| Success | `SUCCESS` | 200 + captured JSON/text |
| Empty Response | `EMPTY_RESPONSE` | 200 + `{}` or `[]` |
| Empty Body | `EMPTY_BODY` | 200 + `""` |

If cache and mock table are both empty, Success uses `"{}"`.

### 5.6 Select Slow Response + delay

**Writes:**

- `activeScenarioKey = SLOW_RESPONSE`
- `slowDelayMs = 500 | 1000 | 2000 | 3000 | 5000 | custom`

**At request time:** engine copies Success body, sets `delayMs`. Interceptor `Thread.sleep` then returns 200. App sees a slow success, not a timeout exception.

### 5.7 Select Timeout / No Internet / Connection Failure

**Writes:** `activeScenarioKey = TIMEOUT` (or `NO_INTERNET` / `CONNECTION_FAILURE`).

**At request time:** `MockPlan.failure != NONE`. Interceptor does **not** return HTTP. It logs a `NetworkCall` with `statusCode = 0`, `exception = SocketTimeoutException: DevTool mock: timeout (...)`, then **throws**. Retrofit/OkHttp callbacks get a failure, not a 500 body.

This is the difference from 4xx/5xx scenarios.

### 5.8 Add a custom scenario

**UI:** `+` on Scenarios screen. Fields: name, description, status, delay ms, headers (`Name: Value` lines), body.

**Example:** Payment Pending

```text
Name: Payment Pending
Status: 202
Delay: 1500
Body: {"status":"pending"}
```

**Writes:**

1. INSERT `mock_custom_scenarios` → e.g. `id = 12`
2. `activateScenario(..., "custom:12")` → `mock_api_config.activeScenarioKey = custom:12`

**At request time:** engine uses that row (status 202, body, headers, 1500 ms sleep). HTTP response, not a thrown exception.

Edit: `updateCustom` overwrites the same id. Delete: removes row; if it was active, `activeScenarioKey` cleared.

### 5.9 Override Response (detail screen)

Saves current body/status/headers into **`cached_responses` only**. Does not set `activeScenarioKey`. Used when global mock is ON and no scenario is selected (step 5 in `decide()`).

### 5.10 Production / release safety

`MockSafety.debugBuild` is false if the **host app** is not debuggable. Then `setMockingEnabled(true)` is ignored and interceptors never short-circuit. Release apps keep talking to real APIs.

---

## 6. Worked examples per main scenario

Assume:

- Global mock **ON**
- API mock **ON**
- Endpoint `GET https://shop.test/api/payment`
- Captured body in cache: `{"status":"paid","amount":99}`

### Success

- Store: `activeScenarioKey = SUCCESS`
- App receives: **200**, body `{"status":"paid","amount":99}`
- Real server: not called

### Empty Response

- Key: `EMPTY_RESPONSE`
- App receives: **200**, body `{}` (object capture). If capture had been `[{...}]`, body would be `[]`

### Empty Body

- Key: `EMPTY_BODY`
- App receives: **200**, body empty string

### 4xx (e.g. 401)

- Key: `HTTP_401`
- App receives: **401**, JSON error payload, `X-Mock-Scenario: 401 Unauthorized`
- Useful to test login-expired UI without backend

### 5xx (e.g. 503)

- Key: `HTTP_503`
- App receives: **503** JSON, no throw

### Timeout

- Key: `TIMEOUT`
- App: `SocketTimeoutException` — `onFailure` / catch, not a parsed error body

### No Internet

- Key: `NO_INTERNET`
- App: `UnknownHostException`

### Connection Failure

- Key: `CONNECTION_FAILURE`
- App: `ConnectException`

### Slow Response (2s)

- Keys/columns: `SLOW_RESPONSE`, `slowDelayMs = 2000`
- App waits ~2s, then **200** + captured success body

### Custom “Payment Pending”

- Row in `mock_custom_scenarios` (id 12)
- Config: `activeScenarioKey = custom:12`
- App waits `delayMs`, then **202** + `{"status":"pending"}` + custom headers

---

## 7. Matching rules (important)

Mocks bind to the **full URL string** plus **method**, not path-only.

`GET https://shop.test/api/payment?id=1` and `GET https://shop.test/api/payment?id=2` are **two** APIs. Select the scenario on the same Network row the app will call.

---

## 8. Headers the UI looks at

| Header | Meaning |
|--------|---------|
| `X-Mock-Source` | `Scenario`, `Missing`, or older cache/legacy labels |
| `X-Mock-Scenario` | Human name (`500 Internal Server Error`, `Payment Pending`, …) |
| `X-Mock-Key` | `HTTP_500`, `custom:12`, … |

Detail banner also reads live `MockScenarioRepository.observe` so it shows the **configured** scenario even before the next mocked call.

---

## 9. What was intentionally not changed

- App still only adds the existing interceptor/plugin
- Old `mock_responses` + cache fallback still work when no scenario is selected
- Global Missing-404 when mock is ON and nothing is cached remains (so unconfigured endpoints do not silently hit production while mock is globally on)

---

## 10. Quick reference — files to open

```text
Engine:     mock/scenario/MockScenarioEngine.kt
Catalog:    mock/scenario/MockScenarioCatalog.kt
Persist:    mock/scenario/MockScenarioRepository.kt
            internal/database/MockApiConfigEntity.kt
            internal/database/MockCustomScenarioEntity.kt
            internal/database/MockScenarioDao.kt
Decide:     mock/MockManager.kt
OkHttp:     network/interceptor/DevToolNetworkInterceptor.kt
Ktor:       DevToolPlugin.kt
Safety:     mock/MockSafety.kt
UI:         ui/dashboard/network/MockScenarioScreen.kt
            ui/dashboard/network/NetworkDetailScreen.kt
```
