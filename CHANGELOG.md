# Changelog

All notable changes to this project will be documented in this file.

## [1.0.2] - 2026-05-06

### Improved
- Synchronized Android and iOS project configurations.
- Improved build compatibility with modern Flutter environments (NDK 27+).
- Refined manifest merger logic to prevent duplicate service declarations.

## [1.0.1] - 2026-05-06

### Fixed
- Fixed Android "Main Class Not Found" error by correctly structuring the package folders.
- Fixed iOS directory naming (renamed `ios_core` to `ios`).
- Added required `NfcProPlugin.swift` and `.podspec` for iOS support.
- Cleaned up `AndroidManifest.xml` to prevent build conflicts and double icons in host apps.

## [1.0.0] - 2026-05-05

### Added
- Initial release of NFC Pro Manager.
- **Host Card Emulation (HCE)** support for Android.
- **Card Cloning** functionality (Read -> Emulate).
- **NDEF Tag Writer** for physical tags.
- **Smart Data Parsing** for URLs, VCards, Emails, and Phones.
- **iOS-Inspired Design System** for native Android activities.
- **MethodChannel Bridge** for Flutter integration.
- **Real-time Event Stream** for tag discovery.
- **Example App** in Flutter demonstrating all core features.
旋
