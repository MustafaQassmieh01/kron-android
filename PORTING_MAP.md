# Swift → Kotlin MVP Map

| iOS / Swift concept | Android MVP |
|---|---|
| `Application/Settings/AppSettings.swift` | `application/settings/AppSettings.kt` |
| network response models | `models/network/Response.kt` |
| `KronAPIService.swift` | `services/kron/api/KronApiService.kt` |
| `EventStorageService.swift` | `services/kron/store/event/EventStorageService.kt` |
| bookmarks screen | `screens/bookmarks/BookmarksScreen.kt` |
| weekly calendar | weekly mode inside `BookmarksScreen.kt` |
| event cards | `screens/other/EventCard.kt` |
| event details | `screens/bookmarks/details/EventDetailsScreen.kt` |
| search | `screens/search/SearchScreen.kt` |
| schedule preview | `screens/search/details/SearchDetailsScreen.kt` |
| basic settings | `screens/settings/SettingsScreen.kt` |
| SwiftUI navigation/coordinators | Navigation Compose in `MainActivity.kt` |
| Combine / observable state | Compose state + `StateFlow` |
| `UserDefaults` | Android `SharedPreferences` |
| local event file | private Android JSON cache |

## Important behavior preserved

- Literal `+` schedule IDs remain safe when sent as query parameters.
- `%2B` schedule IDs normalize back to `+` in the local event model.
- Server timestamps accept standard ISO-8601/offset forms used by the iOS app.
- Bookmarked schedule events are cached locally.
- Cache entries older than 30 days are cleaned once per day.
- Saved schedules can be refreshed manually from the bookmarks screen.

## Intentionally not mapped in the MVP

Monthly calendar, filters, notes, notifications, background workers, widgets, Pro/billing, course-color editing, advanced storage/network controls, and bookmark visibility were removed rather than ported forward in this reduced version.
