# Repository Guidelines

## Project Structure & Module Organization

Planszowsky is a single-module Android application. Kotlin code is under `app/src/main/java/pl/pointblank/planszowsky/`:

- `ui/`: Compose screens, ViewModels, navigation, and themes.
- `domain/`: business models and repository contracts.
- `data/`: Room, Retrofit, and repository implementations.
- `di/`: Hilt modules; `util/`: platform helpers.
- `app/src/main/res/`: resources; `gradle/libs.versions.toml`: dependency versions.

Place JVM tests in `app/src/test/` and device/Compose tests in `app/src/androidTest/`, mirroring the production package structure.

## Build, Test, and Development Commands

Use the checked-in Gradle wrapper with JDK 17 and Android SDK 36:

- `./gradlew assembleDebug` (`gradlew.bat assembleDebug` on Windows): build the debug APK.
- `./gradlew testDebugUnitTest`: run local JUnit 4 tests.
- `./gradlew connectedDebugAndroidTest`: run AndroidX, Espresso, and Compose UI tests on a device.
- `./gradlew lintDebug`: run Android lint for the debug variant.

## Coding Style & Naming Conventions

Use Android Studio's Kotlin defaults: four-space indentation and trailing commas where useful. Use `PascalCase` for classes, Composables, and files; `camelCase` for functions and properties; and `UPPER_SNAKE_CASE` for constants. Use suffixes `*Screen`, `*ViewModel`, `*Entity`, `*Dao`, and `*Impl`. Keep UI state in ViewModels and wiring in Hilt modules. Add user-facing strings to Polish, English (`values-en`), and German (`values-de`) resources. No dedicated formatter is configured.

## Testing Guidelines

Use JUnit 4 for domain and ViewModel logic; use AndroidX Test, Espresso, and Compose APIs for integration/UI behavior. Name classes after the subject, for example `CollectionViewModelTest`, and methods by behavior, such as `filtersGamesByCategory`. Add regression coverage for fixes; no coverage threshold is configured.

## Security & Data Changes

Keep `BGG_API_KEY` and `GEMINI_API_KEY` in untracked `local.properties` or environment variables. Never commit credentials. Room schema changes must increment the database version and include a migration; do not introduce destructive migration fallback.

The app is in production. Preserve every released database version, DataStore key, navigation route, import/export format, and stored image path. Stage replacement data before deleting current data. Prefer additive migrations and feature gates; never assume users upgrade from only the latest release.

## Commits, Pull Requests & Agent Conduct

History uses short subjects (for example, `new fonts`) and Dependabot `Bump ...` commits. Prefer concise imperative subjects. Pull requests should explain scope, link issues, note database/configuration impact, and include UI screenshots when relevant.

Agents communicate concisely at engineering level. Do not run tests or suggest them; the user owns test execution. Report changed files and unverified assumptions directly.
