# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

Kounta — native Android app (Kotlin + Jetpack Compose) for scoring 5 card/dice games: **Truco**, **Generala**, **Chin Chon**, **10Mil**, **Comodín** (generic scorer). Single-module app, no backend, no tests currently in repo.

## Commands

```bash
./gradlew installDebug     # build + install debug APK on connected device/emulator
./gradlew assembleDebug    # build debug APK only
./gradlew build            # full build (compile + lint + assemble)
./gradlew lint             # Android lint
```

No test suite exists in this repo yet. If adding tests, wire them under `app/src/test` (JVM/unit) or `app/src/androidTest` (instrumented) and run via `./gradlew test` / `./gradlew connectedAndroidTest`.

Requires JDK 17 + Android SDK (compileSdk/targetSdk 35, minSdk 26). Usually driven from Android Studio; `gradlew` wrapper may need Android Studio's first sync to generate on Windows.

## Architecture

Each game is a **self-contained vertical slice** under `app/src/main/java/com/gamescounter/truco/<game>/` (except Truco, which lives at the top level alongside the shared files), following the same 3-file pattern per game:

- `<Game>Models.kt` — data classes/enums, validation constants, `clamped()`/normalization logic
- `<Game>Repository.kt` — DataStore Preferences persistence, load/save/clear
- `<Game>ViewModel.kt` (`AndroidViewModel`) — `MutableStateFlow<UiState>` exposed as `StateFlow`, auto-save scheduling

Corresponding screen composables live in `ui/<Game>Screen.kt`, with shared UI pieces in `ui/components/` (`GameTopBar`, `PlayerSetupScreen`, `ScoreGridCells`, `KountaComponents`) and theme in `ui/theme/`.

### Persistence pattern (repeated per game, not abstracted into a base class)

- One DataStore Preferences instance per game (`"<game>_scores"`), created via `preferencesDataStore` extension property on `Context`.
- Every saved record stores a `last_updated` epoch-ms timestamp.
- On `load()`, if `now - last_updated > SAVE_RETENTION_DAYS` (`data/TrucoScore.kt`, currently 1 day), the repository clears itself and returns `null` — this is the app's expiry mechanism, re-implemented per repository rather than shared.
- Complex state (Generala's per-player grid) is serialized into single string preferences using custom delimiters (`,` for initials, `|` for rows, `;` for cells, `_` for null) rather than JSON — follow this convention if extending a grid-based game.

### ViewModel state flow

- UI state is a single data class (`<Game>UiState`) with `score`/`board`, `saveStatus: SaveStatus` (`Idle` / `Pending` / `Saved`), and `isLoading`.
- Mutations go through a private `adjustScore`/equivalent helper that clamps values, sets `hasUnsavedChanges = true`, updates state to `Pending`, and calls `scheduleAutoSave()`.
- `scheduleAutoSave()` cancels any pending `autoSaveJob` and relaunches a coroutine that `delay(AUTO_SAVE_DELAY_MS)` (30s) then persists.
- `flushSave()` is called from `MainActivity.onStop()` for every ViewModel, to force-persist unsaved changes immediately when the app is backgrounded — always wire new games into this.
- `resetGame()` cancels the autosave job, clears the repository, and resets UI state to defaults.

### Navigation

No navigation library — `MainActivity` holds a single `rememberSaveable { mutableStateOf("home") }` string and switches composables in a `when` block (`GameEntry.<Game>.name` as keys, from `ui/HomeScreen.kt`). Each game ViewModel is instantiated eagerly via `by viewModels()` in `MainActivity`, not lazily per screen.

### Game-specific rules (encoded in `Models.kt`/`ViewModel.kt`, not config)

- **Truco**: two-team score, clamped 0–30.
- **Generala**: up to `MAX_GENERALA_PLAYERS`, rows 1,2,3,4,5,6,E,F,P,G,2G, computed totals.
- **Chin Chon**: per-round signed values, running total, visual flag at 100+.
- **10Mil**: per-round values starting from 750, exact target 10000, visual flag on win.
- **Comodín**: generic per-round scorer (up to `MAX_COMODIN_PLAYERS`), no target/win condition, optional negative values, for any game not covered above.

When adding a new game, mirror this structure: `Models` + `Repository` + `ViewModel` in a new package, a `Screen.kt`, a `GameEntry` entry, a `by viewModels()` field + `flushSave()` wiring in `MainActivity`.
