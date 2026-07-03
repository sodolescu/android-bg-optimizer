# Android Background Optimizer — Native Kotlin App

A high-performance Android application that implements the **Golden Ratio (φ ≈ 1.618) cache eviction algorithm** for intelligent background process management and memory optimization.

## Overview

The Android Background Optimizer uses the mathematical constant **φ (phi)** to partition the device's cache into two segments:

- **HOT Segment**: 61.8% of total capacity — holds frequently accessed processes
- **COLD Segment**: 38.2% of total capacity — holds recently admitted processes

This partition mirrors the mathematical property of the Golden Ratio: the ratio of the whole to the larger part equals the ratio of the larger part to the smaller part.

### Algorithm Features

- **Two-Tier Eviction**: New processes enter COLD. A second access promotes them to HOT.
- **LRU Demotion**: When HOT is full, the least-recently-used item demotes to COLD.
- **LRU Eviction**: When COLD is full, the least-recently-used item is evicted.
- **Real-Time Metrics**: Live hit rate, memory pressure, promotion/demotion counts, and eviction tracking.

## Project Structure

```
android-bg-optimizer-native/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/androidbgoptimizer/
│   │   │   │   ├── MainActivity.kt              # Main Compose UI
│   │   │   │   ├── cache/
│   │   │   │   │   └── GoldenRatioCache.kt      # Core eviction engine
│   │   │   │   └── ui/theme/
│   │   │   │       ├── Theme.kt                 # Material Design 3 theme
│   │   │   │       └── Type.kt                  # Typography
│   │   │   ├── res/
│   │   │   │   ├── values/
│   │   │   │   │   ├── strings.xml
│   │   │   │   │   └── themes.xml
│   │   │   │   └── AndroidManifest.xml
│   │   │   └── ...
│   │   ├── test/                                # Unit tests
│   │   └── androidTest/                         # Instrumented tests
│   ├── build.gradle.kts                         # App-level build config
│   └── proguard-rules.pro                       # ProGuard rules
├── build.gradle.kts                             # Project-level build config
├── settings.gradle.kts                          # Gradle settings
└── README.md
```

## Tech Stack

- **Language**: Kotlin 1.9.21
- **UI Framework**: Jetpack Compose with Material Design 3
- **Minimum SDK**: Android 8.0 (API 26)
- **Target SDK**: Android 14 (API 34)
- **Build System**: Gradle 8.2.0

## Building the APK

### Prerequisites

1. **Android Studio** (latest version) or **Android SDK Command-Line Tools**
2. **Java Development Kit (JDK) 11+**
3. **Gradle 8.2.0+** (included with Android Studio)

### Step 1: Clone or Download the Project

```bash
cd android-bg-optimizer-native
```

### Step 2: Build the APK

#### Using Android Studio (Recommended)

1. Open the project in Android Studio
2. Click **Build** → **Build Bundle(s) / APK(s)** → **Build APK(s)**
3. The APK will be generated at `app/build/outputs/apk/debug/app-debug.apk`

#### Using Gradle Command Line

```bash
# Debug APK (for testing)
./gradlew assembleDebug

# Release APK (optimized, requires signing)
./gradlew assembleRelease
```

The APK files will be located in:
- Debug: `app/build/outputs/apk/debug/app-debug.apk`
- Release: `app/build/outputs/apk/release/app-release.apk`

### Step 3: Install on Device or Emulator

```bash
# Install debug APK
adb install app/build/outputs/apk/debug/app-debug.apk

# Or use Android Studio's Run button
```

## Features

### Dashboard

- **Real-Time Metrics**: Hit rate, memory pressure, process counts, eviction statistics
- **Process Registry**: View all cached processes, sorted by segment (HOT/COLD)
- **Interactive Controls**: 
  - Add random processes
  - Touch (access) individual processes
  - Force evict processes
  - Auto-simulate mode with adjustable speed

### Visualizations

