package com.nfcpro.manager

import android.app.Activity
import android.nfc.TagLostException
import android.nfc.tech.IsoDep
import android.util.Log
import io.flutter.plugin.common.EventChannel
import java.lang.ref.WeakReference
import java.util.concurrent.atomic.AtomicBoolean

class NfcController(activity: Activity) : NfcCoreManager.NfcCallback {

    private val activityRef = WeakReference(activity)
    private val nfcCoreManager = NfcCoreManager(activity)
    
    private var eventSink: EventChannel.EventSink? = null
    private var lastUid: String? = null
    
    private val pendingEvents = mutableListOf<Map<String, Any?>>()
    
    private val isSessionActive = AtomicBoolean(false)
    private val isProcessing = AtomicBoolean(false)

    init {
        nfcCoreManager.setCallback(this)
    }

    @Synchronized
    fun updateEventSink(sink: EventChannel.EventSink?) {
        this.eventSink = sink
        if (sink == null) return
        
        val currentSink = sink
        if (pendingEvents.isNotEmpty()) {
            val eventsToFlush = ArrayList(pendingEvents)
            pendingEvents.clear()
            
            activityRef.get()?.runOnUiThread {
                eventsToFlush.forEach { currentSink.success(it) }
            }
        }
    }

    fun clearPendingEvents() {
        synchronized(this) {
            pendingEvents.clear()
        }
    }

    fun startNfcSession() {
        if (!isSessionActive.compareAndSet(false, true)) return
        lastUid = null
        nfcCoreManager.startSession()
    }

    fun stopNfcSession() {
        isSessionActive.set(false)
        nfcCoreManager.stopSession()
    }

    fun transceiveApdu(commandHex: String): String? {
        if (!isProcessing.compareAndSet(false, true)) return null
        
        return try {
            val commandBytes = nfcCoreManager.hexStringToByteArray(commandHex)
            val responseBytes = nfcCoreManager.transceiveApdu(commandBytes)
            responseBytes?.let { nfcCoreManager.byteArrayToHexString(it) }
        } catch (e: TagLostException) {
            onError("TAG_LOST", "NFC connection lost")
            null
        } finally {
            isProcessing.set(false)
        }
    }

    fun isTagConnected(): Boolean {
        return try {
            nfcCoreManager.getCurrentTag()?.let { tag ->
                IsoDep.get(tag)?.isConnected == true
            } ?: false
        } catch (_: Exception) {
            false
        }
    }

    fun writeNdef(message: String): Boolean {
        val tag = nfcCoreManager.getCurrentTag() ?: return false
        return nfcCoreManager.writeNdefMessage(tag, message)
    }

    @Synchronized
    override fun onTagDiscovered(uid: String, cardType: String, content: String?) {
        if (!isSessionActive.get()) return
        if (uid == lastUid) return
        lastUid = uid

        val eventData = mapOf(
            "uid" to uid,
            "type" to cardType,
            "content" to content
        )
        
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
        } catch (_: Exception) {}
    }

    override fun onLogGenerated(message: String) {
        // Logging for production debugging
    }

    @Synchronized
    override fun onError(code: String, message: String) {
        val currentSink = eventSink
        activityRef.get()?.runOnUiThread {
            currentSink?.error(code, message, null)
        }
    }
}
