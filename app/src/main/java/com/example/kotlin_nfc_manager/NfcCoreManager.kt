package com.example.kotlin_nfc_manager

import android.app.Activity
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.tech.*
import android.util.Log
import java.io.IOException
import java.nio.charset.Charset

/**
 * Core Manager for NFC operations on Android.
 * Handles Reader Mode, Tag Discovery, NDEF reading/writing, and ISO-DEP communication.
 */
class NfcCoreManager(private val activity: Activity) : NfcAdapter.ReaderCallback {

    private val nfcAdapter: NfcAdapter? = NfcAdapter.getDefaultAdapter(activity)
    private var callback: NfcCallback? = null
    private var currentIsoDep: IsoDep? = null
    private var currentTag: Tag? = null

    interface NfcCallback {
        fun onTagDiscovered(uid: String, cardType: String, content: String?)
        fun onIsoDepDetected(isoDep: IsoDep)
        fun onError(message: String)
        fun onLogGenerated(message: String)
    }

    private fun log(message: String) {
        Log.d("NfcCore", message)
        callback?.onLogGenerated(message)
    }

    fun setCallback(callback: NfcCallback) {
        this.callback = callback
    }

    fun getCurrentTag(): Tag? = currentTag

    fun startSession() {
        if (nfcAdapter == null) {
            callback?.onError("NFC is not available on this device.")
            return
        }

        if (!nfcAdapter.isEnabled) {
            callback?.onError("NFC is disabled. Please enable it in settings.")
            return
        }

        val flags = NfcAdapter.FLAG_READER_NFC_A or
                    NfcAdapter.FLAG_READER_NFC_B or
                    NfcAdapter.FLAG_READER_NFC_F or
                    NfcAdapter.FLAG_READER_NFC_V or
                    NfcAdapter.FLAG_READER_NFC_BARCODE or
                    NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK

        nfcAdapter.enableReaderMode(activity, this, flags, null)
    }

    fun stopSession() {
        nfcAdapter?.disableReaderMode(activity)
        disconnectIsoDep()
    }

    override fun onTagDiscovered(tag: Tag?) {
        if (tag == null) return
        currentTag = tag

        val uid = tag.id.joinToString("") { "%02X".format(it) }
        val cardType = detectCardType(tag)
        var content = readNdefMessage(tag)

        // Intelligent check: If it's an ISO-DEP tag (like our emulation mode)
        val isoDep = IsoDep.get(tag)
        if (isoDep != null) {
            currentIsoDep = isoDep
            try {
                isoDep.connect()
                isoDep.timeout = 5000 
                
                val selectCommand = byteArrayOf(
                    0x00.toByte(), 0xA4.toByte(), 0x04.toByte(), 0x00.toByte(),
                    0x07.toByte(), 0xF0.toByte(), 0x01.toByte(), 0x02.toByte(), 0x03.toByte(), 0x04.toByte(), 0x05.toByte(), 0x06.toByte()
                )
                
                log("Handshaking with HCE Identity...")
                val response = isoDep.transceive(selectCommand)
                
                if (response.size >= 2 && response[response.size - 2] == 0x90.toByte() && response[response.size - 1] == 0x00.toByte()) {
                    val identity = String(response.copyOfRange(0, response.size - 2))
                    content = "Digital ID: $identity"
                    log("SUCCESS: Identity Received!")
                } else {
                    log("Tag responded but not with our ID.")
                }
            } catch (e: Exception) {
                log("HCE Link failed: ${e.message}")
            }
            callback?.onIsoDepDetected(isoDep)
        }
        
        callback?.onTagDiscovered(uid, cardType, content)
    }

    fun writeNdefMessage(tag: Tag, message: String): Boolean {
        val ndef = Ndef.get(tag) ?: return false
        return try {
            val record = NdefRecord.createTextRecord("en", message)
            val ndefMessage = NdefMessage(arrayOf(record))
            
            ndef.connect()
            if (!ndef.isWritable) {
                log("Tag is read-only")
                return false
            }
            if (ndef.maxSize < ndefMessage.byteArrayLength) {
                log("Message is too large for this tag")
                return false
            }
            
            ndef.writeNdefMessage(ndefMessage)
            log("Write Successful!")
            true
        } catch (e: Exception) {
            log("Write failed: ${e.message}")
            false
        } finally {
            try { ndef.close() } catch (e: Exception) {}
        }
    }

    private fun readNdefMessage(tag: Tag): String? {
        val ndef = Ndef.get(tag) ?: return null
        return try {
            ndef.connect()
            val ndefMessage = ndef.ndefMessage
            ndef.close()
            
            ndefMessage?.records?.joinToString("\n") { record ->
                val payload = record.payload
                if (payload.isNotEmpty()) {
                    String(payload, Charset.forName("UTF-8")).filter { it.isLetterOrDigit() || it.isWhitespace() }
                } else {
                    ""
                }
            }
        } catch (e: Exception) {
            Log.e("NfcCore", "Error reading NDEF", e)
            null
        }
    }

    private fun detectCardType(tag: Tag): String {
        val techList = tag.techList
        return when {
            techList.contains(IsoDep::class.java.name) -> "Smart Card (ISO-DEP)"
            techList.contains(MifareClassic::class.java.name) -> "Mifare Classic"
            techList.contains(MifareUltralight::class.java.name) -> "Mifare Ultralight"
            techList.contains(Ndef::class.java.name) -> "NDEF Tag"
            techList.contains(NfcA::class.java.name) -> "NFC-A"
            techList.contains(NfcB::class.java.name) -> "NFC-B"
            techList.contains(NfcF::class.java.name) -> "NFC-F"
            techList.contains(NfcV::class.java.name) -> "NFC-V"
            else -> "Unknown Tag"
        }
    }

    // Helper functions for HEX/Byte conversion
    fun hexStringToByteArray(s: String): ByteArray {
        val len = s.length
        val data = ByteArray(len / 2)
        var i = 0
        while (i < len) {
            data[i / 2] = ((Character.digit(s[i], 16) shl 4) + Character.digit(s[i + 1], 16)).toByte()
            i += 2
        }
        return data
    }

    fun byteArrayToHexString(bytes: ByteArray): String {
        return bytes.joinToString("") { "%02X".format(it) }
    }

    fun transceiveApdu(apduCommand: ByteArray): ByteArray? {
        return try {
            val isoDep = currentIsoDep ?: return null
            if (!isoDep.isConnected) isoDep.connect()
            isoDep.transceive(apduCommand)
        } catch (e: IOException) {
            Log.e("NfcCore", "APDU transceive failed", e)
            null
        }
    }

    fun transceiveNfcA(command: ByteArray): ByteArray? {
        val nfcA = NfcA.get(currentTag) ?: return null
        return try {
            if (!nfcA.isConnected) nfcA.connect()
            nfcA.transceive(command)
        } catch (e: IOException) {
            Log.e("NfcCore", "NfcA transceive failed", e)
            null
        } finally {
            try { nfcA.close() } catch (e: Exception) {}
        }
    }

    fun disconnectIsoDep() {
        try {
            currentIsoDep?.close()
            currentIsoDep = null
        } catch (e: IOException) {
            Log.e("NfcCore", "Error closing IsoDep", e)
        }
    }

    fun getPresenceToken(tag: Tag): String {
        return tag.id.joinToString("") { "%02X".format(it) }
    }
}
