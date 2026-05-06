import 'dart:async';
import 'package:flutter/services.dart';

/// Error types returned by the NFC hardware.
enum NfcErrorType {
  notSupported,
  disabled,
  timeout,
  connectionLost,
  invalidApdu,
  unknown
}

/// Internal session state tracker.
enum NfcSessionState { idle, starting, active, processing, stopped, error }

/// Device hardware support status.
class NfcSupport {
  final bool isAvailable;
  final bool isHceSupported;
  NfcSupport({required this.isAvailable, required this.isHceSupported});
}

/// Data model for detected NFC tags.
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

/// Professional NFC Manager SDK for Flutter.
/// 
/// Handles tag discovery, APDU communication (ISO-DEP), 
/// and Host Card Emulation (HCE).
class NfcPro {
  static const MethodChannel _methodChannel = MethodChannel('com.nfcpro/methods');
  static const EventChannel _eventChannel = EventChannel('com.nfcpro/events');

  static StreamSubscription? _sessionSubscription;
  static Timer? _sessionTimer;
  static NfcSessionState _state = NfcSessionState.idle;
  static bool _debugMode = false;

  static NfcSessionState get state => _state;

  /// Enable internal debug logging.
  static void enableDebug(bool enable) => _debugMode = enable;

  /// Check if the current device supports NFC and HCE.
  static Future<NfcSupport> checkSupport() async {
    final bool available = await _methodChannel.invokeMethod('isAvailable') ?? false;
    final bool hce = await _methodChannel.invokeMethod('supportsEmulation') ?? false;
    return NfcSupport(isAvailable: available, isHceSupported: hce);
  }

  /// Navigate to system NFC settings.
  static Future<void> openSettings() async {
    await _methodChannel.invokeMethod('openSettings');
  }

  /// Start a new NFC session. 
  /// 
  /// The session automatically handles stream cleanup and timeouts.
  static Future<void> startSession({
    required Function(NfcTag) onDiscovered,
    Function(NfcException)? onError,
    Duration? timeout,
  }) async {
    if (_state == NfcSessionState.starting || _state == NfcSessionState.active) {
      return;
    }

    await stopSession();
    _state = NfcSessionState.starting;

    try {
      await _methodChannel.invokeMethod('startScan');
      _state = NfcSessionState.active;

      _sessionSubscription = onTagDiscovered.listen(
        (tag) {
          _state = NfcSessionState.processing;
          onDiscovered(tag);
          _state = NfcSessionState.active;
        },
        onError: (e) {
          _state = NfcSessionState.error;
          if (onError != null) {
            onError(NfcException.fromPlatformException(e as PlatformException));
          }
        },
      );

      if (timeout != null) {
        _sessionTimer = Timer(timeout, () async {
          await stopSession();
          if (onError != null) {
            onError(NfcException("Session timed out", type: NfcErrorType.timeout, code: "TIMEOUT"));
          }
        });
      }
    } on PlatformException catch (e) {
      _state = NfcSessionState.error;
      if (onError != null) onError(NfcException.fromPlatformException(e));
      else rethrow;
    }
  }

  /// Close the current session and release resources.
  static Future<void> stopSession() async {
    _sessionTimer?.cancel();
    _sessionTimer = null;
    await _sessionSubscription?.cancel();
    _sessionSubscription = null;

    try {
      await _methodChannel.invokeMethod('stopScan');
    } catch (_) {}
    
    _state = NfcSessionState.stopped;
  }

  /// Transceive raw APDU command to an ISO-DEP tag.
  static Future<String?> transceive(String capdu) async {
    try {
      return await _methodChannel.invokeMethod('transceive', {'capdu': capdu});
    } on PlatformException catch (e) {
      throw NfcException.fromPlatformException(e);
    }
  }

  /// Execute a batch of APDU commands.
  static Future<List<String?>> runScript(List<String> script) async {
    final List<String?> responses = [];
    for (final command in script) {
      responses.add(await transceive(command));
    }
    return responses;
  }

  /// Check if the tag is still in range.
  static Future<bool> isTagPresent() async {
    try {
      return await _methodChannel.invokeMethod('isTagPresent') ?? false;
    } catch (_) {
      return false;
    }
  }

  /// Write NDEF data to a tag.
  static Future<bool> writeTag(String data) async {
    final bool? success = await _methodChannel.invokeMethod('writeTag', {'data': data});
    return success ?? false;
  }

  /// Configure local Host Card Emulation identity.
  static Future<bool> setEmulationId(String id) async {
    final bool? success = await _methodChannel.invokeMethod('setClonedId', {'id': id});
    return success ?? false;
  }

  static Stream<NfcTag> get onTagDiscovered {
    return _eventChannel
        .receiveBroadcastStream()
        .map((event) => NfcTag.fromMap(Map<String, dynamic>.from(event)));
  }
}

/// Utilities for building ISO 7816-4 commands.
class NfcApdu {
  static String selectAid(String aid) {
    final String lc = (aid.length ~/ 2).toRadixString(16).padLeft(2, '0');
    return "00A40400$lc${aid}00";
  }

  static String readBinary({int sfi = 0, int offset = 0, int length = 256}) {
    final String p1 = offset.toRadixString(16).padLeft(2, '0');
    final String le = length.toRadixString(16).padLeft(2, '0');
    return "00B0$p1${sfi.toRadixString(16).padLeft(2, '0')}$le";
  }
}

/// Standardized Exception for NFC operations.
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
      case 'TAG_LOST': type = NfcErrorType.connectionLost; break;
      default: type = NfcErrorType.unknown;
    }
    return NfcException(e.message ?? "NFC error", type: type, code: e.code);
  }

  @override
  String toString() => 'NfcException: $message ($code)';
}
