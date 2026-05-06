# NFC Pro Manager 🚀

A professional-grade NFC library for Flutter. Designed for high-performance NFC operations, including **Identity Emulation (HCE)**, **ISO-DEP Raw Communication**, and **Advanced NDEF Operations**.

---

## 🌟 Core Features

- **Identity Emulation (HCE)**: Securely emulate tag identities via Android Host Card Emulation.
- **ISO-DEP / APDU Transceive**: Direct low-level communication with Smart Cards (EMV, Mifare, etc.).
- **NDEF Engine**: High-level API for reading and writing structured data (URLs, Text, vCards).
- **Strongly Typed API**: All tag data is returned as structured `NfcTag` objects.
- **Lifecycle Awareness**: Managed NFC sessions to preserve battery and hardware resources.

---

## 🔒 Security & Legal Disclaimer

**Identity Emulation** features are intended for authorized use cases such as digital employee badges, loyalty programs, and academic research. 
- **DO NOT** use this library to attempt unauthorized access to secure systems.
- This package **cannot** bypass hardware-level encryption (e.g., Triple DES, AES) without valid keys.
- The developer of this package is not responsible for any misuse or legal consequences of using emulation features.

---

## ⚠️ Technical Limitations

1. **Hardware Support**: Identity Emulation requires hardware support for HCE (Host Card Emulation). Use `NfcPro.checkSupport()` (coming soon) to verify.
2. **iOS Policy**: Emulation is **NOT** available on iOS due to Apple's system-level restrictions.
3. **Encrypted Tags**: Reading or writing to encrypted sectors requires specialized keys which are not managed by this library.

---

## 📖 Deep Dive: APDU Communication

APDU (Application Protocol Data Unit) is the communication language between an NFC reader and a Smart Card. 

### How to send a command:
```dart
// Select PPSE (Contactless Payment System Environment)
// Format: [CLA][INS][P1][P2][LC][DATA][LE]
String selectCommand = "00A404000E325041592E5359532E444446303100";

try {
  String? response = await NfcPro.transceive(selectCommand);
  print("Card Response: $response");
} on NfcException catch (e) {
  print("Failed to communicate: ${e.message}");
}
```

---

## 💻 Usage

### Listen for Tags
```dart
NfcPro.onTagDiscovered.listen((NfcTag tag) {
  print("Scanned Tag UID: ${tag.uid}");
  print("Technology: ${tag.type}");
});

await NfcPro.startScan();
```

### Identity Emulation
```dart
// Sets the phone's HCE identity
await NfcPro.setEmulationId("MY-ID-1234");
```

---

## 📱 Device Compatibility

| Feature | Android (minSDK 30) | iOS (13+) |
| :--- | :---: | :---: |
| UID Discovery | ✅ | ✅ |
| NDEF R/W | ✅ | ✅ |
| APDU Transceive | ✅ | ✅ |
| Identity Emulation | ✅ | ❌ |

---

## 📄 License
MIT License.

_Built with ❤️ for the Flutter Community._
