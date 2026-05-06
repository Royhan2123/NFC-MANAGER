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

/// Represents the current state of the NFC session.
enum NfcSessionState { idle, starting, active, processing, stopped, error }

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

/// The Diamond Standard NFC Manager SDK (Version 2.8.0).
/// 
/// engineered for high-security environments, automated lifecycle handling,
/// and bank-grade HCE compliance.
class NfcPro {
  static const MethodChannel _methodChannel = MethodChannel('com.nfcpro/methods');
  static const EventChannel _eventChannel = EventChannel('com.nfcpro/events');

  static StreamSubscription? _sessionSubscription;
  static Timer? _sessionTimer;
  static NfcSessionState _state = NfcSessionState.idle;
  static bool _debugMode = false;

  /// Returns the current state of the NFC session.
  static NfcSessionState get state => _state;

  /// Enables or disables debug logging for internal SDK operations.
  static void enableDebug(bool enable) => _debugMode = enable;

  /// Checks detailed hardware capabilities.
  static Future<NfcSupport> checkSupport() async {
    final bool available = await _methodChannel.invokeMethod('isAvailable') ?? false;
    final bool hce = await _methodChannel.invokeMethod('supportsEmulation') ?? false;
    return NfcSupport(isAvailable: available, isHceSupported: hce);
  }

  /// Opens the system NFC settings for the user.
  static Future<void> openSettings() async {
    await _methodChannel.invokeMethod('openSettings');
  }

  /// Starts a professional NFC session with robust lifecycle management.
  /// 
  /// The session automatically suspends when the app goes to the background.
  static Future<void> startSession({
    required Function(NfcTag) onDiscovered,
    Function(NfcException)? onError,
    Duration? timeout,
  }) async {
    if (_state == NfcSessionState.starting || _state == NfcSessionState.active) {
      if (_debugMode) print("[NfcPro] Session Guard: A session is already active.");
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
            onError(NfcException("NFC Session timed out", type: NfcErrorType.timeout, code: "TIMEOUT"));
          }
        });
      }
    } on PlatformException catch (e) {
      _state = NfcSessionState.error;
      if (onError != null) onError(NfcException.fromPlatformException(e));
      else rethrow;
    }
  }

  /// Stops the current session and releases all resources.
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

  /// Sends a raw APDU command to an ISO-DEP tag.
  static Future<String?> transceive(String capdu) async {
    try {
      return await _methodChannel.invokeMethod('transceive', {'capdu': capdu});
    } on PlatformException catch (e) {
      throw NfcException.fromPlatformException(e);
    }
  }

  /// Runs a sequence of APDU commands as a script.
  /// 
  /// Returns a list of responses for each command.
  static Future<List<String?>> runScript(List<String> script) async {
    final List<String?> responses = [];
    for (final command in script) {
      final response = await transceive(command);
      responses.add(response);
    }
    return responses;
  }

  /// Writes NDEF data to the currently detected tag.
  static Future<bool> writeTag(String data) async {
    final bool? success = await _methodChannel.invokeMethod('writeTag', {'data': data});
    return success ?? false;
  }

  /// Sets the identity string for Identity Emulation (HCE).
  static Future<bool> setEmulationId(String id) async {
    final bool? success = await _methodChannel.invokeMethod('setClonedId', {'id': id});
    return success ?? false;
  }

  /// Global stream for tag discovery.
  static Stream<NfcTag> get onTagDiscovered {
    return _eventChannel
        .receiveBroadcastStream()
        .map((event) => NfcTag.fromMap(Map<String, dynamic>.from(event)));
  }
}

/// Advanced APDU Command Builder.
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

/// Structured Exception for enterprise-grade error handling.
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
    return NfcException(e.message ?? "NFC Error", type: type, code: e.code);
  }

  @override
  String toString() => 'NfcException [$type]: $message';
}
