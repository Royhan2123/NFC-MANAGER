# NFC Pro Manager 🚀💎

[![pub package](https://img.shields.io/pub/v/nfc_pro_manager.svg)](https://pub.dev/packages/nfc_pro_manager)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

**The industry-grade NFC SDK for Flutter.**  
Engineered for high-security environments, enterprise lifecycle management, and bank-grade HCE compliance.

---

## 🔥 What's New in v3.0.0 (The Professional Rebranding)
- **Professional Identity**: Migrated to `com.nfcpro.manager` namespace for production credibility.
- **Diamond Lifecycle**: Automated session suspension when app enters background via `DefaultLifecycleObserver`.
- **Bank-Grade HCE**: Full ISO 7816-4 FCI Template support (6F -> 84 -> A5).
- **Atomic Concurrency**: Lock-free hardware access using `AtomicBoolean` for extreme reliability.
- **Real-time Presence**: Detection of card removal via `isTagPresent()`.

---

## 🛠 Features
- ✅ **HCE (Host Card Emulation)**: Clone identities or emulate smart cards.
- ✅ **ISO-DEP / APDU**: Full support for banking, e-money, and access cards.
- ✅ **NDEF Support**: Read/Write Text and URI records with multi-record support.
- ✅ **Persistent Connection**: Keep connections alive for multiple APDU exchanges.
- ✅ **Event Buffering**: No data loss during application startup.
- ✅ **Cross Platform**: Robust implementation for both Android and iOS.

---

## 🚀 Getting Started

### Installation
Add this to your `pubspec.yaml`:
```yaml
dependencies:
  nfc_pro_manager: ^3.0.0
```

### Android Configuration
Ensure your `AndroidManifest.xml` includes:
```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.your.app.id">
    
    <uses-permission android:name="android.permission.NFC" />
    <uses-feature android:name="android.hardware.nfc" android:required="false" />
</manifest>
```

---

## 📖 Usage

### Start Scanning
```dart
import 'package:nfc_pro_manager/nfc_pro_manager.dart';

void startNfc() async {
  await NfcPro.startSession(
    onDiscovered: (tag) {
      print("Tag Found: ${tag.uid}");
    },
    onError: (exception) {
      print("NFC Error: ${exception.message}");
    },
  );
}
```

### Transceive APDU (Smart Cards)
```dart
String? response = await NfcPro.transceive("00A4040007F001020304050600");
```

### runScript (APDU Batch)
```dart
List<String?> responses = await NfcPro.runScript([
  NfcApdu.selectAid("F0010203040506"),
  NfcApdu.readBinary(sfi: 1, offset: 0),
]);
```

### Identity Emulation (HCE)
```dart
await NfcPro.setEmulationId("PRO-IDENTITY-123");
```

---

## 🎖 Support Matrix
| Feature | Android | iOS |
|---------|:-------:|:---:|
| Tag Reading | ✅ | ✅ |
| NDEF Write | ✅ | ❌ |
| HCE (Emulation) | ✅ | ❌ |
| APDU (ISO-DEP) | ✅ | ✅ |
| FeliCa | ✅ | ✅ |
| MiFare | ✅ | ✅ |

---

## 🤝 Contributing
Contributions are welcome! Please feel free to submit a Pull Request.

## 📄 License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
