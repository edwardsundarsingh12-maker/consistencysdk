# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project overview

`consistencysdk` packages the habit-tracking / consistency-grid feature as a **reusable Android library**
(`:ed_sdk`) plus a thin demo app (`:app`) that consumes it. The library is the product; the app exists to
exercise it. The codebase was derived from the standalone `Habittracker` app (a sibling project) and the
library tracks that app's features — see "Relationship to Habittracker" below.

## Modules

- **`:ed_sdk`** — the library (Android library plugin). **Namespace is `com.edapp.ed_sdk`** but all source
  lives under the package **`com.edapp.habittracker`** (a carryover from the origin app). This split matters:
  generated resources are `com.edapp.ed_sdk.R`, so widget/Activity code in the library imports
  `com.edapp.ed_sdk.R`, not `com.edapp.habittracker.R`.
- **`:app`** — demo/host app (`com.edapp.consistencysdk`, `Application` class `com.edapp.MyApp`). Depends on
  `:ed_sdk` via `implementation(project(":ed_sdk"))`.
- `sample_app/` exists on disk but is **not** included in `settings.gradle.kts` (only `:app` and `:ed_sdk` are).
- Published via JitPack (`jitpack.yml`, JDK 17).

## Build & run commands

Gradle wrapper from the repo root (PowerShell on Windows):

```powershell
.\gradlew.bat :ed_sdk:assembleDebug      # build the library AAR
.\gradlew.bat :ed_sdk:compileDebugKotlin # fastest way to type-check library changes
.\gradlew.bat assembleDebug              # build everything (app + library)
.\gradlew.bat :app:installDebug          # install the demo app on a device/emulator
.\gradlew.bat clean
```

There is **no meaningful test suite** — only stock `ExampleUnitTest`/`ExampleInstrumentedTest` stubs, and the
library's test deps are commented out in `ed_sdk/build.gradle.kts`. Verify changes by compiling and running the
demo app, not via `gradlew test`.

Note: the project sets `compileSdk = 36` with AGP 8.7.3, which emits an "untested compileSdk" warning — expected,
not an error.

## The SDK configuration layer (the key abstraction)

This is what makes `:ed_sdk` a configurable library rather than a copy of an app. Two singletons in
`ed_sdk/.../util/`:

- **`ConsistencySDK`** — immutable config built via `ConsistencySDK.Builder` with feature flags:
  `enableAddNewHabit`, `enableRowEditOption`, `canShowAllMonth`, `enableLineChart`,
  `enableReminderNotification`. `Builder.build()` **requires** `setAppContext(application)` or it throws.
- **`SDK`** — process-wide holder. `build()` calls `SDK.setAppContext(...)` and `SDK.init(config)`. Throughout
  the library, **`SDK.getAppContext()` is the canonical way to get a Context** (do not reintroduce references to
  the host app's `MyApp`), and `SDK.config.<flag>` gates behavior (e.g. `MainScreen` shows the add button only
  when `enableAddNewHabit`; `ReminderReceiver` no-ops unless `enableReminderNotification`;
  `PreferenceUtil.isRowView()` is `&&`-gated by `canShowAllMonth`).

Host apps initialize the SDK in `Application.onCreate()` (see `app/.../MyApp.kt`) before any library screen or
DAO is touched. `DatabaseModule.provideHabitRepository` calls `SDK.initRepository(repo)` so the repository is
reachable outside Hilt (e.g. broadcast receivers).

## Architecture (inside `:ed_sdk`)

Layered MVVM wired with Hilt: `data` (Room) → `domain` (plain models) → `ui` (Compose + `HabitViewModel`),
with `di` mapping between layers.

- **`data/`** — Room. `Entity.kt` (`HabitEntity`/`HabitLogEntity`/`HabitTagEntity` + `HabitWithLogs`),
  `HabitDao`, `HabitDatabase`, `HabitRepository` (the hub: insert/edit habits, fill missing days, schedule
  alarms via `AlarmManager`, combine flows into domain `Habit`s, archive/lock/reorder/backup helpers),
  `Converters` (Gson JSON for `List<ReminderData>`).
  - **DB schema is version 4.** Migrations are defined in `HabitDatabase.Companion` and registered in
    `di/DatabaseModule.kt` via `addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)`:
    `1→2` adds the SDK-specific `uncheckedColorValue` column, `2→3` adds `isArchived`, `3→4` adds
    `isLocked`/`passKey`. There is **no destructive-migration fallback** in the Hilt-provided DB, so **any
    `HabitEntity` column change needs a new migration**. `HabitDatabase.getDatabase(context)` is a separate
    non-Hilt singleton used by the widget code.
