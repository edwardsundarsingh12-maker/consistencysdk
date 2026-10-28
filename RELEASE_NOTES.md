# Release Notes

## Unreleased — Feature parity with Habit Tracker

The `ed_sdk` consistency library now matches the advanced behavior of the Habit Tracker app, while keeping the SDK's own configuration layer intact.

### Added
- **Archive & lock support** — habits can be archived or locked (with passkey); repository APIs for archive/lock/reorder and JSON backup/restore.
- **Per-habit stats** — streak, completed days, and total days are now computed and exposed on each habit.
- **Tag filtering** — the main habit list reactively filters by selected tags.
- **Reusable consistency view** (`consistencyviewutil`) — a ViewModel-decoupled component (state + callbacks + composable) for rendering the consistency grid on any screen.
- **Home-screen widget** — a configurable App Widget that shows a habit's consistency grid, streak, and one-tap check-in.

### Changed
- Room database upgraded to **version 4** with migrations (no data loss on upgrade).
- Editing a habit now correctly updates the existing record instead of creating a duplicate.

### Preserved
- SDK feature flags (`ConsistencySDK.Builder`: add-habit, row-edit, all-month view, line chart, reminders).
- Per-habit unchecked-cell color.
