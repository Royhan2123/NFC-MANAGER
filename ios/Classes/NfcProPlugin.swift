import Flutter
import UIKit
import CoreNFC

public class NfcProPlugin: NSObject, FlutterPlugin, NFCTagReaderSessionDelegate {
    
    private var channel: FlutterMethodChannel?
    private var eventChannel: FlutterEventChannel?
    private var eventSink: FlutterEventSink?
    private var session: NFCTagReaderSession?
    
    public static func register(with registrar: FlutterPluginRegistrar) {
        let channel = FlutterMethodChannel(name: "com.nfcpro/methods", binaryMessenger: registrar.messenger())
        let eventChannel = FlutterEventChannel(name: "com.nfcpro/events", binaryMessenger: registrar.messenger())
        
        let instance = NfcProPlugin()
        instance.channel = channel
        registrar.addMethodCallDelegate(instance, channel: channel)
        
        eventChannel.setStreamHandler(instance)
        instance.eventChannel = eventChannel
    }

    public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
        switch call.method {
        case "isAvailable":
            result(NFCReaderSession.readingAvailable)
        case "startScan":
            startNfcSession(result: result)
        case "stopScan":
            stopNfcSession()
            result(true)
        default:
            result(FlutterMethodNotImplemented)
        }
    }
    
    private func startNfcSession(result: @escaping FlutterResult) {
        guard NFCReaderSession.readingAvailable else {
            result(FlutterError(code: "NOT_SUPPORTED", message: "NFC not supported on this device", details: nil))
            return
        }
        
        session = NFCTagReaderSession(pollingOption: [.iso14443, .iso15693, .iso18092], delegate: self, queue: nil)
        session?.alertMessage = "Hold your device near the NFC tag."
        session?.begin()
        result(true)
    }
    
    private func stopNfcSession() {
        session?.invalidate()
        session = nil
    }

    // MARK: - NFCTagReaderSessionDelegate
    
    public func tagReaderSessionDidBecomeActive(_ session: NFCTagReaderSession) {
        // Session active
    }
    
    public func tagReaderSession(_ session: NFCTagReaderSession, didInvalidateWithError error: Error) {
        self.session = nil
        eventSink?(FlutterError(code: "SESSION_INVALIDATED", message: error.localizedDescription, details: nil))
    }
    
    public func tagReaderSession(_ session: NFCTagReaderSession, didDetect tags: [NFCTag]) {
        guard let tag = tags.first else { return }
        
        session.connect(to: tag) { (error: Error?) in
            if let error = error {
                session.invalidate(errorMessage: "Connection failed: \(error.localizedDescription)")
                return
            }
            
            self.processTag(tag, session: session)
        }
    }
    
    private func processTag(_ tag: NFCTag, session: NFCTagReaderSession) {
        var uid = ""
        var type = "unknown"
        
        switch tag {
        case .miFare(let mifareTag):
            uid = mifareTag.identifier.map { String(format: "%02hhX", $0) }.joined()
            type = "MiFare"
        case .iso7816(let iso7816Tag):
            uid = iso7816Tag.identifier.map { String(format: "%02hhX", $0) }.joined()
            type = "ISO7816"
        case .feliCa(let felicaTag):
            uid = felicaTag.currentID.map { String(format: "%02hhX", $0) }.joined()
            type = "FeliCa"
        default:
            break
        }
        
        let eventData: [String: Any] = [
            "uid": uid,
            "type": type,
            "content": "iOS Tag Detected"
        ]
        
        DispatchQueue.main.async {
            self.eventSink?(eventData)
        }
    }
}

extension NfcProPlugin: FlutterStreamHandler {
    public func onListen(withArguments arguments: Any?, eventSink events: @escaping FlutterEventSink) -> FlutterError? {
        self.eventSink = events
        return nil
    }
    
    public func onCancel(withArguments arguments: Any?) -> FlutterError? {
        self.eventSink = nil
        return nil
    }
}
