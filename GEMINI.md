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

## Recent Work: Identifying and Fixing a Performance Weakness (Double API Calls for Recommendations)

### Identified Weakness

A significant performance and logic flaw was identified where the application was making redundant API calls to load recommendations. Specifically, the `homeViewModel.loadRecommendedUmkm` function was being called multiple times unnecessarily:
*   **On application startup (HomeScreen):** Triggered twice in quick succession.
*   **Upon navigating to a DetailScreen:** Triggered twice in quick succession.

This behavior led to inefficient resource usage (network, CPU, battery) and a flawed code logic flow, directly addressing the UAP criteria for `Performa aplikasi yang masih dapat ditingkatkan` (Application performance that can still be improved) and `Alur logika kode yang tidak tepat` (Incorrect code logic flow).

### Root Cause Analysis

The root cause was traced to a common anti-pattern in Jetpack Compose:
*   `LaunchedEffect` blocks in both `HomeScreen.kt` and `DetailScreen.kt` were configured to react to changes in state variables like `currentUser` and `umkm`.
*   During initial composition or when navigating, these state variables would often transition from an initial `null` state to their resolved values.
*   This state change, while necessary for UI updates, caused the `LaunchedEffect` to cancel and restart, leading to the `homeViewModel.loadRecommendedUmkm` function being invoked multiple times. This created a race condition where the recommendation loading logic was executed redundantly.

### Architectural Solution Implemented

To address this, an event-driven architectural pattern was applied, shifting the control of side-effects (API calls) from reactive `LaunchedEffect` triggers to explicit event handlers within the `ViewModel`. This ensures that API calls are made only once per relevant event, regardless of UI recompositions.

**Key Changes:**

1.  **`HomeViewModel.kt`:**
    *   Introduced a guard flag (`homeScreenRecommendationsLoaded`) and a new public event handler function (`onHomeScreenReady(uid: String?)`).
    *   This function ensures that the initial recommendation loading logic (`loadRecommendedUmkm`) is executed only once when the `HomeScreen` is ready and the user state is available.

2.  **`HomeScreen.kt`:**
    *   The `LaunchedEffect` was modified to simply call `homeVm.onHomeScreenReady(currentUser?.uid)`, thereby triggering the event in the ViewModel instead of directly invoking API calls.

3.  **`DetailViewModel.kt`:**
    *   The `loadUmkmDetails(umkmId: String)` function was refactored into a `suspend` function that returns the `Umkm?` object. This allows its caller to `await` its completion.

4.  **`DetailScreen.kt`:**
    *   The two separate `LaunchedEffect` blocks were replaced with a single `LaunchedEffect(umkmId)`.
    *   This single effect sequentially performs the following actions:
        *   Calls and `await`s the result of `detailViewModel.loadUmkmDetails(umkmId)`.
        *   Only after the UMKM details are successfully loaded, it proceeds to call secondary actions like `homeViewModel.recordCategoryView` and `homeViewModel.loadRecommendedUmkm`, and `detailViewModel.checkIfWishlisted`.
    *   This sequential flow guarantees that recommendation loading for the detail screen happens exactly once per unique UMKM visited.

### Verification and Diagnostic Logging

During the debugging process, extensive diagnostic `Log.d` statements were temporarily added to `HomeViewModel.kt`, `HomeScreen.kt`, and `DetailScreen.kt` to precisely trace the execution flow and identify the root cause of the redundant calls. These logs were instrumental in confirming the `LaunchedEffect` re-execution issue and validating the ViewModel stability. These diagnostic logs are included in the final provided code for further verification by the user.

### Conclusion

The applied changes ensure a robust, efficient, and architecturally sound data loading process for recommendations in both the home and detail screens, eliminating redundant API calls and adhering to best practices for Compose state management and side-effect handling. The project now exhibits improved performance and a corrected code logic flow.