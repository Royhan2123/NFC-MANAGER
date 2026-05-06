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
        
        private const val INS_SELECT = 0xA4.toByte()
        private const val INS_READ_BINARY = 0xB0.toByte()
        private const val TARGET_AID = "F0010203040506"
    }

    private var sessionActive = false

    override fun processCommandApdu(commandApdu: ByteArray?, extras: Bundle?): ByteArray {
        if (commandApdu == null || commandApdu.size < 4) return FAILURE_SW

        val ins = commandApdu[1]
        
        if (ins == INS_SELECT) {
            if (isValidAid(commandApdu)) {
                sessionActive = true
                val prefs = getSharedPreferences("NfcProPrefs", MODE_PRIVATE)
                val identity = prefs.getString("cloned_identity", "NFC-PRO-DIAMOND") ?: "NFC-PRO-DIAMOND"
                
                val payload = identity.toByteArray()
                
                // Fix 5: ISO 7816-4 Compliant FCI Template (6F)
                // Format: [6F] [Len] [84] [Len AID] [AID] [A5] [Len Prop] ...
                // For simplicity, we wrap the identity in a standard FCI structure
                val fci = byteArrayOf(
                    0x6F.toByte(), (payload.size + 2).toByte(),
                    0x84.toByte(), payload.size.toByte()
                ) + payload
                
                return fci + SUCCESS_SW
            } else {
                return UNKNOWN_SW
            }
        }

        if (sessionActive && ins == INS_READ_BINARY) {
            val mockData = "DIAMOND-SECURE-PAYLOAD-V4".toByteArray()
            return mockData + SUCCESS_SW
        }

        return if (sessionActive) SUCCESS_SW else UNKNOWN_SW
    }

    private fun isValidAid(apdu: ByteArray): Boolean {
        if (apdu.size < 6) return false
        val lc = apdu[4].toInt() and 0xFF
        if (apdu.size < 5 + lc) return false
        val aidBytes = apdu.copyOfRange(5, 5 + lc)
        val receivedAid = aidBytes.joinToString("") { "%02X".format(it) }
        return receivedAid == TARGET_AID
    }

    override fun onDeactivated(reason: Int) {
        sessionActive = false
    }
}
