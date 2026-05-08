# NFC Pro Manager 🚀

[![pub package](https://img.shields.io/pub/v/nfc_pro_manager.svg)](https://pub.dev/packages/nfc_pro_manager)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](https://opensource.org/licenses/MIT)

A robust Flutter SDK designed for advanced NFC use cases: payment simulation, smart card communication (ISO-DEP/APDU), and Host Card Emulation (HCE). Built to handle real-world reliability issues like data loss during app startup and connection drops.

## ❓ Why Nfc Pro Manager?

Most Flutter NFC plugins are limited to basic NDEF tag reading. This SDK goes further by providing:

- **Full ISO-DEP / APDU support** for complex smart card interactions.
- **Native Host Card Emulation (HCE)** to turn your phone into a smart card.
- **Persistent Sessions** that keep connections alive for multi-command workflows (crucial for payment).
- **Event Buffering** which prevents missing card detections during app initialization.

## ⚡ Quick Start

```dart
await NfcPro.startSession(
  onDiscovered: (tag) => print("Card UID: ${tag.uid}"),
);
```

## 🆚 Comparison

| Feature | nfc_manager | nfc_pro_manager |
|---------|:-----------:|:---------------:|
| Raw APDU Commands | ❌ | ✅ Full Support |
| Card Emulation (HCE) | ❌ | ✅ Supported |
| Persistent ISO-DEP | ⚠️ Basic | ✅ Optimized |
| Lifecycle Management | ❌ | ✅ Auto-Suspend |
| Event Buffering | ❌ | ✅ Native Queue |

## 📖 Usage Examples

### Reading a Smart Card (APDU)
Interact with banking cards, e-money, or access cards using batch commands:
```dart
List<String?> responses = await NfcPro.runScript([
  NfcApdu.selectAid("F0010203040506"),
  NfcApdu.readBinary(sfi: 1, offset: 0),
]);
```

### Identity Emulation (HCE)
Turn your Android device into a programmable NFC tag:
```dart
await NfcPro.setEmulationId("MY-IDENTITY-TOKEN");
```

### Real Output Example
```text
UID: 04A224B1C39280
Type: android.nfc.tech.IsoDep, android.nfc.tech.NfcA
Content: Secure Identity Token v3
```

## 💡 Common Use Cases
- 💳 **E-money reader** for balance checking.
- 🏢 **Digital access cards** for office or gym entry.
- 🔐 **Secure login** via physical NFC tokens.
- 📇 **Smart identity cards** for attendance systems.

## 🎖 Platform Support & Limitations

| Feature | Android | iOS |
|---------|:-------:|:---:|
| Tag Discovery | ✅ | ✅ |
| APDU (ISO-DEP) | ✅ | ✅ (ISO7816 only) |
| HCE (Emulation) | ✅ | ❌ (Apple Lock) |
| NDEF Write | ✅ | ❌ |

### ⚠️ iOS CoreNFC Limitations:
- **No HCE**: Apple does not allow third-party apps to emulate cards.
- **Scanning UI**: iOS shows a mandatory system modal during scanning.
- **Background**: Very limited background scanning compared to Android.

## 🔧 Installation & Setup

### Prerequisites
- **Flutter**: >= 3.0.0
- **Android SDK**: API 30 or higher (automatically enforced by plugin)
- **iOS**: iOS 12.0+

### Installation

Just add to your `pubspec.yaml`:
```yaml
dependencies:
  nfc_pro_manager: ^3.1.3
```

Run `flutter pub get` and you're done! ✅

The plugin automatically enforces **minSdkVersion 30** on Android. No manual configuration needed!

### ✅ Compatibility Matrix

| Flutter Version | Dart | Android SDK | Status |
|-----------------|------|-------------|--------|
| 3.0.x - 3.5.x   | >=3.0| 30+ | ✅ Supported |
| 4.0.x+          | >=3.3| 30+ | ✅ Supported |

> **Note**: Pure Kotlin/Java implementation - no NDK required!

## 📄 License
MIT License. Created with ❤️ for the Flutter community.
