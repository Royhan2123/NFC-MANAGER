import Foundation
import CoreNFC

/// Core Manager for iOS NFC operations. 
/// Handles NDEF reading and ISO-DEP (APDU) commands using CoreNFC.
class NfcCoreManager: NSObject, NFCTagReaderSessionDelegate {
    
    private var session: NFCTagReaderSession?
    private var connectedTag: NFCTag?
    
    var onTagDiscovered: ((String, String, String?) -> Void)?
    var onError: ((String) -> Void)?
    
    func startSession() {
        guard NFCTagReaderSession.readingAvailable else {
            onError?("NFC is not supported on this device.")
            return
        }
        
        session = NFCTagReaderSession(pollingOption: [.iso14443, .iso15693, .iso18092], delegate: self, queue: nil)
        session?.alertMessage = "Hold your iPhone near the item to scan."
        session?.begin()
    }
    
    func stopSession() {
        session?.invalidate()
    }
    
    // MARK: - NFCTagReaderSessionDelegate
    
    func tagReaderSessionDidBecomeActive(_ session: NFCTagReaderSession) {
        // Session active
    }
    
    func tagReaderSession(_ session: NFCTagReaderSession, didInvalidateWithError error: Error) {
        if let nfcError = error as? NFCReaderError, nfcError.code != .readerSessionInvalidationErrorUserCanceled {
            onError?(error.localizedDescription)
        }
    }
    
    func tagReaderSession(_ session: NFCTagReaderSession, didDetect tags: [NFCTag]) {
        guard let tag = tags.first else { return }
        
        session.connect(to: tag) { [weak self] error in
            if let error = error {
                self?.onError?(error.localizedDescription)
                return
            }
            
            self?.connectedTag = tag
            self?.processTag(tag, session: session)
        }
    }
    
    private fun processTag(_ tag: NFCTag, session: NFCTagReaderSession) {
        var uid = ""
        var type = ""
        
        switch tag {
        case .miFare(let mifareTag):
            uid = mifareTag.identifier.map { String(format: "%02hhX", $0) }.joined()
            type = "MiFare (ISO-14443-3A)"
        case .iso7816(let iso7816Tag):
            uid = iso7816Tag.identifier.map { String(format: "%02hhX", $0) }.joined()
            type = "Smart Card (ISO-7816)"
        case .feliCa(let felicaTag):
            uid = felicaTag.currentID.map { String(format: "%02hhX", $0) }.joined()
            type = "FeliCa (NFC-F)"
        case .iso15693(let iso15693Tag):
            uid = iso15693Tag.identifier.map { String(format: "%02hhX", $0) }.joined()
            type = "ISO-15693"
        @unknown default:
            type = "Unknown Tag"
        }
        
        // Attempt to read NDEF if available
        if case let .miFare(mifareTag) = tag {
            mifareTag.queryNDEFStatus { status, capacity, error in
                if status == .notSupported {
                    self.onTagDiscovered?(uid, type, nil)
                } else {
                    mifareTag.readNDEF { message, error in
                        let content = message?.records.compactMap { 
                            String(data: $0.payload, encoding: .utf8) 
                        }.joined(separator: "\n")
                        self.onTagDiscovered?(uid, type, content)
                    }
                }
            }
        } else {
            onTagDiscovered?(uid, type, nil)
        }
    }
    
    // MARK: - APDU / Raw Transceive
    
    func transceiveApdu(commandHex: String, completion: @escaping (String?) -> Void) {
        guard let tag = connectedTag else {
            completion(nil)
            return
        }
        
        let commandBytes = hexToData(commandHex)
        
        if case let .iso7816(iso7816Tag) = tag {
            let apdu = NFCISO7816APDU(data: commandBytes)!
            iso7816Tag.sendCommand(apdu: apdu) { responseData, sw1, sw2, error in
                if let error = error {
                    completion(nil)
                    return
                }
                var fullResponse = responseData
                fullResponse.append(sw1)
                fullResponse.append(sw2)
                completion(fullResponse.map { String(format: "%02hhX", $0) }.joined())
            }
        } else {
            completion(nil)
        }
    }
    
    private func hexToData(_ hex: String) -> Data {
        var data = Data()
        var hex = hex
        while(hex.count > 0) {
            let subIndex = hex.index(hex.startIndex, offsetBy: 2)
            let c = String(hex[..<subIndex])
            hex = String(hex[subIndex...])
            var ch: UInt32 = 0
            Scanner(string: c).scanHexInt32(&ch)
            var char = UInt8(ch)
            data.append(&char, count: 1)
        }
        return data
    }
}
