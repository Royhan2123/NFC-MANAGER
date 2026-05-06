# Changelog

All notable changes to this project will be documented in this file.

## [2.1.0] - 2026-05-06

### Fixed
- **Race Condition**: Fixed `eventSink` being null during activity attachment.
- **Lifecycle Bug**: Corrected typo in `onReattachedToActivityForConfigChanges`.
- **Memory Safety**: Improved Activity reference management in `NfcController`.

### Added
- **Tag Debouncing**: Added native `lastUid` check to prevent double-reading tags.
- **Hardware Stability**: Added 5-second timeout to `IsoDep` communication to prevent UI hanging.
- **SDK Error Codes**: Added specific native error codes like `CONNECTION_LOST`.

## [2.0.0] - 2026-05-06
...
