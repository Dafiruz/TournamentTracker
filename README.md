# Tournament Tracker

An Android-only app for running Magic: The Gathering tournaments with randomized
deck assignments and brackets. Built with Kotlin and Jetpack Compose.

## Features

| Feature | Status |
| --- | --- |
| Add player names and deck names per tournament | :white_check_mark: done |
| Randomize deck assignment per player (battlebox style, duplicates allowed when players > decks) | :white_check_mark: done |
| Single-elimination brackets: 2, 3, 4, 8, 16 or 32 players, with random byes | :white_check_mark: done |
| Match pairing avoids mirror matches (same deck) whenever possible | :white_check_mark: done |
| Set match winners, auto-advance, champion when the final finishes | :white_check_mark: done |
| Tournament statuses: In progress / Paused / Done, full history on home screen | :white_check_mark: done |
| Simple two-player life tracker (no commander damage, no poison) | :construction: planned |
| Project skeleton (Gradle, Compose, theme, app icon) | :white_check_mark: done |
| Room database: tournaments, players, decks, matches (with byes) | :white_check_mark: done |

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
