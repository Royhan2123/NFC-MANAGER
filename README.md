# NFC Pro Manager 🚀

A professional and powerful NFC library for Flutter. Designed for advanced use cases including **Host Card Emulation (HCE)**, **Card Identity Emulation**, **NDEF Operations**, and **Raw APDU Transceive (ISO-DEP)**.

---

## 🌟 Key Features

- **Identity Emulation (HCE)**: Make your phone act like a specific NFC card.
- **Card Cloning Workflow**: Read a tag's UID and emulate it instantly.
- **NDEF Read/Write**: Support for URLs, Text, and Smart Data.
- **Raw APDU Commands**: Send custom commands to ISO-DEP smart cards (e.g., E-Money, Credit Cards).
- **Headless Architecture**: No forced UI, full control via Flutter.

---

## ⚠️ Important Limitations

Before using this package, please understand the following technical constraints:

1. **Hardware Specific**: Not all Android phones support HCE (Host Card Emulation). If the hardware doesn't support it, the emulation features will not work.
2. **Encryption Barrier**: This package **CANNOT** bypass encrypted sectors of high-security cards (like Mifare DESFire or HID iClass) unless you provide the encryption keys. It focuses on **UID Emulation** and **NDEF data**.
3. **Android 11+ Requirement**: For stable performance and advanced HCE features, this package requires **minSdkVersion 30**.
4. **iOS Constraints**: iOS (Apple) strictly limits NFC access. Emulation (HCE) is **NOT supported on iOS** due to Apple's security policy. iOS only supports NDEF reading/writing and some ISO-DEP operations.

---

## 🛠 Platform Setup

### Android
Add permissions to your `app/src/main/AndroidManifest.xml`:
```xml
<uses-permission android:name="android.permission.NFC" />
<uses-feature android:name="android.hardware.nfc" android:required="true" />
```

### iOS
Add to your `Info.plist`:
```xml
<key>NFCReaderUsageDescription</key>
<string>We need NFC access to scan tags.</string>
```

---

## 📖 Usage Examples

### 1. Basic Tag Discovery
```dart
import 'package:nfc_pro_manager/nfc_pro_manager.dart';

NfcPro.onTagDiscovered.listen((tag) {
  print("Tag UID: ${tag['uid']}");
  print("Tag Type: ${tag['type']}");
});

await NfcPro.startScan();
```

### 2. Identity Emulation (HCE)
```dart
// Make this phone appear as a card with UID "BEEF-1234"
await NfcPro.setClonedId("BEEF-1234");
```

### 3. Advanced: ISO-DEP / APDU Commands
Use this to talk to smart cards (e.g., reading data from a secure chip).
```dart
// Example: Selecting a payment application (PPSE)
String capdu = "00A404000E325041592E5359532E444446303100";
String? response = await NfcPro.transceive(capdu);

print("Card Response: $response");
```

---

## 💼 Real Use Cases

- **Digital Access Keys**: Emulate employee IDs or gym cards (non-encrypted UIDs).
- **Asset Tracking**: Read and write specialized data to industrial NFC tags.
- **Smart Business Cards**: Share vCards or URLs via NFC write.
- **Payment Research**: Analyze APDU responses from contactless cards (for educational purposes).

---

## 📱 Device Compatibility

| Feature | Android 11+ | iOS 13+ |
| :--- | :---: | :---: |
| UID Reading | ✅ | ✅ |
| NDEF Read/Write | ✅ | ✅ |
| ISO-DEP (APDU) | ✅ | ✅ |
| HCE (Emulation) | ✅ | ❌ (Blocked by Apple) |

---

## 📄 License
This project is licensed under the MIT License.

_Built with ❤️ for the Flutter Community._
