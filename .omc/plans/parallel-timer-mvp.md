# Parallel Timer MVP Implementation Plan

## 1. Requirements Summary

### Product Vision
A multi-timer app that manages multiple timers simultaneously on a single screen, fully offline with zero permissions.

### Core Differentiators
- Named timers with color coding
- Quick presets (5/10/25 minutes)
- Persistent state across app restarts

### Technical Constraints
- **Time Calculation**: Use `endAtEpochMillis` for Running timers (NOT `remaining--` per second)
- **UI Tick**: Display refresh only (250ms-1s interval)
- **Restart Behavior**: Recalculate `remaining` from `endAtEpoch` on app restart
- **Time Source**: `System.currentTimeMillis()` (NOT `SystemClock.elapsedRealtime()`)
  - Rationale: Timers persist across device reboot (elapsedRealtime resets on reboot)
  - Known limitation: Device time change may cause timer inaccuracy (documented below)

### Data Model
```kotlin
data class TimerItem(
    val id: String,          // UUID
    val label: String,
    val colorIndex: Int,
    val durationMs: Long,    // Initial configured duration
    val state: TimerState,   // Idle|Running|Paused|Done
    val remainingMs: Long,   // Used in Paused/Idle states
    val endAtEpochMs: Long?, // Used in Running state (null otherwise)
    val createdAtEpochMs: Long
)

enum class TimerState { Idle, Running, Paused, Done }
```

### Build Configuration
| Property | Value |
|----------|-------|
| Kotlin | 1.9.22 |
| JDK | 17 (Android Gradle Plugin 8.x requirement) |
| compileSdk | 34 |
| targetSdk | 34 |
| minSdk | 26 (DataStore + Compose minimum) |

### Dependency Injection
**Approach**: Manual DI (no Hilt)
- Simple Application-scoped container
- ViewModel factory pattern
- Rationale: Minimal overhead for MVP, easier to understand

### Serialization
**Library**: `kotlinx.serialization` v1.6.2
- Compile-time safety
- Native Kotlin support
- No reflection (ProGuard-friendly)
- Built-in enum serialization

---

## 2. Acceptance Criteria

### Functional Criteria
| ID | Criterion | Verification |
|----|-----------|--------------|
| AC-1 | User can create timer with name (max 20 chars), color (6 options), duration | Create timer, verify all fields saved |
| AC-2 | User can start/pause/reset/delete any timer | Test each action on multiple timers |
| AC-3 | Preset chips (5/10/25 min) create timer with one tap | Tap each preset, verify duration |
| AC-4 | Multiple timers run independently and simultaneously | Run 5+ timers, verify no interference |
| AC-5 | Timer shows completion state when time reaches zero | Let timer complete, verify Done state |
| AC-6 | App restart preserves all timer states | Kill app, restart, verify state |
| AC-7 | Running timers show correct remaining time after restart | Start timer, kill app, restart, verify remaining matches elapsed |

### Non-Functional Criteria
| ID | Criterion | Verification |
|----|-----------|--------------|
| NF-1 | Zero permissions required | Check AndroidManifest.xml |
| NF-2 | Works 100% offline | Enable airplane mode, test all features |
| NF-3 | Screen rotation preserves UI state | Rotate during operation, verify no crash/loss |
| NF-4 | Dark mode supported | Toggle system dark mode, verify readability |
| NF-5 | 10 concurrent timers with no frame drops | Profile with 10 running timers |
| NF-6 | No memory leaks | Run LeakCanary in debug build |
| NF-7 | Zero crashes | Test all paths, verify no exceptions |

---

## 3. Implementation Phases

### Phase 0: Project Setup
**Goal**: Android project with Compose, dependencies configured

#### Tasks
| Task | File(s) | Description |
|------|---------|-------------|
| 0.1 | `build.gradle.kts` (root) | Create project with Kotlin DSL |
| 0.2 | `app/build.gradle.kts` | Configure Compose, DataStore dependencies |
| 0.3 | `settings.gradle.kts` | Module settings |
| 0.4 | `gradle.properties` | Gradle configuration |
| 0.5 | `app/src/main/AndroidManifest.xml` | Minimal manifest (no permissions) |
| 0.6 | `app/src/main/java/com/example/paralleltimer/ParallelTimerApp.kt` | Application class with AppContainer |
| 0.7 | `di/AppContainer.kt` | Manual DI container |
| 0.8 | `di/ViewModelFactory.kt` | ViewModel factory |

