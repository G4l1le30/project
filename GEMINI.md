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
    *   The project requires a `google-services.json` file in the `app/` directory to connect to Firebase.
    *   You will also need a Google Maps API key configured in your `local.properties` file or directly in the `AndroidManifest.xml`.

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

## Feature Specific Notes

*   **Distinction between "Jasa" (Services) and "Benda" (Goods):**
    *   **"Jasa" (Services):** Based on `PAPBBBB.pdf`, the primary interaction for services is focused on displaying profile information and contact details, implying a discovery and direct contact model rather than an in-app ordering/cart flow. The current implementation in `DetailScreen.kt` reflects this by not offering an "Add to Cart" option for services. It has been noted that the explicit "kontak" (contact) information for services is currently missing in the UI and should be added in future iterations.
    *   **"Benda" (Goods/Makanan):** For physical goods like food items, `PAPBBBB.pdf` indicates an in-app ordering process through a cart. The current codebase supports this by allowing `MenuItem`s to be added to the cart via `DetailScreen.kt` and managed in `CartViewModel.kt`.