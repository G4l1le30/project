# GEMINI.md

## Project Overview

This is an Android application named "UMKMami" built with Kotlin and Jetpack Compose. The app serves as a directory for local small and medium-sized enterprises (UMKM), allowing users to discover them through a list or a map interface. It features a splash screen, a main screen with list/map views, and a detail screen for each business.

The project uses the following main technologies:
*   **UI:** Jetpack Compose
*   **Asynchronous Operations:** Kotlin Coroutines
*   **Navigation:** Jetpack Navigation Compose
*   **Data:** Firebase Realtime Database for storing business information, menus, services, and reviews.
*   **Image Loading:** Coil
*   **Maps:** Google Maps Compose Library

The architecture seems to follow a standard Android pattern with a `repository` for data fetching (`UmkmRepository`), `ViewModel`s for business logic (`HomeViewModel`, `DetailViewModel`), and `Screen`s for UI (`HomeScreen`, `DetailScreen`).

## Building and Running

This is a standard Gradle-based Android project.

1.  **Prerequisites:**
    *   Android Studio
    *   Java Development Kit (JDK)
    *   Android SDK

2.  **Configuration:**
    *   The project requires a `google-services.json` file in the `app/` directory to connect to Firebase. This file must not be committed to version control.
    *   You will also need a Google Maps API key. Create a `local.properties` file in the project root and add `MAPS_API_KEY=YOUR_GOOGLE_MAPS_API_KEY`. An example is provided in `local.properties.example`.

3.  **Building the project:**
    *   Open the project in Android Studio.
    *   Gradle will automatically sync and download the required dependencies.
    *   To build the project, you can use the `Build > Make Project` option in Android Studio or run the following command in the terminal:
        ```bash
        ./gradlew build
        ```

4.  **Running the application:**
    *   You can run the app on an Android emulator or a physical device directly from Android Studio by selecting the `app` configuration and clicking the "Run" button.
    *   Alternatively, you can build an APK using the following command:
        ```bash
        ./gradlew assembleDebug
        ```
    *   Then, install the APK on a device/emulator:
        ```bash
        adb install app/build/outputs/apk/debug/app-debug.apk
        ```

5.  **Running tests:**
    *   To run unit tests:
        ```bash
        ./gradlew test
        ```
    *   To run instrumented tests:
        ```bash
        ./gradlew connectedAndroidTest
        ```

## Development Conventions

*   The project follows modern Android development practices, utilizing Kotlin, Coroutines, and the latest Jetpack libraries.
*   The UI is built entirely with Jetpack Compose, separating UI components into `screens` and `components` packages.
*   Data management is centralized in the `UmkmRepository`, which communicates with Firebase.
*   State management in the UI is handled by `ViewModel`s and collected as state in the composable functions.
*   The project uses a `libs.versions.toml` file in the `gradle` directory to manage dependencies, which is a recommended practice for modern Android projects.

## Recent Work and Implemented Improvements

### 1. Fixing Redundant API Calls (`Performa Aplikasi & Alur Logika Kode`)

A significant performance and logic flaw was identified where the `homeViewModel.loadRecommendedUmkm` function was being called multiple times unnecessarily on app startup and navigation. This was traced to a `LaunchedEffect` anti-pattern.

The issue was resolved by implementing an event-driven architecture in the `HomeViewModel` and `DetailViewModel`, ensuring that the recommendation loading logic is executed exactly once per relevant user action, eliminating redundant network calls and improving performance.

### 2. API Key Security Enhancement (`Potensi Risiko Keamanan`)

**Weakness:** The Google Maps API Key was hardcoded in `app/src/main/AndroidManifest.xml`, and the `google-services.json` file (containing Firebase keys) was not in `.gitignore`. This is a critical security risk.

**Solution:**
1.  **API Key Externalization:** The Google Maps API key was moved to the `local.properties` file.
2.  **Secure Injection:** The `app/build.gradle.kts` file was configured to read the key from `local.properties` and inject it into the manifest at build time using `manifestPlaceholders`.
3.  **Ignoring Sensitive Files:** The `google-services.json` and `local.properties.example` files were added to `.gitignore`.
4.  **Documentation:** A `local.properties.example` file was created to guide future developers.

### 3. Corrected WhatsApp Button Logic (`Alur Logika Kode yang Tidak Tepat`)

**Weakness:** For "Jasa" (Service) UMKM, both the call button and the WhatsApp button incorrectly used the `umkm.contact` field, which contained a local number format, causing the WhatsApp intent to fail.

**Solution:**
1.  The logic in `DetailScreen.kt` was corrected. The regular call button uses `umkm.contact`.
2.  The WhatsApp button now correctly uses the separate `umkm.whatsapp` field which contains the required international format.
3.  The WhatsApp button is now only displayed if the `umkm.whatsapp` field is available.

### 4. Review Form Input Validation (`Potensi Risiko Keamanan`)

**Weakness:** The "Leave a Review" form lacked input validation for length and whitespace, creating a risk of UI issues or spam.

**Solution:**
1.  **Max Length Validation:** A maximum length of 50 characters for the author name and 200 characters for the comment was implemented in `AddReviewForm`.
2.  **Whitespace Trimming:** The submit button's logic was updated to use `.trim().isNotBlank()` to invalidate comments with only spaces.
3.  **Button State:** The submit button is now correctly disabled if validation rules are not met.

### 5. Refactoring Asynchronous Code (`Penulisan Kode yang Kurang Baik`)

**Weakness:** Many functions in `UmkmRepository.kt` used a verbose `suspendCancellableCoroutine` pattern for Firebase calls, making the code hard to read.

**Solution:**
1.  All relevant data-fetching and data-setting functions in `UmkmRepository.kt` were refactored to use the modern `.await()` extension function from the `kotlinx-coroutines-play-services` library.
2.  This change significantly improved the readability, conciseness, and maintainability of the data layer by replacing legacy callbacks with a cleaner `try-catch` block idiomatic to Kotlin Coroutines.
