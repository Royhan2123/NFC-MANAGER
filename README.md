# NFC Pro Manager 🚀

The most powerful, aesthetically pleasing, and feature-rich NFC library for Flutter. Built for enterprise-grade applications requiring advanced features like **Host Card Emulation (HCE)**, **Card Cloning**, and **Smart Data Parsing**.

[![Pub Version](https://img.shields.io/pub/v/nfc_pro_manager)](https://pub.dev/packages/nfc_pro_manager)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

## 🌟 Key Features

- 📱 **Host Card Emulation (HCE)**: Turn your Android device into a digital identity card.
- 👯 **Card Cloning**: Read a physical tag, store its identity, and emulate it instantly.
- ✍️ **Tag Writer**: Write URLs, Text, and complex NDEF records to physical tags.
- 🧠 **Smart Data Parsing**: Automatically categorizes NDEF content (Emails, VCards, URLs, etc.).
- 🛡️ **Secure handshaking**: Advanced ISO-DEP communication for secure identity verification.
- 🎨 **Premium UI Components**: Built-in iOS-inspired design system for your NFC apps.

---

## 🚀 Getting Started

### Installation

Add `nfc_pro_manager` to your `pubspec.yaml`:

```yaml
dependencies:
  nfc_pro_manager: ^1.0.0
```

### Usage

#### 1. Listening for Tags
```dart
import 'package:nfc_pro_manager/nfc_pro_api.dart';

// Start listening to the stream
NfcPro.onTagDiscovered.listen((tag) {
  print("Found Tag: ${tag['uid']}");
  print("Data: ${tag['content']}");
});

// Start the scanning session
await NfcPro.startScan();
```

#### 2. Writing Data
```dart
bool success = await NfcPro.writeTag("https://flutter.dev");
if (success) print("Write Successful!");
```

#### 3. Card Cloning (HCE)
```dart
// Capture a card's ID and make the phone emulate it
await NfcPro.setClonedId("IDENTITY-9988");
```

---

## 🛠 Platform Setup

### Android
Add permissions to your `AndroidManifest.xml`:
```xml
<uses-permission android:name="android.permission.NFC" />
<uses-feature android:name="android.hardware.nfc" android:required="true" />
```

For HCE support, register the service:
```xml
<service android:name="com.example.kotlin_nfc_manager.NfcHceService"
         android:exported="true"
         android:permission="android.permission.BIND_NFC_SERVICE">
    <!-- ... apdu service xml ... -->
</service>
```

---

## 🎨 Design System
This package includes a pre-built UI library to help you create stunning NFC apps:
- **Command Center Dashboard**
- **Secure Scan Terminal**
- **Cloning Wizard**

---

## 📄 License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🤝 Contributing
Contributions are welcome! Please open an issue or submit a pull request on GitHub.

---
*Built with ❤️ for the Flutter Community.*

---
Contact me to for enterprise Solution : 
royhankeren12@gmail.com