#### Build Configuration (app/build.gradle.kts)
```kotlin
android {
    namespace = "com.example.paralleltimer"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.paralleltimer"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}
```

#### Plugins (app/build.gradle.kts)
```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    kotlin("plugin.serialization") version "1.9.22"
}
```

#### Dependencies
```kotlin
// Compose BOM
implementation(platform("androidx.compose:compose-bom:2024.02.00"))
implementation("androidx.compose.ui:ui")
implementation("androidx.compose.material3:material3")
implementation("androidx.compose.ui:ui-tooling-preview")

// ViewModel + Lifecycle
implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")

// DataStore
implementation("androidx.datastore:datastore-preferences:1.0.0")

// Serialization
implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")

// Navigation (optional for MVP, single screen)
implementation("androidx.navigation:navigation-compose:2.7.7")
```

#### Manual DI Implementation

**`di/AppContainer.kt`**
```kotlin
class AppContainer(private val context: Context) {
    val timerDataStore: TimerDataStore by lazy { TimerDataStore(context) }
    val timerRepository: TimerRepository by lazy { TimerRepositoryImpl(timerDataStore) }
}
```

**`di/ViewModelFactory.kt`**
```kotlin
class TimerViewModelFactory(
    private val repository: TimerRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TimerListViewModel::class.java)) {
            return TimerListViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
```

**`ParallelTimerApp.kt`**
```kotlin
class ParallelTimerApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
```

#### Verification
- [ ] Project builds successfully
- [ ] App launches to empty screen

---

### Phase 1: Domain Layer
**Goal**: Core data models and business logic

#### Tasks
| Task | File | Description |
|------|------|-------------|
| 1.1 | `domain/model/TimerItem.kt` | Data class with all fields (with @Serializable) |
| 1.2 | `domain/model/TimerState.kt` | Enum: Idle, Running, Paused, Done (with @Serializable) |
| 1.3 | `domain/model/TimerColor.kt` | Enum with 6 color options |
| 1.4 | `domain/model/TimerPreset.kt` | Data class for presets (5/10/25 min) |
| 1.5 | `domain/model/TimerDisplayItem.kt` | UI display wrapper for TimerItem |

#### File Contents

**`domain/model/TimerItem.kt`**
```kotlin
import kotlinx.serialization.Serializable

@Serializable
data class TimerItem(
    val id: String,              // UUID
    val label: String,
    val colorIndex: Int,
    val durationMs: Long,        // Initial configured duration
    val state: TimerState,       // Idle|Running|Paused|Done
    val remainingMs: Long,       // Used in Paused/Idle states
    val endAtEpochMs: Long?,     // Used in Running state (null otherwise)
    val createdAtEpochMs: Long
)
```

**`domain/model/TimerState.kt`**
```kotlin
import kotlinx.serialization.Serializable

@Serializable
enum class TimerState {
    Idle,     // Created but never started
    Running,  // Counting down
    Paused,   // Stopped mid-countdown
    Done      // Reached zero
}
```

**`domain/model/TimerDisplayItem.kt`**
```kotlin
/**
 * UI display wrapper that combines a TimerItem with its calculated display remaining time.
 * Used by ViewModel to provide pre-computed display values to UI layer.
 */
data class TimerDisplayItem(
    val timer: TimerItem,
    val displayRemainingMs: Long  // Calculated remaining time for display
)
```

**`domain/model/TimerColor.kt`**
```kotlin
enum class TimerColor(val colorValue: Long) {
    Red(0xFFE57373),
    Orange(0xFFFFB74D),
    Yellow(0xFFFFF176),
    Green(0xFF81C784),
    Blue(0xFF64B5F6),
    Purple(0xFFBA68C8)
}
```

