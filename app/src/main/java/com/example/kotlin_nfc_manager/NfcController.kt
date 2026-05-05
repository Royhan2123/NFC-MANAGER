package com.example.kotlin_nfc_manager

import android.app.Activity
import android.nfc.tech.IsoDep
import android.util.Log

class NfcController(activity: Activity) : NfcCoreManager.NfcCallback {

    private val nfcCoreManager = NfcCoreManager(activity)

    var onTagScanned: ((uid: String, cardType: String, content: String?) -> Unit)? = null
    var onErrorEvent: ((message: String) -> Unit)? = null
    var onLogEvent: ((message: String) -> Unit)? = null

    init {
        nfcCoreManager.setCallback(this)
    }

    fun startNfcSession() {
        Log.d("NfcController", "Starting NFC Session")
        nfcCoreManager.startSession()
    }

    fun stopNfcSession() {
        Log.d("NfcController", "Stopping NFC Session")
        nfcCoreManager.stopSession()
    }

    fun transceiveApdu(commandHex: String): String? {
        val commandBytes = nfcCoreManager.hexStringToByteArray(commandHex)
        val responseBytes = nfcCoreManager.transceiveApdu(commandBytes)
        
        return if (responseBytes != null) {
            nfcCoreManager.byteArrayToHexString(responseBytes)
        } else {
            null
        }
    }

    fun transceiveNfcA(commandHex: String): String? {
        val commandBytes = nfcCoreManager.hexStringToByteArray(commandHex)
        val responseBytes = nfcCoreManager.transceiveNfcA(commandBytes)
        
        return if (responseBytes != null) {
            nfcCoreManager.byteArrayToHexString(responseBytes)
        } else {
            null
        }
    }

    fun writeNdef(message: String): Boolean {
        val tag = nfcCoreManager.getCurrentTag() ?: return false
        return nfcCoreManager.writeNdefMessage(tag, message)
    }

    fun getPresenceToken(): String? {
        val tag = nfcCoreManager.getCurrentTag()
        return if (tag != null) {
            nfcCoreManager.getPresenceToken(tag)
        } else {
            null
        }
    }

    fun testApduCommand(): String {
        val selectPpse = "00A404000E325041592E5359532E444446303100"
        val response = transceiveApdu(selectPpse)
        return response ?: "No Response / Failed"
    }

    override fun onTagDiscovered(uid: String, cardType: String, content: String?) {
        Log.d("NfcController", "Tag Discovered - UID: $uid, Type: $cardType")
        onTagScanned?.invoke(uid, cardType, content)
    }

    override fun onIsoDepDetected(isoDep: IsoDep) {
        Log.d("NfcController", "ISO-DEP (Smart Card) detected")
    }

    override fun onLogGenerated(message: String) {
        onLogEvent?.invoke(message)
    }

    override fun onError(message: String) {
        Log.e("NfcController", "NFC Error: $message")
        onErrorEvent?.invoke(message)
    }
}
