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

## 🎯 Designed For
- **Enterprise Access Control**: Emulate employee badges and security keys.
- **Smart Card Research**: Deep-dive into APDU communication and response handling.
- **Authentication Systems**: Build secure challenge-response login flows.

---

## ⚡ Performance Metrics
- **Discovery Latency**: < 250ms from physical contact to Dart event.
- **APDU Round-trip**: 50ms - 150ms (average overhead ~5ms).
- **Emulation Warm-up**: Instant (once ID is set).

---

## 🔄 Session Lifecycle Management

Choose the listener that fits your architecture:
- **`startSession()`**: Recommended for controlled, one-time flows (e.g., specific payment or login). Automatically scoped.
- **`onTagDiscovered`**: Recommended for global, persistent listeners (background-style scanning).

**Cleanup**: Always call `NfcPro.stopSession()` when the operation is complete to release hardware resources and save battery.

---

## 📖 Deep Dive: APDU SDK Helpers

NFC Pro Manager acts as an SDK, providing helpers to build professional APDU commands.

```dart
// 1. Build a SELECT AID command easily
String aid = "A000000003000000";
String capdu = NfcUtils.buildSelectAid(aid);

// 2. Transceive
String? response = await NfcPro.transceive(capdu);

// 3. Verify success (Check for '9000')
if (NfcUtils.isSuccess(response)) {
  print("Handshake successful!");
}
```

---

## 🛡️ Identity Emulation (HCE)

- **AID Routing**: The service listens for specific Application Identifiers (AIDs).
- **APDU Handling**: Supports bidirectional APDU exchange for secure authentication.
- **Technology Support**: Emulates ISO/IEC 7816-4 based cards.

---

## ⚠️ Platform Limitations & Disclaimers
- **Hardware Dependency**: Identity Emulation (HCE) is **Android-only**.
- **iOS Restrictions**: Apple limits NFC to NDEF and limited ISO-DEP reading only.
- **Encryption**: This library provides the transport layer. Encrypted sectors require valid security keys to be sent via APDU commands.

---

## 📱 Hardware Verification
```dart
bool hasNfc = await NfcPro.isAvailable();
bool hasHce = await NfcPro.supportsEmulation();
```

---

## 📄 License
MIT License.

_Engineered for reliability. Built for the Flutter Community._
