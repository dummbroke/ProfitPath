# ProfitPath: Trading Journal App

ProfitPath is a modern, mobile-first Trading Journal App built with Jetpack Compose and Kotlin, designed to help traders log, analyze, and optimize their trading performance. The app leverages a clean MVVM architecture, integrates with Firebase Firestore for cloud data storage, and uses local storage for screenshots, ensuring both accessibility and privacy.

## Features

- **Trade Entry:** Log new trades with mandatory fields, attach screenshots, and categorize by strategy.
- **Trade History:** View, filter, and search past trades with win/loss status and timestamps.
- **Trade Detail:** Inspect individual trades, review notes, and view attached screenshots.
- **Performance Analytics:** Visualize win/loss ratios, monthly trends, and strategy effectiveness with interactive charts.
- **Settings:** Customize the app theme (TradingView-inspired dark mode by default), manage backups, and adjust preferences.
- **Cloud Sync:** All trade data (except images) is stored in Firebase Firestore for secure, cross-device access.
- **Local Image Storage:** Screenshots are saved locally on the device, with only file paths stored in Firestore for privacy and efficiency.

## Architecture

- **MVVM Pattern:**
  - **UI Layer:** Built with Jetpack Compose, organized into modular packages for each screen.
  - **ViewModel Layer:** Manages UI state and business logic using StateFlow or LiveData.
  - **Repository Layer:** Abstracts data operations, handling both Firestore and local storage.
- **Kotlin Coroutines:** Used for all asynchronous operations, ensuring smooth, non-blocking UI.
- **Material Design 3:** Provides a modern, accessible, and visually appealing interface.

## Project Structure

```
app/src/main/java/com/dummbroke/profitpath/
  ui/
    home/
    trade_entry/
    trade_history/
    trade_detail/
    performance/
    settings/
    theme/
```

Each feature folder contains:
- `Screen.kt` (Composable UI)
- `ViewModel.kt` (State & logic)
- `Repository.kt` (Data access)

## Setup Instructions

1. **Clone the repository:**
   ```sh
   git clone <your-repo-url>
   ```
2. **Open in Android Studio.**
3. **Sync Gradle:**
   - Ensure all dependencies are downloaded.
4. **Firebase Setup:**
   - Add your `google-services.json` to `app/`.
   - Configure your Firebase project for Firestore.
5. **Run the app on an emulator or device.**

## Tech Stack
- **Kotlin**
- **Jetpack Compose**
- **Firebase Firestore**
- **Kotlin Coroutines**
- **Material Design 3**
- **Timber (logging)**

## Contribution
Pull requests are welcome! Please follow Kotlin and Android best practices, and ensure all code is well-documented and tested.

## License
This project is licensed under the MIT License. 