package com.example.kotlin_nfc_manager

import android.app.Activity
import android.nfc.tech.IsoDep
import android.util.Log
import io.flutter.plugin.common.EventChannel

class NfcController(activity: Activity, private val eventSink: EventChannel.EventSink?) : NfcCoreManager.NfcCallback {

    private val nfcCoreManager = NfcCoreManager(activity)

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

    fun writeNdef(message: String): Boolean {
        val tag = nfcCoreManager.getCurrentTag() ?: return false
        return nfcCoreManager.writeNdefMessage(tag, message)
    }

    override fun onTagDiscovered(uid: String, cardType: String, content: String?) {
        Log.d("NfcController", "Tag Discovered - UID: $uid, Type: $cardType")
        
        // Dispatch to Flutter via EventChannel
        val eventData = mapOf(
            "uid" to uid,
            "type" to cardType,
            "content" to content
        )
        
        activity.runOnUiThread {
            eventSink?.success(eventData)
        }
    }

    override fun onIsoDepDetected(isoDep: IsoDep) {
        Log.d("NfcController", "ISO-DEP (Smart Card) detected")
    }

    override fun onLogGenerated(message: String) {
        Log.d("NfcController", "Log: $message")
    }

    override fun onError(message: String) {
        Log.e("NfcController", "NFC Error: $message")
        activity.runOnUiThread {
            eventSink?.error("NFC_ERROR", message, null)
        }
    }
}
