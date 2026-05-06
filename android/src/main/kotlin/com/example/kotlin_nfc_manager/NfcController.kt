package com.example.kotlin_nfc_manager

import android.app.Activity
import android.nfc.tech.IsoDep
import android.util.Log
import io.flutter.plugin.common.EventChannel
import java.lang.ref.WeakReference

class NfcController(activity: Activity) : NfcCoreManager.NfcCallback {

    // Fix 2: Memory Safety using WeakReference to prevent Activity leaks
    private val activityRef = WeakReference(activity)
    private val nfcCoreManager = NfcCoreManager(activity)
    
    private var eventSink: EventChannel.EventSink? = null
    private var lastUid: String? = null
    
    @Synchronized
    private val pendingEvents = mutableListOf<Map<String, Any?>>()
    
    @Volatile private var isSessionActive = false
    @Volatile private var isProcessing = false

    init {
        nfcCoreManager.setCallback(this)
    }

    @Synchronized
    fun updateEventSink(sink: EventChannel.EventSink?) {
        this.eventSink = sink
        if (sink == null) return
        
        // Fix 1: Capture current sink reference before jumping to UI thread
        val currentSink = sink
        if (pendingEvents.isNotEmpty()) {
            val eventsToFlush = ArrayList(pendingEvents)
            pendingEvents.clear()
            
            activityRef.get()?.runOnUiThread {
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
        
        // Fix 1: Capture current sink for thread-safe UI thread delivery
        val currentSink = eventSink
        activityRef.get()?.runOnUiThread {
            if (currentSink == null) {
                synchronized(this) {
                    pendingEvents.add(eventData)
                }
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
        val currentSink = eventSink
        activityRef.get()?.runOnUiThread {
            if (currentSink == null) {
                Log.e("NfcController", "Deferred Error: $message")
            } else {
                currentSink.error(code, message, null)
            }
        }
    }
}
