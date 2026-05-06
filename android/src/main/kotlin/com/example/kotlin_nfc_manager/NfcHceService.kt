package com.example.kotlin_nfc_manager

import android.nfc.cardemulation.HostApduService
import android.os.Bundle
import android.util.Log

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

        if (isSelectAidCommand(commandApdu)) {
            sessionActive = true
            
            val prefs = getSharedPreferences("NfcProPrefs", MODE_PRIVATE)
            val identity = prefs.getString("cloned_identity", "NFC-PRO-GENERIC") ?: "NFC-PRO-GENERIC"
            
            // Fix IMPROVEMENT 1: ISO7816-4 Compliant TLV Response
            // Format: [Tag (2 bytes)] [Length (1 byte)] [Value (N bytes)]
            // Using 0x5F20 (Display Name / Identity)
            val payload = identity.toByteArray()
            val tlvResponse = byteArrayOf(0x5F.toByte(), 0x20.toByte(), payload.size.toByte()) + payload
            
            return tlvResponse + SUCCESS_SW
        }

        return if (sessionActive) SUCCESS_SW else UNKNOWN_SW
    }

    override fun onDeactivated(reason: Int) {
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
