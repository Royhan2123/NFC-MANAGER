# NFC Pro Manager 🚀

The enterprise-grade NFC SDK for Flutter. Engineered for low-latency communication, advanced **ISO-DEP (APDU)** interaction, and **Identity Emulation (HCE)** with virtual AID routing.

---

## 🏗 Architecture Overview

```text
[ Flutter Application ]
          |
    [ NfcPro SDK ]  <--- (Strongly Typed API)
          |
   [ Platform Channel ]
    /            \
[ Android ]    [ iOS ]
    |             |
[HCE / IsoDep] [CoreNFC]
    |             |
[   Physical Hardware  ]
```

---

## 📦 Installation

Add this to your `pubspec.yaml`:
```yaml
dependencies:
  nfc_pro_manager: ^1.5.2
```

---

## 🚀 Quick Start

Get up and running in seconds:
```dart
import 'package:nfc_pro_manager/nfc_pro_manager.dart';

// Start a simple session
await NfcPro.startSession(
  onDiscovered: (tag) {
    print("UID Found: ${tag.uid}");
  },
);
```

---

## 📖 SDK Features

*   **Session Lifecycle Management**: Robust `startSession` and `stopSession` with built-in **Timeout Control**.
*   **Identity Emulation (HCE)**: Securely emulate virtual smart cards via Android Host Card Emulation.
*   **ISO-DEP / APDU Transceive**: Direct low-level communication with Smart Cards at native speeds.
*   **Strongly Typed Engine**: All tag data is returned as structured `NfcTag` models.
*   **SDK Helpers**: Professional utilities like `NfcUtils.buildSelectAid()` and response validation.

---

## 📡 NFC Session Flow

```text
[ Start Session ] 
      ↓
[ Wait for Tag ] <--- (Hardware Level)
      ↓
[ Tag Discovered ] 
      ↓
[ Process (NDEF / APDU) ]
      ↓
[ Complete / Stop Session ]
```

---

## 🔧 Common Errors & Solutions

| Error Type | Meaning | Possible Solution |
| :--- | :--- | :--- |
| `notSupported` | Device lacks NFC hardware. | Check device specs or use `isAvailable()`. |
| `disabled` | NFC is turned off in settings. | Prompt user to enable NFC. |
| `timeout` | No tag detected within duration. | Increase timeout or retry session. |
| `connectionLost` | Tag moved too quickly. | Ask user to hold the tag steadily. |

---

## ⚡ Performance Metrics
- **Discovery Latency**: < 250ms.
- **APDU Round-trip**: 50ms - 150ms.
- **Disclaimer**: *Performance may vary depending on device hardware and NFC chip quality.*

---

## 🔄 Versioning Strategy
- **v1.x.x**: Stable API. Focus on bug fixes and performance optimization.
- **v2.x.x**: (Future) Planned breaking changes to support Multi-session HCE.

---

## 📄 License
MIT License.

_Engineered for reliability. Built for the Flutter Community._
