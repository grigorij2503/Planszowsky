# Planszowsky Context

## Project Overview

**Planszowsky** is a modern Android application designed for board game enthusiasts to manage their personal collections. It features a visually rich "juicy" UI with a masonry grid layout, ensuring game covers are the focal point.

### Key Features
*   **Collection Management:** Pinterest-style grid view of games.
*   **Smart Scanning:** Hybrid OCR (Text) and Barcode (EAN) scanner using Google ML Kit to quickly add games.
*   **Borrowing System:** Track games lent to friends.
*   **Randomizer:** "Slot machine" feature to pick a game to play.
*   **BGG Integration:** Fetches game metadata (currently mocked due to API auth changes).
*   **Wishlist:** Separate list for wanted games.

## Technical Architecture

The project follows **Modern Android Development (MAD)** standards:

*   **Language:** Kotlin
*   **UI:** Jetpack Compose (Material 3)
*   **Architecture:** MVVM + Clean Architecture
*   **DI:** Hilt
*   **Database:** Room (SQLite)
*   **Network:** Retrofit + OkHttp + Jackson (for XML)
*   **Image Loading:** Coil
*   **Camera/ML:** CameraX + ML Kit (Text Recognition, Barcode Scanning)

### Key Files & Directories

*   **Entry Points:**
    *   `app/src/main/java/com/planszowsky/android/PlanszowskyApp.kt`: Application class (Hilt setup).
    *   `app/src/main/java/com/planszowsky/android/MainActivity.kt`: Main activity hosting the Compose UI.
*   **Dependency Injection:**
    *   `app/src/main/java/com/planszowsky/android/di/AppModule.kt`: Provides `AppDatabase`, `OkHttpClient` (with Mock interceptor), and `BggApi`.
*   **Data Layer:**
    *   `app/src/main/java/com/planszowsky/android/data/local/`: Room Database entities and DAOs.
    *   `app/src/main/java/com/planszowsky/android/data/remote/`: Retrofit services. **Note:** `MockBggInterceptor.kt` is currently used to mock BGG responses.
*   **UI Layer:**
    *   `app/src/main/java/com/planszowsky/android/ui/screens/`: Composable screens (Collection, Details, Scanner, etc.).
    *   `app/src/main/java/com/planszowsky/android/ui/viewmodel/`: ViewModels managing UI state.
*   **Utilities:**
    *   `app/src/main/java/com/planszowsky/android/util/GameScannerAnalyzer.kt`: Core logic for CameraX image analysis.

## Building and Running

### Prerequisities
*   JDK 17
*   Android Studio (Latest)
*   Android SDK 36 (Preview/Beta support might be needed due to bleeding edge config)

### Commands
*   **Build Debug APK:**
    ```bash
    ./gradlew assembleDebug
    ```
*   **Run Tests:**
    ```bash
    ./gradlew test
    ```

## Development Status & Conventions

*   **BGG API:** Currently in **MOCK MODE**. The `MockBggInterceptor` in `AppModule.kt` intercepts requests to `boardgamegeek.com/xmlapi2/` and returns static data (Ticket to Ride, Catan, etc.).
    *   *To enable real API:* You would need to remove the interceptor and potentially handle the new BGG Auth requirements (though XMLAPI2 is usually public, recent changes might require a token or session cookie).
*   **Scanner:** The scanner uses a frame buffer to stabilize OCR results.
*   **Permissions:** Camera and Internet permissions are required and declared in `AndroidManifest.xml`.

## Common Tasks

*   **Add a new Screen:** Create a Composable in `ui/screens`, a ViewModel in `ui/viewmodel`, and add a route in `PlanszowskyNavHost.kt`.
*   **Modify Database:** Update `GameEntity`, modify `GameDao`, and increment database version in `AppDatabase` (handling migrations).
*   **Switch to Real API:** Remove `MockBggInterceptor()` from `AppModule` and ensure BGG API connectivity.
