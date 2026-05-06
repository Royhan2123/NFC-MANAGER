package com.example.kotlin_nfc_manager

import android.app.Activity
import android.nfc.tech.IsoDep
import android.util.Log
import io.flutter.plugin.common.EventChannel

class NfcController(private val activity: Activity, private var eventSink: EventChannel.EventSink?) : NfcCoreManager.NfcCallback {

    private val nfcCoreManager = NfcCoreManager(activity)
    private var lastUid: String? = null

    init {
        nfcCoreManager.setCallback(this)
    }

    // Fix BUG 1: Dynamic eventSink update
    fun updateEventSink(sink: EventChannel.EventSink?) {
        this.eventSink = sink
    }

    fun startNfcSession() {
        lastUid = null // Reset debounce on new session
        nfcCoreManager.startSession()
    }

    fun stopNfcSession() {
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
        // Fix IMPROVEMENT 3: Debounce logic to prevent double triggers
        if (uid == lastUid) return
        lastUid = uid

        Log.d("NfcController", "Tag Discovered - UID: $uid")
        
        val eventData = mapOf(
            "uid" to uid,
            "type" to cardType,
            "content" to content
        )
        
        // Fix BUG 3: activity is now a property
        activity.runOnUiThread {
            eventSink?.success(eventData)
        }
    }

    override fun onIsoDepDetected(isoDep: IsoDep) {
        // Fix DESIGN ISSUE: Add timeout to prevent hanging
        try {
            isoDep.timeout = 5000 
        } catch (e: Exception) {
            Log.e("NfcController", "Failed to set IsoDep timeout")
        }
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
