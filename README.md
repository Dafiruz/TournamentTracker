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
| Re-roll decks / matches independently before starting | :white_check_mark: done |
| Set match winners, auto-advance, champion when the final finishes | :white_check_mark: done |
| Tournament statuses: In progress / Paused / Done, full history on home screen | :white_check_mark: done |
| Pause / Resume tournaments, delete with confirmation | :white_check_mark: done |
| Simple two-player life tracker (no commander damage, no poison) | :white_check_mark: done |
| Full-screen face-off life tracker: 50/50 split, top player rotated 180°, tap left = −1 / right = +1 | :white_check_mark: done |

## Bracket rules

| Players | Bracket |
| --- | --- |
| 2 | Finals only |
| 3 | Quarter Finals (1 match + 1 bye) → Finals |
| 4 | Semi Finals → Final |
| 5–8 | Quarter Finals (with byes) → Semis → Final |
| 9–16 | Round of 16 (with byes) → Quarters → Semis → Final |
| 17–32 | Round of 32 (with byes) → R16 → Quarters → Semis → Final |

Byes are assigned randomly. Pairings are shuffled to avoid mirror matches
(both players on the same deck) whenever possible.

## Tech

- Kotlin 2.0, Jetpack Compose (Material 3)
- Room for local persistence
- Navigation Compose
- Min SDK 26 (Android 8.0), target SDK 35
- Pure-Kotlin bracket/assignment logic with JUnit unit tests

## Build

Requires JDK 17 and the Android SDK. Open the project in Android Studio, or
from the command line (Windows):

```bash
set JAVA_HOME=<path-to-jdk-17>
gradlew.bat assembleDebug
```

Run the unit tests:

```bash
gradlew.bat testDebugUnitTest
```

Install on a connected device/emulator:

```bash
gradlew.bat installDebug
```
