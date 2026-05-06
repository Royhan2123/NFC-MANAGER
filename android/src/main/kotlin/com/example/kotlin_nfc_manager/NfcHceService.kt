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
        
        // Instruction Codes (ISO 7816-4)
        private const val INS_SELECT = 0xA4.toByte()
        private const val INS_READ_BINARY = 0xB0.toByte()
    }

    private var sessionActive = false

    override fun processCommandApdu(commandApdu: ByteArray?, extras: Bundle?): ByteArray {
        if (commandApdu == null || commandApdu.size < 4) return FAILURE_SW

        val ins = commandApdu[1]
        Log.d(TAG, "Incoming INS: ${"%02X".format(ins)}")

        // 1. Handle SELECT AID
        if (ins == INS_SELECT) {
            sessionActive = true
            val prefs = getSharedPreferences("NfcProPrefs", MODE_PRIVATE)
            val identity = prefs.getString("cloned_identity", "NFC-PRO-GENERIC") ?: "NFC-PRO-GENERIC"
            
            val payload = identity.toByteArray()
            val tlv = byteArrayOf(0x5F.toByte(), 0x20.toByte(), payload.size.toByte()) + payload
            return tlv + SUCCESS_SW
        }

        // 2. Handle READ BINARY (If session is active)
        if (sessionActive && ins == INS_READ_BINARY) {
            Log.i(TAG, "READ BINARY received. Returning data chunk.")
            // Simulate reading a data block
            val mockData = "SECURE-PAYLOAD-V2".toByteArray()
            return mockData + SUCCESS_SW
        }

        return if (sessionActive) SUCCESS_SW else UNKNOWN_SW
    }

    override fun onDeactivated(reason: Int) {
        sessionActive = false
    }
}
