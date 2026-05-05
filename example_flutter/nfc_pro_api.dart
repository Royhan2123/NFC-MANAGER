import 'dart:async';
import 'package:flutter/services.dart';

class NfcPro {
  static const MethodChannel _methodChannel = MethodChannel('com.nfcpro/methods');
  static const EventChannel _eventChannel = EventChannel('com.nfcpro/events');

  /// Starts the NFC scanning session.
  /// When a tag is discovered, the [onTagDiscovered] stream will emit data.
  static Future<bool> startScan() async {
    final bool? success = await _methodChannel.invokeMethod('startScan');
    return success ?? false;
  }

  /// Stops the NFC scanning session.
  static Future<void> stopScan() async {
    await _methodChannel.invokeMethod('stopScan');
  }

  /// Writes NDEF data (Text/URL) to the currently detected tag.
  static Future<bool> writeTag(String data) async {
    final bool? success = await _methodChannel.invokeMethod('writeTag', {'data': data});
    return success ?? false;
  }

  /// Sets the identity string for HCE (Identity Emulation).
  /// This is used for cloning a card to the phone.
  static Future<bool> setClonedId(String id) async {
    final bool? success = await _methodChannel.invokeMethod('setClonedId', {'id': id});
    return success ?? false;
  }

  /// Gets the currently stored cloned identity.
  static Future<String?> getClonedId() async {
    return await _methodChannel.invokeMethod('getClonedId');
  }

  /// Stream that listens to NFC events from the native side.
  /// Returns a Map with 'uid', 'type', and 'content'.
  static Stream<Map<String, dynamic>> get onTagDiscovered {
    return _eventChannel
        .receiveBroadcastStream()
        .map((event) => Map<String, dynamic>.from(event));
  }
}