- **`domain/`** — `Habit`/`HabitYear`/`HabitMonth`/`HabitLog`, `HabitTag`, `UpdateHabit` (mutable draft for
  create/edit), `ReminderData`, and the `BootReceiver`/`ReminderReceiver` broadcast receivers (registered in
  the library `AndroidManifest.xml`).
- **`di/`** — `DatabaseModule` (DB/DAO/Repository singletons + default tags + migrations + `SDK.initRepository`)
  and `HabitMapper.kt`, which contains `HabitMapper.mapToDomain` (entity+logs → `Habit`, including streak/total/
  completed stats via `calculateStats`) **and** `IconMapper` (the large string→icon registry used everywhere).
- **`ui/`** — `HabitViewModel` (`@HiltViewModel`, the shared screen state) plus Compose screens/components:
  - `HabitViewModel` deliberately serves two tag concepts: `addOrRemoveTag` toggles tags **on the habit being
    edited** (`_updateHabit.tagIds`), while `toggleTag`/`selectedTags`/`clearSelection` drive the **main-list
    filter**; the `habits` flow is `selectedTags.flatMapLatest { repository.getAllHabits(it) }`.
  - `consitency/Consistency.kt` — the consistency grid renderer. Note `IconStrike`/`MonthConsistencyCompose`
    take a per-habit `uncheckedColorValue` (an SDK feature absent from the origin app).
  - `consistencyviewutil/` — ViewModel-decoupled reusable consistency UI: `ConsistencyViewState` (data),
    `ConsistencyViewCallbacks` (interface), `ConsistencyViewComposable` (renders list/grid + calendar sheet via
    callbacks). Use this for new host screens instead of binding `HabitViewModel` directly.
  - `widgets/` — see below.
  - `components/` — shared Compose widgets (`ToolbarWithAnimation`+`HabitTagRow`, `Tags`/`TagChip`,
    `EditHabitPopup`/`HabitCalendarBottomSheet`, etc.).
- **`util/`** — `SDK`/`ConsistencySDK` (above), `PreferenceUtil` (SharedPreferences), `Screens` (nav routes),
  `HabitStatusEnum` (NotDone/Partial/Done/Streak persisted as ints 0/25/50/100 in `HabitLogEntity.status`),
  `CommonUtility`, `Simmer.kt` (shimmer + `FullScreenLoader`), `IconRepresentation` (Vector vs Emoji).

## Home-screen widget (in `:ed_sdk`)

The functional widget is **RemoteViews-based** (`ui/widgets/HabitWidgetProvider` : `AppWidgetProvider`, with
`HabitWidgetConfigActivity`), registered in the library manifest and backed by `res/layout/simple_habit_widget.xml`,
`res/xml/habit_widget_info.xml`, etc. It reads the DB through `HabitDatabase.getDatabase(context)` and renders
the consistency grid as a `Canvas` bitmap. Because the library has no `MainActivity`, the widget's tap target
uses `packageManager.getLaunchIntentForPackage(context.packageName)` to open the **host** app.
`HabitGlanceWidget.kt`/`HabitWidgetComposable.kt` are self-contained Glance composables that are **not yet wired
to a `GlanceAppWidget`/receiver** — building blocks, not a live widget.

## Relationship to Habittracker

`:ed_sdk` mirrors the sibling `Habittracker` app's habit/consistency logic, then adds the SDK config layer and a
per-habit `uncheckedColorValue`. When porting features back from `Habittracker`, preserve the two SDK
adaptations: **`SDK.config` flag gating** and **`SDK.getAppContext()`** (the origin app uses `MyApp` and a
concrete `MainActivity`, neither of which exists here). App-only features that depend on `MainActivity`/app
resources or out-of-scope packages (daily-reminder settings, locked/archived/reorder screens, theme store) are
intentionally not part of the library.

## Conventions & gotchas

- `java.time` date logic is gated behind `@RequiresApi(Build.VERSION_CODES.O)` / `Build.VERSION.SDK_INT >=
  Build.VERSION_CODES.O` because `minSdk = 24`. Follow that pattern when touching habit/log/date code.
- `HabitStatusEnum`'s int mapping is persisted; keep it stable or write a migration.
