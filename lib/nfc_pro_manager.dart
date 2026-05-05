import 'dart:async';
import 'package:flutter/services.dart';

/**
 * NfcPro - The ultimate NFC library for Flutter.
 * 
 * Supports:
 * - HCE (Host Card Emulation)
 * - Card Cloning (Read -> Store -> Emulate)
 * - NDEF Read/Write
 * - Smart Data Parsing (VCard, URLs, Emails)
 * - Raw APDU Transceive
 */
class NfcPro {
  static const MethodChannel _methodChannel =
      MethodChannel('com.nfcpro/methods');
  static const EventChannel _eventChannel = EventChannel('com.nfcpro/events');

  /// Starts the NFC scanning session.
  /// Use [onTagDiscovered] to listen for results.
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

  /// Sets the identity string for HCE (Identity Emulation).
  /// This allows your phone to act as a specific card.
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
  /// Returns a Map: { 'uid': String, 'type': String, 'content': String? }
  static Stream<Map<String, dynamic>> get onTagDiscovered {
    return _eventChannel
        .receiveBroadcastStream()
        .map((event) => Map<String, dynamic>.from(event));
  }
}
