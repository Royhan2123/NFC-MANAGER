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
        
        // 1. Handle SELECT AID
        if (ins == INS_SELECT) {
            if (isValidAid(commandApdu)) {
                sessionActive = true
                val prefs = getSharedPreferences("NfcProPrefs", MODE_PRIVATE)
                val identity = prefs.getString("cloned_identity", "NFC-PRO-GOD-TIER") ?: "NFC-PRO-GOD-TIER"
                
                val payload = identity.toByteArray()
                val tlv = byteArrayOf(0x5F.toByte(), 0x20.toByte(), payload.size.toByte()) + payload
                return tlv + SUCCESS_SW
            } else {
                return UNKNOWN_SW
            }
        }

        // 2. Handle READ BINARY (With session state)
        if (sessionActive && ins == INS_READ_BINARY) {
            // Fix 5: Stateful HCE Response
            val mockData = "GOD-TIER-SECURE-PAYLOAD-V3".toByteArray()
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