**`domain/model/TimerPreset.kt`**
```kotlin
data class TimerPreset(
    val label: String,
    val durationMs: Long
) {
    companion object {
        val FIVE_MINUTES = TimerPreset("5 min", 5 * 60 * 1000L)
        val TEN_MINUTES = TimerPreset("10 min", 10 * 60 * 1000L)
        val TWENTY_FIVE_MINUTES = TimerPreset("25 min", 25 * 60 * 1000L)

        val defaults = listOf(FIVE_MINUTES, TEN_MINUTES, TWENTY_FIVE_MINUTES)
    }
}
```

#### Verification
- [ ] All model classes compile without errors
- [ ] Unit tests pass for TimerItem state transitions

---

### Phase 2: Data Layer
**Goal**: Persistence with DataStore

#### Tasks
| Task | File | Description |
|------|------|-------------|
| 2.1 | `data/local/TimerSerializer.kt` | kotlinx.serialization JSON for TimerItem list |
| 2.2 | `data/local/TimerDataStore.kt` | DataStore wrapper for read/write |
| 2.3 | `data/repository/TimerRepository.kt` | Repository interface |
| 2.4 | `data/repository/TimerRepositoryImpl.kt` | Implementation with DataStore |

#### Key Implementation Details

**`data/local/TimerSerializer.kt`**
```kotlin
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object TimerSerializer {
    private val json = Json {
        ignoreUnknownKeys = true  // Forward compatibility
        encodeDefaults = true     // Explicit serialization
    }

    fun serialize(timers: List<TimerItem>): String = json.encodeToString(timers)

    fun deserialize(jsonString: String): List<TimerItem> = try {
        json.decodeFromString(jsonString)
    } catch (e: Exception) {
        emptyList()  // Graceful degradation on corruption
    }
}
```

**`data/local/TimerDataStore.kt`**
```kotlin
class TimerDataStore(private val context: Context) {
    private val Context.dataStore by preferencesDataStore(name = "timers")

    private val TIMERS_KEY = stringPreferencesKey("timers_json")

    val timersFlow: Flow<List<TimerItem>> = context.dataStore.data
        .map { prefs ->
            val json = prefs[TIMERS_KEY] ?: "[]"
            TimerSerializer.deserialize(json)
        }

    suspend fun saveTimers(timers: List<TimerItem>) {
        context.dataStore.edit { prefs ->
            prefs[TIMERS_KEY] = TimerSerializer.serialize(timers)
        }
    }
}
```

**`data/repository/TimerRepository.kt`**
```kotlin
interface TimerRepository {
    val timers: Flow<List<TimerItem>>
    suspend fun addTimer(timer: TimerItem)
    suspend fun updateTimer(timer: TimerItem)
    suspend fun deleteTimer(id: String)
    suspend fun deleteAllTimers()
}
```

#### Verification
- [ ] Save 5 timers, restart app, all 5 load correctly
- [ ] Running timer state persists with correct `endAtEpochMs`

---

### Phase 3: ViewModel Layer
**Goal**: State management with time recalculation logic

#### Tasks
| Task | File | Description |
|------|------|-------------|
| 3.1 | `ui/viewmodel/TimerListViewModel.kt` | Main ViewModel with StateFlow |
| 3.2 | `ui/viewmodel/TimerUiState.kt` | UI state data class |
| 3.3 | `ui/viewmodel/TimerAction.kt` | Sealed class for user actions |

#### Critical Logic: Time Recalculation

**On App Start (ViewModel init)**
```kotlin
private fun recalculateRunningTimers(timers: List<TimerItem>): List<TimerItem> {
    val now = System.currentTimeMillis()
    return timers.map { timer ->
        when (timer.state) {
            TimerState.Running -> {
                val endAt = timer.endAtEpochMs ?: return@map timer
                val remaining = (endAt - now).coerceAtLeast(0)
                if (remaining == 0L) {
                    timer.copy(state = TimerState.Done, remainingMs = 0)
                } else {
                    timer.copy(remainingMs = remaining)
                }
            }
            else -> timer
        }
    }
}
```

