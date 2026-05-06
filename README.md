# NFC Pro Manager 🚀 (Version 2.2.0)

The enterprise-grade NFC SDK for Flutter. Engineered for low-latency communication, advanced **ISO-DEP (APDU)** interaction, and **Identity Emulation (HCE)**.

---

## 🏗 Architecture Overview

```text
[ Flutter Application ]
          |
    [ NfcPro SDK ]  <--- (Session State Machine)
          |
   [ Platform Channel ]
    /            \
[ Android ]    [ iOS ]
    |             |
[HCE / IsoDep] [CoreNFC*]
```
*\*iOS support is currently limited to NDEF Read/Write. HCE is Android-only.*

---

## 🔥 Professional Features (v2.2.0)

-   **Session State Machine**: Track lifecycle via `NfcPro.state` (idle, active, processing, etc).
-   **NfcApdu Builder**: Simplified APDU command construction (SELECT AID, READ BINARY).
-   **Hardware Stability**: Native debouncing and 5-second hardware timeouts.
-   **UX Helpers**: `NfcPro.openSettings()` to guide users when NFC is disabled.
-   **Deterministic Lifecycle**: Fixed race conditions in event dispatching.

---

## 📦 Installation

```yaml
dependencies:
  nfc_pro_manager: ^2.2.0
```

---

## 🚀 Quick Start

```dart
// 1. Check & Open Settings
final support = await NfcPro.checkSupport();
if (!support.isAvailable) {
  await NfcPro.openSettings();
}

// 2. Start Secure Session
await NfcPro.startSession(
  timeout: Duration(seconds: 10),
  onDiscovered: (tag) async {
    print("Tag: ${tag.uid}");
    
    // 3. Use Apdu Builder
    final command = NfcApdu.selectAid("F0010203040506");
    final response = await NfcPro.transceive(command);
  },
);
```

---

## 🔧 Platform Support Matrix

| Feature | Android | iOS |
| :--- | :---: | :---: |
| NDEF Read/Write | ✅ | ✅ |
| ISO-DEP (APDU) | ✅ | 🚧 (Experimental) |
| HCE (Emulation) | ✅ | ❌ (OS Limit) |
| Session Timeout | ✅ | ✅ |

---

## 🔄 Versioning Strategy
- **v2.x.x**: Stable Enterprise SDK.
- **v3.x.x**: (Planned) Advanced iOS CoreNFC integration.

---

## 📄 License
MIT License.
