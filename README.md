# WellLife

WellLife is an Android app for tracking everyday wellness habits. It brings water intake, sleep, physical activity, and personal goals into one dashboard so users can review their habits and follow their progress over time.

Built by **Group 10** for the **Android Mobile Application Development CAT 2** under the theme **Health and Wellness for All**.

## Features

| Feature | What it does |
| --- | --- |
| Dashboard | Shows wellness summaries and provides access to each tracking screen. |
| Hydration | Record water intake and review hydration progress. |
| Sleep | Log sleep duration and review previous entries. |
| Physical activity | Record exercise and review daily activity minutes. |
| Wellness goals | Set personal wellness targets. |
| Progress and history | Review wellness data from the past seven days. |

## How it works

- **Kotlin** is used for app logic.
- **Jetpack Compose and Material 3** provide the Android interface.
- **AndroidX ViewModel** manages screen state and exposes updates to the UI.
- **Room** stores wellness entries and goals locally, so saved data is available offline. The dashboard repository reads from the Room data access objects to build the wellness summaries.

## Run locally

1. Clone the repository.

   ```bash
   git clone https://github.com/Zacharia223/Welllife.git
   ```

2. Open the cloned `Welllife` folder in Android Studio and let Gradle finish syncing. Install the Android SDK requested by the project if prompted.
3. Select the `app` run configuration and an Android emulator or connected device. The app requires Android 7.0 (API 24) or later.
4. Click **Run** to launch the app.

## Project structure

The application code is in [`app/src/main/java/com/example/welllife`](app/src/main/java/com/example/welllife).

| Location | Responsibility |
| --- | --- |
| `MainActivity.kt` | App entry point, dashboard, and navigation between screens. |
| `data/` | Room database, entities, data access objects, and dashboard repository. |
| `ui/` | Feature screens, ViewModels, and Compose theme. |

## Contributors

Developed by Group 10. See the [GitHub contributors page](https://github.com/Zacharia223/Welllife/graphs/contributors) for contribution history.