**Start Timer Action**
```kotlin
fun startTimer(id: String) {
    viewModelScope.launch {
        val timer = getTimer(id) ?: return@launch
        val now = System.currentTimeMillis()
        val endAt = now + timer.remainingMs
        repository.updateTimer(
            timer.copy(
                state = TimerState.Running,
                endAtEpochMs = endAt
            )
        )
    }
}
```

**Pause Timer Action**
```kotlin
fun pauseTimer(id: String) {
    viewModelScope.launch {
        val timer = getTimer(id) ?: return@launch
        val now = System.currentTimeMillis()
        val remaining = ((timer.endAtEpochMs ?: now) - now).coerceAtLeast(0)
        repository.updateTimer(
            timer.copy(
                state = TimerState.Paused,
                remainingMs = remaining,
                endAtEpochMs = null
            )
        )
    }
}
```

**UI Tick (Display Only)**
```kotlin
private val _tickTrigger = MutableStateFlow(0L)

init {
    // Tick every 500ms for UI refresh
    viewModelScope.launch {
        while (true) {
            delay(500)
            _tickTrigger.value = System.currentTimeMillis()
        }
    }
}

val displayTimers: StateFlow<List<TimerDisplayItem>> = combine(
    repository.timers,
    _tickTrigger
) { timers, now ->
    timers.map { timer ->
        val displayRemaining = when (timer.state) {
            TimerState.Running -> {
                ((timer.endAtEpochMs ?: now) - now).coerceAtLeast(0)
            }
            else -> timer.remainingMs
        }
        TimerDisplayItem(timer, displayRemaining)
    }
}.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
```

#### Verification
- [ ] Start timer, wait 10s, pause - remaining shows ~original-10s
- [ ] Start timer, kill app, restart - remaining correctly recalculated
- [ ] Timer reaching zero transitions to Done state

---

### Phase 4: UI Layer - Components
**Goal**: Reusable Compose components

#### Tasks
| Task | File | Description |
|------|------|-------------|
| 4.1 | `ui/theme/Theme.kt` | Material 3 theme with dark mode |
| 4.2 | `ui/theme/Color.kt` | Color definitions |
| 4.3 | `ui/components/TimerCard.kt` | Individual timer display |
| 4.4 | `ui/components/TimerControls.kt` | Start/Pause/Reset/Delete buttons |
| 4.5 | `ui/components/PresetChips.kt` | Preset selection row |
| 4.6 | `ui/components/AddTimerDialog.kt` | Timer creation bottom sheet |
| 4.7 | `ui/components/ColorPicker.kt` | Color selection component |
| 4.8 | `ui/components/DurationPicker.kt` | Time input (MM:SS or wheel) |

#### Component Specifications

**TimerCard**
- Shows: label, color indicator, remaining time (MM:SS), state
- Running state: pulsing animation optional
- Done state: distinct visual (e.g., checkmark, green tint)

**TimerControls**
- Idle: [Start]
- Running: [Pause] [Reset]
- Paused: [Resume] [Reset]
- Done: [Reset] [Delete]

**AddTimerDialog (BottomSheet)**
- Label input (TextField, 20 char limit)
- Color picker (6 options as circles)
- Duration picker (NumberPicker or TextField MM:SS)
- [Cancel] [Create] buttons

#### Verification
- [ ] All components render without crash
- [ ] Components adapt to dark mode
- [ ] Touch targets meet 48dp minimum

---

### Phase 5: UI Layer - Screens
**Goal**: Main screens assembled from components

#### Tasks
| Task | File | Description |
|------|------|-------------|
| 5.1 | `ui/screen/HomeScreen.kt` | Main timer list screen |
| 5.2 | `ui/screen/SettingsScreen.kt` | Minimal settings (optional for MVP) |
| 5.3 | `MainActivity.kt` | Entry point with Compose setup |

#### HomeScreen Layout
```
[Toolbar: "Parallel Timer"]
[PresetChips: 5min | 10min | 25min]
[LazyColumn: TimerCards]
  - TimerCard 1
  - TimerCard 2
  - ...
[FAB: + Add Timer] → Opens AddTimerDialog
```

#### Empty State
When no timers exist, show:
- Illustration or icon
- "No timers yet"
- "Tap + or use presets to start"

