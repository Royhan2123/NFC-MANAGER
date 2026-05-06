package com.nfcpro.manager

import android.app.Activity
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.IsoDep
import android.nfc.tech.Ndef
import android.util.Log
import java.lang.ref.WeakReference

class NfcCoreManager(activity: Activity) : NfcAdapter.ReaderCallback {

    private val activityRef = WeakReference(activity)
    private var nfcAdapter: NfcAdapter? = NfcAdapter.getDefaultAdapter(activity)
    private var callback: NfcCallback? = null
    private var currentTag: Tag? = null
    private var activeIsoDep: IsoDep? = null

    interface NfcCallback {
        fun onTagDiscovered(uid: String, cardType: String, content: String?)
        fun onIsoDepDetected(isoDep: IsoDep)
        fun onLogGenerated(message: String)
        fun onError(code: String, message: String)
    }

    fun setCallback(callback: NfcCallback) {
        this.callback = callback
    }

    fun startSession() {
        val activity = activityRef.get() ?: return
        if (nfcAdapter == null) {
            callback?.onError("NOT_SUPPORTED", "NFC is not supported")
            return
        }
        if (!nfcAdapter!!.isEnabled) {
            callback?.onError("DISABLED", "NFC is disabled")
            return
        }

        val options = android.os.Bundle()
        options.putInt(NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY, 250)

        nfcAdapter?.enableReaderMode(
            activity,
            this,
            NfcAdapter.FLAG_READER_NFC_A or 
            NfcAdapter.FLAG_READER_NFC_B or 
            NfcAdapter.FLAG_READER_NFC_F or 
            NfcAdapter.FLAG_READER_NFC_V or 
            NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK,
            options
        )
    }

    fun stopSession() {
        val activity = activityRef.get() ?: return
        nfcAdapter?.disableReaderMode(activity)
        closeActiveConnection()
        currentTag = null
    }

    private fun closeActiveConnection() {
        try {
            activeIsoDep?.close()
        } catch (_: Exception) {}
        activeIsoDep = null
    }

    override fun onTagDiscovered(tag: Tag?) {
        if (tag == null) return
        closeActiveConnection()
        currentTag = tag

        val uid = byteArrayToHexString(tag.id)
        val techList = tag.techList.joinToString(", ")
        
        var parsedContent = ""
        try {
            val ndef = Ndef.get(tag)
            ndef?.connect()
            val message = ndef?.cachedNdefMessage
            message?.records?.forEach { record ->
                val payload = record.payload
                when {
                    record.tnf == NdefRecord.TNF_WELL_KNOWN && record.type.contentEquals(NdefRecord.RTD_TEXT) -> {
                        val langLength = payload[0].toInt() and 0x3F
                        parsedContent += String(payload, 1 + langLength, payload.size - 1 - langLength) + "\n"
                    }
                    record.tnf == NdefRecord.TNF_WELL_KNOWN && record.type.contentEquals(NdefRecord.RTD_URI) -> {
                        parsedContent += "URI: " + String(payload) + "\n"
                    }
                }
            }
            ndef?.close()
        } catch (e: Exception) {
            Log.e("NfcCoreManager", "NDEF Error: ${e.message}")
        }

        val isoDep = IsoDep.get(tag)
        if (isoDep != null) {
            activeIsoDep = isoDep
            callback?.onIsoDepDetected(isoDep)
        }

        activityRef.get()?.runOnUiThread {
            callback?.onTagDiscovered(uid, techList, parsedContent.trim())
        }
    }

    @Synchronized
    fun transceiveApdu(command: ByteArray): ByteArray? {
        val isoDep = activeIsoDep ?: return null
        return try {
            if (!isoDep.isConnected) isoDep.connect()
            isoDep.transceive(command)
        } catch (e: Exception) {
            Log.e("NfcCoreManager", "Transceive error: ${e.message}")
            closeActiveConnection()
            null
        }
    }

    fun writeNdefMessage(tag: Tag, message: String): Boolean {
        return try {
            val ndef = Ndef.get(tag)
            if (ndef != null) {
                ndef.connect()
                val ndefRecord = NdefRecord.createTextRecord("en", message)
                val ndefMessage = NdefMessage(arrayOf(ndefRecord))
                ndef.writeNdefMessage(ndefMessage)
                ndef.close()
                true
            } else false
        } catch (e: Exception) {
            false
        }
    }

    fun getCurrentTag(): Tag? = currentTag

    // Visibility changed to internal for access by NfcController
    internal fun byteArrayToHexString(bytes: ByteArray): String = bytes.joinToString("") { "%02X".format(it) }

    fun hexStringToByteArray(s: String): ByteArray {
        val len = s.length
        val data = ByteArray(len / 2)
        for (i in 0 until len step 2) {
            data[i / 2] = ((Character.digit(s[i], 16) shl 4) + Character.digit(s[i + 1], 16)).toByte()
        }
        return data
    }
}
