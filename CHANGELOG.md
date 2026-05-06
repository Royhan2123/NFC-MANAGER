# Changelog

All notable changes to this project will be documented in this file.

## [1.4.0] - 2026-05-06

### Improved
- **Developer Experience (DX)**: Full synchronization between README and Example project.
- **Example App**: Added "Secure Login" demo using APDU challenge-response flow.
- **UI Refinement**: Renamed "Cloning" to "Virtual Card Configuration" for professional compliance.
- **Hardware Status**: Added real-time NFC/HCE hardware capability indicators in the example app.
- **Documentation**: Added Architecture Diagram (Mermaid) to README.

## [1.3.0] - 2026-05-06

### Added
- **Hardware Verification**: Added `isAvailable()` and `supportsEmulation()` to check device capabilities.
- **Session Lifecycle**: Introduced `startSession()` and `stopSession()` for better resource management.
- **Enterprise Error Handling**: Added `NfcErrorType` enum for structured exception handling.

## [1.2.0] - 2026-05-06

### Refactored
- **Professional Terminology**: Renamed "Cloning" to "Identity Emulation" throughout the package.
- **Strongly Typed Models**: Replaced `Map` responses with structured `NfcTag` classes.

## [1.1.0] - 2026-05-06

### Added
- **APDU Transceive Support**: Added `transceive` method to send raw hex commands.

## [1.0.0] - 2026-05-05

### Added
- Initial release of NFC Pro Manager.