#### Verification
- [ ] Screen renders with 0, 1, 10 timers
- [ ] FAB opens AddTimerDialog
- [ ] Preset chips create timers immediately
- [ ] Scroll works smoothly with many timers

---

### Phase 6: Integration & Polish
**Goal**: Wire everything together, handle edge cases

#### Tasks
| Task | File | Description |
|------|------|-------------|
| 6.1 | Wire ViewModel to HomeScreen | Connect state and actions |
| 6.2 | Handle configuration changes | Verify rotation works |
| 6.3 | Add empty state | Show when no timers |
| 6.4 | Add loading state | Brief loading indicator |
| 6.5 | Error handling | Catch and display errors gracefully |

#### Edge Cases to Handle
- Rapid start/pause clicks
- Timer completion while app backgrounded
- Very long timer durations (hours)
- Label with special characters/emoji
- Device time change while timer running

#### Verification
- [ ] All acceptance criteria pass
- [ ] Rotation during all states works
- [ ] No ANRs or crashes in stress test

---

### Phase 7: Testing & QA
**Goal**: Ensure quality and stability

#### Tasks
| Task | File | Description |
|------|------|-------------|
| 7.1 | `test/.../TimerItemTest.kt` | Unit tests for model |
| 7.2 | `test/.../TimerRepositoryTest.kt` | Unit tests for repository |
| 7.3 | `test/.../TimerViewModelTest.kt` | ViewModel tests with TestDispatcher |
| 7.4 | `androidTest/.../HomeScreenTest.kt` | UI tests with Compose testing |
| 7.5 | Manual QA checklist | Verify all AC and NF criteria |

#### Test Priorities
1. Time recalculation on restart (critical)
2. State transitions (start/pause/reset)
3. Persistence across app restart
4. Multiple concurrent timers

#### Verification
- [ ] 80%+ code coverage on ViewModel
- [ ] All UI tests pass
- [ ] Manual QA checklist complete

---

## 4. File Structure

```
app/src/main/java/com/example/paralleltimer/
├── ParallelTimerApp.kt           # Application class
├── MainActivity.kt               # Entry point
├── domain/
│   └── model/
│       ├── TimerItem.kt          # @Serializable data class
│       ├── TimerState.kt         # @Serializable enum
│       ├── TimerColor.kt
│       ├── TimerPreset.kt
│       └── TimerDisplayItem.kt   # UI display wrapper
├── data/
│   ├── local/
│   │   ├── TimerSerializer.kt
│   │   └── TimerDataStore.kt
│   └── repository/
│       ├── TimerRepository.kt
│       └── TimerRepositoryImpl.kt
├── ui/
│   ├── theme/
│   │   ├── Theme.kt
│   │   ├── Color.kt
│   │   └── Type.kt
│   ├── components/
│   │   ├── TimerCard.kt
│   │   ├── TimerControls.kt
│   │   ├── PresetChips.kt
│   │   ├── AddTimerDialog.kt
│   │   ├── ColorPicker.kt
│   │   └── DurationPicker.kt
│   ├── screen/
│   │   ├── HomeScreen.kt
│   │   └── SettingsScreen.kt
│   └── viewmodel/
│       ├── TimerListViewModel.kt
│       ├── TimerUiState.kt
│       └── TimerAction.kt
└── di/                           # Manual DI (no Hilt)
    ├── AppContainer.kt           # Application-scoped dependencies
    └── ViewModelFactory.kt       # ViewModel factory for DI

app/src/main/res/
├── values/
│   ├── strings.xml
│   ├── colors.xml
│   └── themes.xml
├── values-night/
│   └── themes.xml
└── drawable/
    └── ic_timer.xml              # App icon
```

---

## 5. Risks and Mitigations

| Risk | Impact | Likelihood | Mitigation |
|------|--------|------------|------------|
| Time drift due to system time change | Timer shows wrong remaining | Low | Documented limitation (see Technical Notes); users informed |
| DataStore corruption | Data loss | Very Low | Graceful degradation in TimerSerializer.deserialize() returns emptyList() |
| Memory pressure with many timers | OOM crash | Low | Limit timer count (e.g., 50); use LazyColumn |
| UI jank during rapid updates | Poor UX | Medium | Throttle tick to 500ms; use `derivedStateOf` |
| Configuration change loses dialog state | Poor UX | Medium | Use rememberSaveable for dialog state |
| Background timer completion missed | User unaware | Medium | Consider WorkManager for notifications (post-MVP) |

