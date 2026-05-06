import 'dart:async';
import 'package:flutter/services.dart';

/// Represents a discovered NFC tag with structured data.
class NfcTag {
  final String uid;
  final String type;
  final String? content;

  NfcTag({required this.uid, required this.type, this.content});

  factory NfcTag.fromMap(Map<String, dynamic> map) {
    return NfcTag(
      uid: map['uid'] ?? 'unknown',
      type: map['type'] ?? 'unknown',
      content: map['content'],
    );
  }
}

/// Professional NFC Manager API.
class NfcPro {
  static const MethodChannel _methodChannel = MethodChannel('com.nfcpro/methods');
  static const EventChannel _eventChannel = EventChannel('com.nfcpro/events');

  /// Starts the NFC scanning session.
  static Future<bool> startScan() async {
    try {
      final bool? success = await _methodChannel.invokeMethod('startScan');
      return success ?? false;
    } on PlatformException catch (e) {
      throw NfcException(e.message ?? "Failed to start scan", code: e.code);
    }
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

  /// Sends a raw APDU command to an ISO-DEP tag.
  static Future<String?> transceive(String capdu) async {
    try {
      return await _methodChannel.invokeMethod('transceive', {'capdu': capdu});
    } on PlatformException catch (e) {
      throw NfcException(e.message ?? "APDU Transmission Failed", code: e.code);
    }
  }

  /// Sets the identity string for HCE (Identity Emulation).
  /// Replaced 'cloning' terminology for professional compliance.
  static Future<bool> setEmulationId(String id) async {
    final bool? success = await _methodChannel.invokeMethod('setClonedId', {'id': id});
    return success ?? false;
  }

  /// Gets the currently stored emulation identity.
  static Future<String?> getEmulationId() async {
    return await _methodChannel.invokeMethod('getClonedId');
  }

  /// Stream that listens to real-time NFC events.
  static Stream<NfcTag> get onTagDiscovered {
    return _eventChannel
        .receiveBroadcastStream()
        .map((event) => NfcTag.fromMap(Map<String, dynamic>.from(event)));
  }
}

/// Custom Exception for NFC related errors.
class NfcException implements Exception {
  final String message;
  final String code;
  NfcException(this.message, {this.code = 'NFC_ERROR'});

  @override
  String toString() => 'NfcException ($code): $message';
}
