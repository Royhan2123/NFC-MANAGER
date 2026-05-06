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
                val identity = prefs.getString("cloned_identity", "NFC-PRO-ULTIMATE") ?: "NFC-PRO-ULTIMATE"
                
                val payload = identity.toByteArray()
                
                // Fix 3: Full ISO 7816 FCI Template (6F -> 84 -> A5)
                // [6F] FCI Template
                //   [84] DF Name (AID)
                //   [A5] Proprietary Template
                //     [50] Application Label (Identity)
                val aidBytes = hexStringToByteArray(TARGET_AID)
                val proprietaryTemplate = byteArrayOf(0x50.toByte(), payload.size.toByte()) + payload
                val proprietaryWrapper = byteArrayOf(0xA5.toByte(), proprietaryTemplate.size.toByte()) + proprietaryTemplate
                
                val dfNameWrapper = byteArrayOf(0x84.toByte(), aidBytes.size.toByte()) + aidBytes
                
                val fciContent = dfNameWrapper + proprietaryWrapper
                val fciTemplate = byteArrayOf(0x6F.toByte(), fciContent.size.toByte()) + fciContent
                
                return fciTemplate + SUCCESS_SW
            } else {
                return UNKNOWN_SW
            }
        }

        if (sessionActive && ins == INS_READ_BINARY) {
            val mockData = "ULTIMATE-SECURE-PAYLOAD-V5".toByteArray()
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

    private fun hexStringToByteArray(s: String): ByteArray {
        val len = s.length
        val data = ByteArray(len / 2)
        for (i in 0 until len step 2) {
            data[i / 2] = ((Character.digit(s[i], 16) shl 4) + Character.digit(s[i + 1], 16)).toByte()
        }
        return data
    }

    override fun onDeactivated(reason: Int) {
        sessionActive = false
    }
}
