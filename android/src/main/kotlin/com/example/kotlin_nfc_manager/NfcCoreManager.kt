package com.example.kotlin_nfc_manager

import android.app.Activity
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.IsoDep
import android.nfc.tech.Ndef
import android.util.Log

class NfcCoreManager(private val activity: Activity) : NfcAdapter.ReaderCallback {

    private var nfcAdapter: NfcAdapter? = NfcAdapter.getDefaultAdapter(activity)
    private var callback: NfcCallback? = null
    private var currentTag: Tag? = null

    interface NfcCallback {
        fun onTagDiscovered(uid: String, cardType: String, content: String?)
        fun onIsoDepDetected(isoDep: IsoDep)
        fun onLogGenerated(message: String)
        fun onError(code: String, message: String) // Structured Error
    }

    fun setCallback(callback: NfcCallback) {
        this.callback = callback
    }

    fun startSession() {
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

        // Fix 2: Complete Reader Flags (including NFC-F and NFC-V)
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
        nfcAdapter?.disableReaderMode(activity)
        currentTag = null
    }

    override fun onTagDiscovered(tag: Tag?) {
        if (tag == null) return
        currentTag = tag

        val uid = byteArrayToHexString(tag.id)
        val techList = tag.techList.joinToString(", ")
        
        var ndefContent: String? = null
        try {
            val ndef = Ndef.get(tag)
            ndef?.connect()
            val message = ndef?.cachedNdefMessage
            ndefContent = message?.records?.getOrNull(0)?.let { record ->
                val payload = record.payload
                if (payload.isNotEmpty()) {
                    val langLength = payload[0].toInt() and 0x3F
                    String(payload, 1 + langLength, payload.size - 1 - langLength)
                } else null
            }
            ndef?.close()
        } catch (e: Exception) {
            Log.e("NfcCoreManager", "NDEF Read Error: ${e.message}")
        }

        val isoDep = IsoDep.get(tag)
        if (isoDep != null) {
            callback?.onIsoDepDetected(isoDep)
        }

        activity.runOnUiThread {
            callback?.onTagDiscovered(uid, techList, ndefContent)
        }
    }

    fun transceiveApdu(command: ByteArray): ByteArray? {
        val tag = currentTag ?: return null
        val isoDep = IsoDep.get(tag) ?: return null
        
        // Fix 3: Proper Connection Management (using finally to close)
        return try {
            if (!isoDep.isConnected) isoDep.connect()
            isoDep.transceive(command)
        } catch (e: Exception) {
            Log.e("NfcCoreManager", "Transceive error: ${e.message}")
            null
        } finally {
            try { isoDep.close() } catch (_: Exception) {}
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

    private fun byteArrayToHexString(bytes: ByteArray): String = bytes.joinToString("") { "%02X".format(it) }

    fun hexStringToByteArray(s: String): ByteArray {
        val len = s.length
        val data = ByteArray(len / 2)
        for (i in 0 until len step 2) {
            data[i / 2] = ((Character.digit(s[i], 16) shl 4) + Character.digit(s[i + 1], 16)).toByte()
        }
        return data
    }
}
