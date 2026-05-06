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
- **Smart Card Research**: Deep-dive into APDU communication.
- **Authentication Systems**: Build secure challenge-response login flows.

---

## 🔧 Common Errors & Solutions

| Error Type | Meaning | Possible Solution |
| :--- | :--- | :--- |
| `notSupported` | Device lacks NFC hardware. | Check device specs or use `isAvailable()`. |
| `disabled` | NFC is turned off in settings. | Prompt user to enable NFC. |
| `timeout` | No tag detected within duration. | Increase timeout or retry session. |
| `connectionLost` | Tag moved too quickly. | Ask user to hold the tag steadily. |
| `invalidApdu` | Malformed APDU command. | Verify hex string format and LC/LE bytes. |

---

## 🔄 Versioning Strategy
- **v1.x.x**: Stable API. Focus on bug fixes and performance optimization.
- **v2.x.x**: (Future) Planned breaking changes to support Multi-session HCE and improved iOS ISO-DEP.

---

## 📖 SDK Features

### Session Lifecycle Management
**Important**: Avoid using `startSession()` and `onTagDiscovered` simultaneously unless you need both localized and global listeners.

```dart
await NfcPro.startSession(
  timeout: Duration(seconds: 10),
  onDiscovered: (tag) => print(tag.uid),
  onError: (e) => print(e.type),
);
```

### SDK Helpers (APDU)
```dart
String capdu = NfcUtils.buildSelectAid("A000000003000000");
String? response = await NfcPro.transceive(capdu);

if (NfcUtils.isSuccess(response)) {
  // Handshake successful
}
```

---

## ⚡ Performance Metrics
- **Discovery Latency**: < 250ms.
- **APDU Round-trip**: 50ms - 150ms.

---

## 📄 License
MIT License.

_Engineered for reliability. Built for the Flutter Community._
