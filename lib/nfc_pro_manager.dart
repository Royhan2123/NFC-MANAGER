import 'dart:async';
import 'package:flutter/services.dart';

/// Supported types of NFC errors for structured handling.
enum NfcErrorType {
  notSupported,
  disabled,
  timeout,
  connectionLost,
  invalidApdu,
  unknown
}

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

/// Professional NFC Manager API with Enterprise Session Control.
class NfcPro {
  static const MethodChannel _methodChannel = MethodChannel('com.nfcpro/methods');
  static const EventChannel _eventChannel = EventChannel('com.nfcpro/events');

  /// Checks if NFC hardware is available and enabled on the device.
  static Future<bool> isAvailable() async {
    final bool? available = await _methodChannel.invokeMethod('isAvailable');
    return available ?? false;
  }

  /// Verifies if the device supports Host Card Emulation (HCE).
  static Future<bool> supportsEmulation() async {
    final bool? supported = await _methodChannel.invokeMethod('supportsEmulation');
    return supported ?? false;
  }

  /// Starts a professional NFC session.
  /// Use this for clean session lifecycle management.
  static Future<void> startSession({
    Function(NfcTag)? onDiscovered,
    Function(NfcException)? onError,
  }) async {
    try {
      await _methodChannel.invokeMethod('startScan');
    } on PlatformException catch (e) {
      if (onError != null) {
        onError(NfcException.fromPlatformException(e));
      } else {
        rethrow;
      }
    }
  }

  /// Stops the current NFC session and releases hardware resources.
  static Future<void> stopSession() async {
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
      throw NfcException.fromPlatformException(e);
    }
  }

  /// Sets the identity string for HCE (Identity Emulation).
  /// [id] is the string identity that will be processed by the internal AID routing.
  static Future<bool> setEmulationId(String id) async {
    final bool? success = await _methodChannel.invokeMethod('setClonedId', {'id': id});
    return success ?? false;
  }

  /// Stream that listens to real-time NFC events.
  static Stream<NfcTag> get onTagDiscovered {
    return _eventChannel
        .receiveBroadcastStream()
        .map((event) => NfcTag.fromMap(Map<String, dynamic>.from(event)));
  }
}

/// Custom Exception for NFC related errors with specific types.
class NfcException implements Exception {
  final String message;
  final NfcErrorType type;
  final String code;

  NfcException(this.message, {this.type = NfcErrorType.unknown, this.code = 'NFC_ERROR'});

  factory NfcException.fromPlatformException(PlatformException e) {
    NfcErrorType type;
    switch (e.code) {
      case 'NOT_SUPPORTED': type = NfcErrorType.notSupported; break;
      case 'DISABLED': type = NfcErrorType.disabled; break;
      case 'TIMEOUT': type = NfcErrorType.timeout; break;
      case 'CONNECTION_LOST': type = NfcErrorType.connectionLost; break;
      case 'INVALID_APDU': type = NfcErrorType.invalidApdu; break;
      default: type = NfcErrorType.unknown;
    }
    return NfcException(e.message ?? "Unknown NFC Error", type: type, code: e.code);
  }

  @override
  String toString() => 'NfcException [$type]: $message';
}