- **Segment Utilization Bars**: Visual representation of HOT/COLD capacity usage
- **Golden Ratio Partition**: Pie chart showing the φ-based partition
- **Event Log**: Real-time log of cache operations (ADMIT, PROMOTE, DEMOTE, EVICT)
- **Live Metrics Panel**: Comprehensive statistics sidebar

### Simulation

- **Auto-Simulate Mode**: Continuously generates realistic app access patterns
- **Adjustable Speed**: 1×, 2×, 4×, 8× simulation speeds
- **Manual Controls**: Pause/resume simulation, add apps on demand

## Usage

1. **Launch the app** on your Android device or emulator
2. **Click "Add Random App"** to admit processes into the cache
3. **Watch the metrics** update in real-time as processes are accessed, promoted, demoted, or evicted
4. **Toggle "Simulate"** to enable auto-simulation with realistic app access patterns
5. **Adjust simulation speed** using the speed controls
6. **View the event log** to see all cache operations

## Architecture

### GoldenRatioCache Engine

The core eviction logic is implemented in `GoldenRatioCache.kt`:

```kotlin
class GoldenRatioCache(val totalCapacityMB: Int = 2048) {
    fun computeCapacities(): Pair<Int, Int>  // Calculate HOT/COLD sizes
    fun admitProcess(process: CacheProcess): Boolean
    fun touchProcess(processId: String): Boolean
    fun evictProcess(processId: String): Boolean
    fun getStats(): CacheStats
    fun getHotProcesses(): List<CacheProcess>
    fun getColdProcesses(): List<CacheProcess>
}
```

### UI Architecture

The UI is built entirely with **Jetpack Compose** using a composable hierarchy:

- `DashboardScreen()` — Main state management and simulation loop
- `HeaderBar()` — Top navigation with stats and controls
- `LeftSidebar()` — Process registry (HOT/COLD grouping)
- `CenterContent()` — Main dashboard with charts and metrics
- `RightSidebar()` — Live metrics panel

## Customization

### Change Cache Capacity

Edit `MainActivity.kt`:

```kotlin
val cache = remember { GoldenRatioCache(4096) }  // 4 GB instead of 2 GB
```

### Add Custom Apps

Edit the `mockApps` list in `DashboardScreen()`:

```kotlin
val mockApps = listOf(
    CacheProcess("app_id", "App Name", "icon", memoryMB, "COLD"),
    // ... more apps
)
```

### Adjust Colors

Edit `MainActivity.kt` color values (e.g., `Color(0xFFC9A961)` for gold)

## Testing

### Run Unit Tests

```bash
./gradlew test
```

### Run Instrumented Tests

```bash
./gradlew connectedAndroidTest
```

## Release Build

To create a release APK for distribution:

```bash
./gradlew assembleRelease
```

**Note**: You'll need to sign the APK with your release key. See [Android Documentation](https://developer.android.com/studio/publish/app-signing) for details.

## Performance

- **Lightweight**: ~5 MB APK size
- **Efficient**: Kotlin coroutines for non-blocking simulation
- **Responsive**: Material Design 3 with smooth animations
- **Battery-Friendly**: Optimized event loop and memory management

## Troubleshooting

### Build Fails with "SDK not found"

1. Install Android SDK via Android Studio
2. Set `ANDROID_HOME` environment variable:
   ```bash
   export ANDROID_HOME=~/Android/Sdk
   ```

### Gradle Sync Issues

1. Click **File** → **Sync Now** in Android Studio
2. Or run: `./gradlew clean && ./gradlew build`

### App Crashes on Launch

Check logcat for errors:

```bash
adb logcat | grep AndroidBGOptimizer
```

## License

MIT License — Feel free to use, modify, and distribute.

## Author

Built with ❤️ using Kotlin and Jetpack Compose

---

**φ = 1.618033988749895...**  
*The Golden Ratio — Nature's Perfect Partition*
