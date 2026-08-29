# KRON Android — Kotlin Port

An Android/Kotlin port of the original KRON schedule application.

This repository is a fork used to rebuild the existing application for Android while preserving the original app's structure, behavior, and overall user experience as closely as practical.

## Project Status

**In development**

The current focus is on establishing a clean Kotlin/Android codebase and bringing the Android version to feature parity with the original application.

## Goals

- Recreate the original KRON application for Android.
- Keep the Android project structure easy to compare with the original application.
- Preserve the existing user flow and core schedule functionality.
- Use native Android/Kotlin tooling.
- Improve Android-specific behavior where appropriate without unnecessarily changing the original design.

## Tech Stack

- **Kotlin**
- **Android Studio**
- **Android SDK**
- **Gradle**

Additional libraries and architectural decisions will be documented as the Android implementation develops.

## Project Structure

The Android project is organized to keep responsibilities separated and make the implementation easy to navigate.

```text
app/
└── src/
    └── main/
        ├── java/          # Kotlin source code
        ├── res/           # Layouts, drawables, strings and other Android resources
        └── AndroidManifest.xml
```

The structure may evolve as features are implemented.

## Getting Started

### Requirements

- Android Studio
- A compatible Android SDK
- JDK supported by the project's Gradle version
- Android emulator or physical Android device

### Run locally

1. Clone the fork:

```bash
git clone https://github.com/MustafaQassmieh01/kron-android.git
cd kron-android
```

2. Open the project in **Android Studio**.
3. Allow Gradle to sync and install any required SDK components.
4. Select an emulator or connected Android device.
5. Run the `app` configuration.

## Development Approach

The port is being developed incrementally:

1. Establish the Android/Kotlin project structure.
2. Recreate the main screens and navigation.
3. Port the original application's data and scheduling logic.
4. Match the original UI and interactions.
5. Test behavior against the original application.
6. Refine Android-specific UX and edge cases.

## Current Work

- Kotlin project setup
- Android screen structure
- Navigation
- Porting existing schedule functionality
- Matching the original application's behavior and layout

## Fork / Attribution Note

This repository is an Android port based on an existing project.

The fork is used to develop and test the Kotlin implementation independently without affecting the original repository. Where appropriate, the original project's behavior and design are retained while the implementation is adapted for Android.

## Roadmap

- [ ] Verify project builds cleanly in Android Studio
- [ ] Complete primary navigation
- [ ] Port all main screens
- [ ] Implement schedule data handling
- [ ] Match original interactions and visual behavior
- [ ] Add loading and error states
- [ ] Test on multiple Android screen sizes
- [ ] Clean up architecture and documentation
- [ ] Prepare a stable Android release

## License

This fork follows the licensing terms of the original repository. See the repository's license file for details.
