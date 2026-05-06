import 'dart:async';
import 'package:flutter/services.dart';

/**
 * NfcPro - The ultimate NFC library for Flutter.
 */
class NfcPro {
  static const MethodChannel _methodChannel =
      MethodChannel('com.nfcpro/methods');
  static const EventChannel _eventChannel = EventChannel('com.nfcpro/events');

  /// Starts the NFC scanning session.
  static Future<bool> startScan() async {
    try {
      final bool? success = await _methodChannel.invokeMethod('startScan');
      return success ?? false;
    } on PlatformException catch (e) {
      print("NFC Error: ${e.message}");
      return false;
    }
  }

  /// Stops the NFC scanning session.
  static Future<void> stopScan() async {
    await _methodChannel.invokeMethod('stopScan');
  }

  /// Writes NDEF data (Text/URL) to the currently detected tag.
  static Future<bool> writeTag(String data) async {
    final bool? success =
        await _methodChannel.invokeMethod('writeTag', {'data': data});
    return success ?? false;
  }

  /// Sends a raw APDU command to an ISO-DEP tag.
  /// [capdu] should be a hex string or list of bytes.
  static Future<String?> transceive(String capdu) async {
    try {
      return await _methodChannel.invokeMethod('transceive', {'capdu': capdu});
    } on PlatformException catch (e) {
      print("APDU Error: ${e.message}");
      return null;
    }
  }

  /// Sets the identity string for HCE (Identity Emulation).
  static Future<bool> setClonedId(String id) async {
    final bool? success =
        await _methodChannel.invokeMethod('setClonedId', {'id': id});
    return success ?? false;
  }

  /// Gets the currently stored cloned identity.
  static Future<String?> getClonedId() async {
    return await _methodChannel.invokeMethod('getClonedId');
  }

  /// Stream that listens to real-time NFC events.
  static Stream<Map<String, dynamic>> get onTagDiscovered {
    return _eventChannel
        .receiveBroadcastStream()
        .map((event) => Map<String, dynamic>.from(event));
  }
}
