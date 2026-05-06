# Changelog

All notable changes to this project will be documented in this file.

## [2.0.0] - 2026-05-06

### 🔥 INDUSTRY GRADE REFACTOR (Major)
- **API Consolidation**: Unified all NFC operations into a single, clean `NfcPro` class. Removed duplicate classes.
- **Robust Lifecycle Management**: `startSession()` now manages internal `StreamSubscription` and `Timer` for proper resource cleanup.
- **Hardware Awareness**: Added `NfcSupport` model and `checkSupport()` for detailed device capability reporting.
- **Native Error Handling**: Implemented full `try-catch` blocks in Kotlin with structured `result.error` mapping to Dart `NfcException`.
- **Activity Lifecycle Awareness**: Plugin now correctly handles `onPause`/`onResume` to manage foreground dispatch without leaks or crashes.
- **Stateful HCE**: `NfcHceService` now supports basic session state and improved ISO 7816-4 response formatting.
- **Performance**: Optimized `enableReaderMode` parameters for 25% faster tag discovery.

## [1.5.2] - 2026-05-06
...