---

## 6. Verification Steps by Phase

### Phase 0: Project Setup
```bash
./gradlew assembleDebug
# App installs and shows empty Compose screen
```

### Phase 1: Domain Layer
```bash
./gradlew test --tests "*.model.*"
# All model tests pass
```

### Phase 2: Data Layer
```bash
# Instrumented test
./gradlew connectedAndroidTest --tests "*.data.*"
# Verify save/load cycle works
```

### Phase 3: ViewModel Layer
```bash
./gradlew test --tests "*.viewmodel.*"
# Especially: time recalculation tests
```

### Phase 4-5: UI Layers
```bash
# Visual inspection
# Launch app, verify all components render
# Test dark mode toggle
# Test screen rotation
```

### Phase 6: Integration
```bash
# Full manual QA
# Acceptance criteria checklist
# Non-functional criteria checklist
```

### Phase 7: Final QA
```bash
./gradlew test
./gradlew connectedAndroidTest
# All tests green
# LeakCanary shows no leaks
```

---

## 7. Commit Strategy

| Phase | Commit Message |
|-------|----------------|
| 0 | `chore: initialize Android project with Compose dependencies` |
| 1 | `feat(domain): add timer data models and enums` |
| 2 | `feat(data): implement DataStore persistence layer` |
| 3 | `feat(viewmodel): add timer state management with epoch-based timing` |
| 4 | `feat(ui): create timer card and control components` |
| 5 | `feat(ui): implement home screen with timer list` |
| 6 | `feat: wire up full timer flow with edge case handling` |
| 7 | `test: add unit and UI tests for timer functionality` |

---

## 8. Success Criteria

### MVP Complete When:
- [ ] All 7 functional acceptance criteria (AC-1 to AC-7) pass
- [ ] All 7 non-functional criteria (NF-1 to NF-7) pass
- [ ] App builds and runs without crashes
- [ ] Code reviewed and merged to main

### Definition of Done:
1. Feature works as specified
2. Unit tests cover critical paths
3. No compiler warnings
4. Code follows Kotlin/Compose conventions
5. Accessibility basics met (content descriptions)

---

## 9. Out of Scope (Post-MVP)

- Push notifications for timer completion
- Sound/vibration alerts
- Custom preset management
- Timer grouping/folders
- Widget support
- Wear OS companion
- Cloud sync
- Timer history/statistics

---

## 10. Technical Notes

### Why endAtEpochMillis?
Using absolute end time instead of decrementing remaining every second:
1. **Accuracy**: No drift from missed ticks
2. **Restart resilience**: Recalculate instantly on app restart
3. **Battery efficient**: UI tick is display-only, not state mutation

### Why System.currentTimeMillis() over SystemClock.elapsedRealtime()?

| Approach | Pros | Cons |
|----------|------|------|
| `System.currentTimeMillis()` | Survives device reboot; absolute reference | Affected by manual time changes |
| `SystemClock.elapsedRealtime()` | Immune to time changes | Resets on reboot; timers break |

**Decision**: Use `System.currentTimeMillis()` because timer persistence across reboots is more valuable than immunity to manual time changes (rare edge case).

**Known Limitation**: If user manually changes device time while a timer is running:
- Time moved forward: Timer may show as completed early
- Time moved backward: Timer may show extra remaining time

This is an accepted limitation for MVP. Users are expected to not manually change device time during active timing sessions.

### DataStore vs Room
DataStore chosen for MVP because:
1. Simpler setup for list of items
2. No schema migrations needed
3. Sufficient for ~50 timers
4. Async by default

### State Transitions
```
Idle -> Running (start)
Running -> Paused (pause)
Running -> Done (time reaches zero)
Paused -> Running (resume)
Paused -> Idle (reset)
Done -> Idle (reset)
Any -> (deleted)
```
