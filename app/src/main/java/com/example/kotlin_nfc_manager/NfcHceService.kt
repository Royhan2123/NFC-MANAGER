package com.example.kotlin_nfc_manager

import android.nfc.cardemulation.HostApduService
import android.os.Bundle
import android.util.Log

/**
 * Host Card Emulation (HCE) Service.
 * This allows the Android device to act as an NFC Smart Card.
 */
class NfcHceService : HostApduService() {

    companion object {
        private const val TAG = "NfcHceService"
        
        // Example AID for our Pro Package (must match apduservice.xml)
        private const val AID = "F0010203040506"
        
        // Command for SELECT AID
        private val SELECT_APDU = byteArrayOf(
            0x00.toByte(), 0xA4.toByte(), 0x04.toByte(), 0x00.toByte(), 
            0x07.toByte(), // length
            0xF0.toByte(), 0x01.toByte(), 0x02.toByte(), 0x03.toByte(), 0x04.toByte(), 0x05.toByte(), 0x06.toByte()
        )
        
        private val SUCCESS_RESPONSE = byteArrayOf(0x90.toByte(), 0x00.toByte())
        private val ERROR_RESPONSE = byteArrayOf(0x6F.toByte(), 0x00.toByte())
    }

    /**
     * Called when a reader sends an APDU to our phone.
     */
    override fun processCommandApdu(commandApdu: ByteArray?, extras: Bundle?): ByteArray {
        if (commandApdu == null) return ERROR_RESPONSE

        val hexCommand = commandApdu.joinToString("") { "%02X".format(it) }
        Log.d(TAG, "Received APDU: $hexCommand")

        // Check if it's a SELECT AID command
        return if (commandApdu.contentEquals(SELECT_APDU)) {
            Log.i(TAG, "AID Selected! Returning cloned identity.")
            
            // Retrieve the captured identity from SharedPreferences
            val prefs = getSharedPreferences("nfc_cloner", MODE_PRIVATE)
            val identityString = prefs.getString("cloned_identity", "NFC-PRO-DEFAULT-ID") ?: "NFC-PRO-DEFAULT-ID"
            
            val identity = identityString.toByteArray()
            identity + SUCCESS_RESPONSE
        } else {
            SUCCESS_RESPONSE
        }
    }

    override fun onDeactivated(reason: Int) {
        Log.d(TAG, "HCE Deactivated: reason=$reason")
    }
}
