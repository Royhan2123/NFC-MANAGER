import Foundation
import CoreNFC

/// Controller that bridges NFC operations for iOS.
/// Designed to be called from Flutter MethodChannels.
class NfcController {
    
    private let nfcCoreManager = NfcCoreManager()
    
    // Closures for Flutter feedback
    var onTagScanned: ((String, String, String?) -> Void)?
    var onErrorEvent: ((String) -> Void)?
    
    init() {
        nfcCoreManager.onTagDiscovered = { [weak self] uid, type, content in
            self?.onTagScanned?(uid, type, content)
        }
        
        nfcCoreManager.onError = { [weak self] message in
            self?.onErrorEvent?(message)
        }
    }
    
    func startNfcSession() {
        nfcCoreManager.startSession()
    }
    
    func stopNfcSession() {
        nfcCoreManager.stopSession()
    }
    
    func transceiveApdu(commandHex: String, completion: @escaping (String?) -> Void) {
        nfcCoreManager.transceiveApdu(commandHex: commandHex, completion: completion)
    }
    
    /// Simulated check for presence (iOS CoreNFC maintains session while tag is connected)
    func getPresenceToken() -> String? {
        // CoreNFC manages connection state internally. 
        // If the session is active and tag is connected, we can return a token.
        return "IOS_ACTIVE_SESSION"
    }
}
