# Planszowsky 🎲

Planszowsky to nowoczesna aplikacja na Androida służąca do zarządzania osobistą kolekcją gier planszowych. Stworzona z myślą o pasjonatach, oferuje "soczysty" i imersyjny interfejs, który stawia okładki gier na pierwszym miejscu.

## ✨ Funkcje

- **Twoja Kolekcja (Masonry Grid):** Przeglądaj swoje gry w dynamicznym, pinterestowym układzie z pełnowymiarowymi okładkami.
- **Skaner Tytułów i Kodów Kreskowych:** Szybko dodawaj gry do bazy, skanując ich tytuł z pudełka (OCR) lub kod EAN (Barcode) przy użyciu Google ML Kit.
- **Wyszukiwarka BGG:** Integracja z bazą BoardGameGeek (pobieranie opisów, statystyk graczy i czasu rozgrywki).
- **Szczegóły Gry:** Pełny podgląd informacji o grze z płynną animacją przejść i metadanymi.
- **Co zagramy? (Randomizer):** Nie wiesz, co wybrać? Użyj wbudowanej "maszyny losującej" (🎰), aby wylosować tytuł z Twojej kolekcji.
- **Wishlist:** Zapisuj gry, które chcesz kupić w przyszłości, w osobnej, czytelnej sekcji.
- **Import z BGG:** Opcja szybkiego przeniesienia swojej kolekcji z serwisu BoardGameGeek.

## 🛠️ Technologia

Projekt wykorzystuje najnowsze standardy tworzenia aplikacji na Androida (Modern Android Development):

- **Język:** Kotlin
- **UI:** Jetpack Compose (Deklaratywny interfejs użytkownika)
- **Architektura:** MVVM (Model-View-ViewModel) + Clean Architecture
- **Wstrzykiwanie zależności:** Hilt
- **Baza danych:** Room (Lokalne przechowywanie kolekcji)
- **Sieć:** Retrofit 2 + OkHttp (Obsługa API BGG)
- **Analiza Obrazu:** CameraX + Google ML Kit (OCR & Barcode Scanning)
- **Obrazy:** Coil (Asynchroniczne ładowanie okładek)
- **Design:** Material 3 (Material You) z pełnym wsparciem dla Dark Mode.

## 🚀 Uruchomienie

1. Sklonuj repozytorium.
2. Otwórz projekt w najnowszej wersji **Android Studio**.
3. (Opcjonalnie) Dodaj swój klucz API BoardGameGeek w `AppModule.kt`. Obecnie aplikacja korzysta z trybu `Mock` dla ułatwienia testowania interfejsu.
4. Zbuduj i uruchom na emulatorze lub fizycznym urządzeniu z systemem Android 8.0+.

## ⚖️ Licencja i Dane

Aplikacja korzysta z danych udostępnianych przez serwis **BoardGameGeek.com**. Wszystkie informacje o grach, ich opisy oraz niektóre grafiki są własnością ich twórców oraz serwisu BGG. Projekt ma charakter hobbystyczny/lokalnego katalogu.

---
Stworzone z ❤️ dla społeczności graczy planszowych.
