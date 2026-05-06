package com.example.kotlin_nfc_manager

import android.nfc.cardemulation.HostApduService
import android.os.Bundle
import android.util.Log

/**
 * Host Card Emulation (HCE) Service - Version 2.0
 * Improved state handling and professional response formatting.
 */
class NfcHceService : HostApduService() {

    companion object {
        private const val TAG = "NfcHceService"
        private val SUCCESS_SW = byteArrayOf(0x90.toByte(), 0x00.toByte())
        private val FAILURE_SW = byteArrayOf(0x6F.toByte(), 0x00.toByte())
        private val UNKNOWN_SW = byteArrayOf(0x6D.toByte(), 0x00.toByte())
    }

    private var sessionActive = false

    override fun processCommandApdu(commandApdu: ByteArray?, extras: Bundle?): ByteArray {
        if (commandApdu == null) return FAILURE_SW

        val hexCommand = commandApdu.joinToString("") { "%02X".format(it) }
        Log.d(TAG, "Incoming APDU: $hexCommand")

        // 1. Handle SELECT AID (Standard ISO 7816-4)
        if (isSelectAidCommand(commandApdu)) {
            sessionActive = true
            Log.i(TAG, "AID Selected. Opening Secure Session.")
            
            val prefs = getSharedPreferences("NfcProPrefs", MODE_PRIVATE)
            val identity = prefs.getString("cloned_identity", "NFC-PRO-GENERIC") ?: "NFC-PRO-GENERIC"
            
            return identity.toByteArray() + SUCCESS_SW
        }

        // 2. Handle Custom Data Requests (if session is active)
        if (sessionActive) {
            // Here you could add logic for AUTH, READ_BINARY, etc.
            // For now, return generic success for any command in active session
            return SUCCESS_SW
        }

        return UNKNOWN_SW
    }

    override fun onDeactivated(reason: Int) {
        Log.d(TAG, "HCE Session Deactivated: reason=$reason")
        sessionActive = false
    }

    private fun isSelectAidCommand(apdu: ByteArray): Boolean {
        return apdu.size >= 4 && 
               apdu[0] == 0x00.toByte() && 
               apdu[1] == 0xA4.toByte() && 
               apdu[2] == 0x04.toByte() && 
               apdu[3] == 0x00.toByte()
    }
}
