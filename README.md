# Siblings or Dating?

An Android game where you guess whether two people in a photo are siblings or dating.

## Features

- Guess relationship from photos
- Upload and tag your own photos
- Shake to skip
- Haptic feedback on answers
- Clean UI with Jetpack Compose and Material 3

## Tech Stack

- Kotlin
- Jetpack Compose + Material 3
- Room (SQLite) for metadata, internal storage for images
- Min SDK: 24 (Android 7.0)
- Target SDK: 34 (Android 14)

## Building

```bash
./gradlew assembleDebug
```

APK output: `app/build/outputs/apk/debug/`

## Adding Photos

Tap the + button to upload a photo and tag it as "siblings" or "dating".
