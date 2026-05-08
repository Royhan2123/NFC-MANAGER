## 3.1.5
- Added Android-only MIFARE Classic support with raw block read/write APIs
- Exposed `readMifareClassic` and `writeMifareClassic` methods in the Flutter plugin
- iOS now returns a clear unsupported message for MIFARE Classic operations

## 3.1.4
- Removed unnecessary NDK version requirement (pure Kotlin implementation)
- Automatic minSdkVersion 30 enforcement
- Updated installation documentation for plug & play setup


## [3.1.3] - 2026-05-09

- **Fix settings.gradle.kts structure**
- **Remove unused resources**

## [3.1.2] - 2026-05-06

### 🛡️ STABILITY FIX RELEASE
- **Kotlin Compilation Fix**: Resolved `@Synchronized` annotation target error in `NfcController`.
- **Visibility Fix**: Updated `byteArrayToHexString` access modifier to `internal` for cross-class communication.
- **Thread Safety**: Implemented explicit `synchronized` blocks for buffer management.

## [3.1.1] - 2026-05-06
- **Technical Fix**: Resolved Gradle namespace conflict and manifest cleanup.

## [3.1.0] - 2026-05-06
- **The Human Perfection Release**: Overhauled documentation and cleaned up AI-traceability.
