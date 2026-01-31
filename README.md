# Planszowsky 🎲

Planszowsky is a modern Android application for managing your personal board game collection. Designed for enthusiasts, it features a "juicy" and immersive interface that puts game covers front and center.

## ✨ Features

- **Your Collection (Masonry Grid):** Browse your games in a dynamic, Pinterest-style layout with full-size covers.
- **Title and Barcode Scanner:** Quickly add games to your database by scanning the title from the box (OCR) or the EAN barcode using Google ML Kit.
- **BGG Integration:** Connected with the BoardGameGeek database (fetch descriptions, player counts, and playtimes).
- **Game Details:** Full view of game information with smooth transitions and metadata chips.
- **Borrowing System:** Manage your collection by marking games as borrowed. Add notes about who has the game and track status on the main list.
- **What Shall We Play? (Randomizer):** Can't decide? Use the built-in "slot machine" (🎰) to pick a random title from your collection.
- **Wishlist:** Save games you want to buy in a separate, clear section.
- **BGG Import:** Quickly transfer your collection from the BoardGameGeek service.

## 🛠️ Technology

The project uses the latest Android development standards (Modern Android Development):

- **Language:** Kotlin
- **UI:** Jetpack Compose (Declarative UI)
- **Architecture:** MVVM (Model-View-ViewModel) + Clean Architecture
- **Dependency Injection:** Hilt
- **Database:** Room (Local collection storage)
- **Networking:** Retrofit 3 + OkHttp 5 (BGG API support)
- **Image Analysis:** CameraX + Google ML Kit (OCR & Barcode Scanning)
- **Images:** Coil (Asynchronous cover loading)
- **Design:** Material 3 (Material You) with full Dark Mode support.

## 🚀 Getting Started

1. Clone the repository.
2. Open the project in the latest version of **Android Studio**.
3. (Optional) Add your BoardGameGeek API key in `AppModule.kt`. Currently, the app uses a `Mock` mode for easier UI testing.
4. Build and run on an emulator or physical device with Android 8.0+.

## ⚖️ Legal and Licensing

### License
This project is licensed under the **Apache License 2.0**. See the [LICENSE](LICENSE) file for more details.

### Data and Attribution
- **BoardGameGeek:** This application uses data from [BoardGameGeek.com](https://boardgamegeek.com). All game titles, descriptions, and metadata are the property of their respective owners and BGG.
- **Game Covers:** Board game cover images are property of their respective publishers and designers. This app displays them for informational and cataloging purposes only (Fair Use).
- **ML Kit:** Image processing and scanning are powered by Google ML Kit. All processing happens locally on the device.

### Disclaimer
Planszowsky is an unofficial fan project and is not affiliated with or endorsed by BoardGameGeek.

---
Created with ❤️ for the board gaming community.