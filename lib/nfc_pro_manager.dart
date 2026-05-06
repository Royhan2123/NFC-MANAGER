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

/// Represents the hardware capabilities of the device.
class NfcSupport {
  final bool isAvailable;
  final bool isHceSupported;

  NfcSupport({required this.isAvailable, required this.isHceSupported});
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

/// Industry-Grade NFC Manager SDK.
class NfcPro {
  static const MethodChannel _methodChannel = MethodChannel('com.nfcpro/methods');
  static const EventChannel _eventChannel = EventChannel('com.nfcpro/events');

  static StreamSubscription? _sessionSubscription;
  static Timer? _sessionTimer;

  /// Checks detailed hardware capabilities.
  static Future<NfcSupport> checkSupport() async {
    final bool available = await _methodChannel.invokeMethod('isAvailable') ?? false;
    final bool hce = await _methodChannel.invokeMethod('supportsEmulation') ?? false;
    return NfcSupport(isAvailable: available, isHceSupported: hce);
  }

  /// Starts a professional NFC session with proper lifecycle management.
  /// 
  /// The session will automatically handle data routing and cleanup.
  static Future<void> startSession({
    required Function(NfcTag) onDiscovered,
    Function(NfcException)? onError,
    Duration? timeout,
  }) async {
    // 1. Cleanup existing session if any
    await stopSession();

    try {
      // 2. Start native scanning
      await _methodChannel.invokeMethod('startScan');

      // 3. Bind the local listener to the global stream
      _sessionSubscription = onTagDiscovered.listen(
        (tag) => onDiscovered(tag),
        onError: (e) {
          if (onError != null) {
            onError(NfcException.fromPlatformException(e as PlatformException));
          }
        },
      );

      // 4. Handle Timeout
      if (timeout != null) {
        _sessionTimer = Timer(timeout, () async {
          await stopSession();
          if (onError != null) {
            onError(NfcException("NFC Session timed out", type: NfcErrorType.timeout, code: "TIMEOUT"));
          }
        });
      }
    } on PlatformException catch (e) {
      if (onError != null) {
        onError(NfcException.fromPlatformException(e));
      } else {
        rethrow;
      }
    }
  }

  /// Stops the session and releases all resources (Native & Dart).
  static Future<void> stopSession() async {
    _sessionTimer?.cancel();
    _sessionTimer = null;
    
    await _sessionSubscription?.cancel();
    _sessionSubscription = null;

    await _methodChannel.invokeMethod('stopScan');
  }

  /// Sends a raw APDU command to an ISO-DEP tag.
  static Future<String?> transceive(String capdu) async {
    try {
      return await _methodChannel.invokeMethod('transceive', {'capdu': capdu});
    } on PlatformException catch (e) {
      throw NfcException.fromPlatformException(e);
    }
  }

  /// Writes NDEF data (Text/URL) to the currently detected tag.
  static Future<bool> writeTag(String data) async {
    final bool? success = await _methodChannel.invokeMethod('writeTag', {'data': data});
    return success ?? false;
  }

  /// Sets the identity string for Identity Emulation (HCE).
  static Future<bool> setEmulationId(String id) async {
    final bool? success = await _methodChannel.invokeMethod('setClonedId', {'id': id});
    return success ?? false;
  }

  /// Global stream for background listeners (if needed).
  static Stream<NfcTag> get onTagDiscovered {
    return _eventChannel
        .receiveBroadcastStream()
        .map((event) => NfcTag.fromMap(Map<String, dynamic>.from(event)));
  }
}

/// SDK Helper for building and parsing APDU commands.
class NfcUtils {
  static String buildSelectAid(String aid) {
    final String lc = (aid.length ~/ 2).toRadixString(16).padLeft(2, '0');
    return "00A40400$lc${aid}00";
  }

  static bool isSuccess(String? rapdu) {
    return rapdu != null && rapdu.endsWith("9000");
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
