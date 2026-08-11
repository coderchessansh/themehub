# ThemeHub Android

This folder is a real Android application project that wraps ThemeHub's HTML/CSS/JS in a WebView.

## Build

### Android Studio
1. Open the `android` folder as a project.
2. Let Gradle sync.
3. Select the `app` configuration.
4. Build the debug APK with **Build > Build APK(s)**.
5. The APK is under `android/app/build/outputs/apk/debug/`.

### AndroidIDE / another Android Gradle IDE
Open the `android` folder as a Gradle project and run the `app:assembleDebug` Gradle task.

## Safety

The app only declares the `INTERNET` permission because the current ThemeHub UI may load online fonts/resources. It does not request contacts, SMS, location, microphone, camera, or storage permissions.

The app loads its UI from `app/src/main/assets/index.html`; inspect that file and the Kotlin source before building if you want to verify the code yourself.
