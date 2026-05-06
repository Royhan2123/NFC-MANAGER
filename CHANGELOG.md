# Changelog

All notable changes to this project will be documented in this file.

## [1.0.4] - 2026-05-06

### Changed
- Total cleanup of native code. Deleted all unused Activities, Layouts, and Resources.
- Finalized "Headless" architecture: Native code is now exclusively for NFC bridge logic.
- Production-ready stable release.

## [1.0.3] - 2026-05-06

### Removed
- Removed all native Android Activities to prevent UI/Manifest conflicts with Flutter host apps.
- The plugin is now headless, focusing purely on NFC core logic and background services.

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
- Host Card Emulation (HCE) support for Android.
- Card Cloning functionality (Read -> Emulate).
- NDEF Tag Writer for physical tags.
