# Quick Build Guide — Android Background Optimizer

## Option 1: Build Locally with Android Studio (Easiest)

### Requirements
- Download and install [Android Studio](https://developer.android.com/studio)
- Minimum 8 GB RAM recommended

### Steps

1. **Open the project in Android Studio**
   - File → Open → Select `android-bg-optimizer-native` folder

2. **Wait for Gradle sync to complete**
   - Android Studio will automatically download dependencies

3. **Build the APK**
   - Click **Build** menu → **Build Bundle(s) / APK(s)** → **Build APK(s)**
   - Or press `Ctrl+Shift+B` (Windows/Linux) or `Cmd+Shift+B` (Mac)

4. **Find the APK**
   - Path: `app/build/outputs/apk/debug/app-debug.apk`
   - Right-click → **Show in Folder** to locate it

5. **Install on device**
   - Connect Android device via USB (enable Developer Mode)
   - Click **Run** button in Android Studio
   - Or drag the APK onto the device

---

## Option 2: Build via Command Line (Gradle)

### Requirements
- Java Development Kit (JDK) 11 or higher
- Gradle 8.2.0+ (or use `./gradlew` wrapper included)

### Steps

```bash
# Navigate to project directory
cd android-bg-optimizer-native

# Build debug APK
./gradlew assembleDebug

# Or build release APK (requires signing)
./gradlew assembleRelease
```

**Output location:**
- Debug: `app/build/outputs/apk/debug/app-debug.apk`
- Release: `app/build/outputs/apk/release/app-release.apk`

---

## Option 3: Build in Cloud (GitHub Actions)

### Steps

1. **Create a GitHub repository** and push the project

2. **Create `.github/workflows/build.yml`:**

```yaml
name: Build APK

on: [push, pull_request]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-java@v3
        with:
          java-version: '11'
          distribution: 'temurin'
      - run: chmod +x gradlew
      - run: ./gradlew assembleDebug
      - uses: actions/upload-artifact@v3
        with:
          name: app-debug.apk
          path: app/build/outputs/apk/debug/app-debug.apk
```

3. **Push to GitHub** — APK will be built automatically and available as an artifact

---

## Installation Methods

### Method 1: Direct Installation (Easiest)

```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

### Method 2: Android Studio

1. Connect device via USB
2. Click **Run** button (green play icon)
3. Select your device
4. App installs automatically

### Method 3: Manual Installation

1. Copy APK to device
2. Open file manager on device
3. Tap the APK file
4. Follow installation prompts

---

## Troubleshooting

### "SDK not found"
```bash
# Set Android SDK path
export ANDROID_HOME=$HOME/Android/Sdk
export PATH=$PATH:$ANDROID_HOME/tools:$ANDROID_HOME/platform-tools
```

### "Gradle sync failed"
```bash
./gradlew clean
./gradlew build
```

### "Device not found"
```bash
# Check connected devices
adb devices

# Enable USB debugging on device:
# Settings → Developer Options → USB Debugging (toggle ON)
```

### "Installation failed"
```bash
# Uninstall previous version
adb uninstall com.example.androidbgoptimizer

# Reinstall
adb install app/build/outputs/apk/debug/app-debug.apk
```

---

## APK Details

| Property | Value |
|----------|-------|
| **Package Name** | `com.example.androidbgoptimizer` |
| **Min SDK** | Android 8.0 (API 26) |
| **Target SDK** | Android 14 (API 34) |
| **Size** | ~5-8 MB |
| **Architecture** | Universal (arm64-v8a, armeabi-v7a) |

---

## Next Steps

1. **Test the app** on your device
2. **Customize** colors, app list, or cache capacity (see README.md)
3. **Sign the release APK** for distribution
4. **Upload to Google Play Store** (requires developer account)

---

**Need help?** Check the full README.md for detailed documentation.
