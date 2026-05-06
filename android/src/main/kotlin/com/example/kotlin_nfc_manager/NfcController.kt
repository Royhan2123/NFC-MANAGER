package com.example.kotlin_nfc_manager

import android.app.Activity
import android.nfc.tech.IsoDep
import android.util.Log
import io.flutter.plugin.common.EventChannel

class NfcController(private val activity: Activity) : NfcCoreManager.NfcCallback {

    private val nfcCoreManager = NfcCoreManager(activity)
    private var eventSink: EventChannel.EventSink? = null
    private var lastUid: String? = null
    
    // Fix 1: Thread-Safe Event Buffering
    private val pendingEvents = mutableListOf<Map<String, Any?>>()
    
    @Volatile private var isSessionActive = false
    @Volatile private var isProcessing = false

    init {
        nfcCoreManager.setCallback(this)
    }

    @Synchronized
    fun updateEventSink(sink: EventChannel.EventSink?) {
        this.eventSink = sink
        // Fix 2: Safe Flush with local reference
        val currentSink = sink ?: return
        if (pendingEvents.isNotEmpty()) {
            val eventsToFlush = ArrayList(pendingEvents)
            pendingEvents.clear()
            activity.runOnUiThread {
                eventsToFlush.forEach { currentSink.success(it) }
            }
        }
    }

    fun startNfcSession() {
        if (isSessionActive) return
        isSessionActive = true
        lastUid = null
        nfcCoreManager.startSession()
    }

    fun stopNfcSession() {
        isSessionActive = false
        nfcCoreManager.stopSession()
    }

    fun transceiveApdu(commandHex: String): String? {
        if (isProcessing) return null
        isProcessing = true
        
        return try {
            val commandBytes = nfcCoreManager.hexStringToByteArray(commandHex)
            val responseBytes = nfcCoreManager.transceiveApdu(commandBytes)
            responseBytes?.let { nfcCoreManager.byteArrayToHexString(it) }
        } finally {
            isProcessing = false
        }
    }

    fun writeNdef(message: String): Boolean {
        val tag = nfcCoreManager.getCurrentTag() ?: return false
        return nfcCoreManager.writeNdefMessage(tag, message)
    }

    @Synchronized
    override fun onTagDiscovered(uid: String, cardType: String, content: String?) {
        if (!isSessionActive) return
        if (uid == lastUid) return
        lastUid = uid

        val eventData = mapOf(
            "uid" to uid,
            "type" to cardType,
            "content" to content
        )
        
        activity.runOnUiThread {
            val currentSink = eventSink
            if (currentSink == null) {
                pendingEvents.add(eventData)
            } else {
                currentSink.success(eventData)
            }
        }
    }

    override fun onIsoDepDetected(isoDep: IsoDep) {
        try {
            isoDep.timeout = 5000 
        } catch (e: Exception) {
            Log.e("NfcController", "Failed to set IsoDep timeout")
        }
    }

    override fun onLogGenerated(message: String) {
        Log.d("NfcController", "Log: $message")
    }

    @Synchronized
    override fun onError(code: String, message: String) {
        activity.runOnUiThread {
            val currentSink = eventSink
            if (currentSink == null) {
                Log.e("NfcController", "Deferred Error: $message")
            } else {
                currentSink.error(code, message, null)
            }
        }
    }
}
