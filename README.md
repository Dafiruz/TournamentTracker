# Tournament Tracker

An Android-only app for running Magic: The Gathering tournaments with randomized
deck assignments and brackets. Built with Kotlin and Jetpack Compose.

## Features

| Feature | Status |
| --- | --- |
| Add player names and deck names per tournament | :construction: planned |
| Randomize deck assignment per player (battlebox style, duplicates allowed when players > decks) | :construction: planned |
| Single-elimination brackets: 2, 3, 4, 8, 16 or 32 players, with random byes | :construction: planned |
| Match pairing avoids mirror matches (same deck) whenever possible | :construction: planned |
| Set match winners, auto-advance, champion when the final finishes | :construction: planned |
| Tournament statuses: In progress / Paused / Done, full history on home screen | :construction: planned |
| Simple two-player life tracker (no commander damage, no poison) | :construction: planned |
| Project skeleton (Gradle, Compose, theme, app icon) | :white_check_mark: done |

## Tech

- Kotlin 2.0, Jetpack Compose (Material 3)
- Room for local persistence
- Navigation Compose
- Min SDK 26 (Android 8.0), target SDK 35

## Build

Open the project in Android Studio, or:

```bash
./gradlew assembleDebug
```

Install on a connected device/emulator:

```bash
./gradlew installDebug
```
