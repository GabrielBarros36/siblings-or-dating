# Siblings or Dating? 👫💑

An Android guessing game where you look at a photo of two people and guess whether they're **siblings** or **dating**!

## Features

- 🎮 **Core Gameplay** — See a photo, tap "Siblings" or "Dating", and find out if you're right
- 📸 **Upload Your Own** — Add photos tagged as siblings or dating for others to guess
- 📳 **Shake to Skip** — Shake your phone to skip to a different photo
- 📱 **Haptic Feedback** — Feel a vibration when you make your guess
- 🎨 **Clean UI** — Built with Jetpack Compose and Material 3

## Tech Stack

- **Language:** Kotlin
- **UI:** Jetpack Compose + Material 3
- **Storage:** Room (SQLite) for image metadata, internal storage for uploaded images
- **Min SDK:** 24 (Android 7.0)
- **Target SDK:** 34 (Android 14)

## Building

Open in Android Studio or build from command line:

```bash
./gradlew assembleDebug
```

The APK will be in `app/build/outputs/apk/debug/`.

## How to Play

1. A photo appears on screen
2. Tap **"Siblings"** or **"Dating"** to make your guess
3. The app reveals the correct answer
4. Swipe or tap to see the next photo
5. Shake your phone to skip!

## Adding Photos

Tap the **+** button to upload a photo from your gallery, then tag it as "siblings" or "dating". It'll be added to the game rotation.
