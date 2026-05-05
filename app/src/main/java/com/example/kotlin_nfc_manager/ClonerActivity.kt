package com.example.kotlin_nfc_manager

import android.content.Context
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class ClonerActivity : AppCompatActivity() {

    private lateinit var nfcController: NfcController
    private lateinit var tvStatus: TextView
    private lateinit var tvData: TextView
    private lateinit var btnClone: MaterialButton
    
    private var capturedIdentity: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_cloner)

        tvStatus = findViewById(R.id.tv_cloner_status)
        tvData = findViewById(R.id.tv_cloned_data)
        btnClone = findViewById(R.id.btn_start_cloning)

        findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar_cloner).setNavigationOnClickListener {
            finish()
        }

        nfcController = NfcController(this)
        setupNfcCallback()

        btnClone.setOnClickListener {
            saveIdentityForCloning()
        }
    }

    private fun setupNfcCallback() {
        nfcController.onTagScanned = { uid, _, content ->
            runOnUiThread {
                capturedIdentity = content ?: uid
                tvStatus.text = "Card Captured!"
                tvData.text = "Identity: $capturedIdentity"
                btnClone.isEnabled = true
                Toast.makeText(this, "Original Data Captured", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveIdentityForCloning() {
        val identityToSave = capturedIdentity ?: return
        
        val prefs = getSharedPreferences("nfc_cloner", Context.MODE_PRIVATE)
        prefs.edit().putString("cloned_identity", identityToSave).apply()
        
        tvStatus.text = "Cloning Active!"
        tvData.text = "This phone is now emulating the captured card."
        btnClone.text = "Identity Saved to System"
        btnClone.isEnabled = false
        
        Toast.makeText(this, "Success! Your phone is now a clone.", Toast.LENGTH_LONG).show()
    }

    override fun onResume() {
        super.onResume()
        nfcController.startNfcSession()
    }

    override fun onPause() {
        super.onPause()
        nfcController.stopNfcSession()
    }
}
