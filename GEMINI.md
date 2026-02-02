# Planszowsky Context

## Project Overview

**Planszowsky** is a modern Android application designed for board game enthusiasts to manage their personal collections. It features a visually rich "juicy" UI with a masonry grid layout, ensuring game covers are the focal point.

### Key Features
*   **Collection Management:** Pinterest-style grid view of games.
*   **Smart Scanning:** Hybrid OCR (Text) and Barcode (EAN) scanner using Google ML Kit to quickly add games.
*   **Borrowing System:** Track games lent to friends.
*   **Randomizer:** "Slot machine" feature to pick a game to play.
*   **Expert AI:** "Ask the Expert" feature using Gemini 1.5 Flash to answer rule questions.
*   **Wishlist:** Separate list for wanted games.

## Technical Architecture

The project follows **Modern Android Development (MAD)** standards:

*   **Language:** Kotlin
*   **UI:** Jetpack Compose (Material 3)
*   **Architecture:** MVVM + Clean Architecture
*   **DI:** Hilt
*   **Database:** Room (SQLite)
*   **Network:** Retrofit + OkHttp + Jackson (for XML)
*   **AI:** Google AI SDK (Gemini 1.5 Flash)
*   **Image Loading:** Coil
*   **Camera/ML:** CameraX + ML Kit (Text Recognition, Barcode Scanning)

### Key Files & Directories

*   **Entry Points:**
    *   `app/src/main/java/com/planszowsky/android/PlanszowskyApp.kt`: Application class (Hilt setup).
    *   `app/src/main/java/com/planszowsky/android/MainActivity.kt`: Main activity hosting the Compose UI.
*   **AI Expert Module:**
    *   `app/src/main/java/com/planszowsky/android/ui/viewmodel/ExpertViewModel.kt`: Manages AI chat sessions.
    *   `app/src/main/java/com/planszowsky/android/ui/screens/ExpertChatBottomSheet.kt`: UI for the expert chat.
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
    *   `app/src/main/java/com/planszowsky/android/util/StringExt.kt`: String utilities (Levenshtein, OCR cleaning).

## Building and Running

### Prerequisities
*   JDK 17
*   Android Studio (Latest)
*   Android SDK 36 (Preview/Beta support might be needed due to bleeding edge config)

### API Setup
1.  **BGG API:** Currently in **MOCK MODE**.
2.  **Gemini AI:** 
    *   Create a key at [Google AI Studio](https://aistudio.google.com/).
    *   Add `GEMINI_API_KEY=your_key` to `local.properties`.

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
*   **Scanner:** Uses 'Central Bias' scoring and frame buffer to stabilize OCR results.
*   **Fuzzy Logic:** `StringExt.kt` contains Levenshtein distance implementation for future fuzzy search improvements.

## Production Migration Path (AI Expert)

When moving to a production/paid model:
1.  **Security:** Migrate from `Google AI SDK` to **Firebase Vertex AI** to enable **App Check** (protection against API key theft).
2.  **Monetization:** Implement In-App Purchases (Google Play Billing) to gate the `showChat` state in `DetailsScreen`.
3.  **Limits:** Monitor quotas in Google Cloud Console.

## Common Tasks

*   **Add a new Screen:** Create a Composable in `ui/screens`, a ViewModel in `ui/viewmodel`, and add a route in `PlanszowskyNavHost.kt`.
*   **Modify Database:** Update `GameEntity`, modify `GameDao`, and increment database version in `AppDatabase` (handling migrations).
*   **Switch to Real API:** Remove `MockBggInterceptor()` from `AppModule` and ensure BGG API connectivity.
