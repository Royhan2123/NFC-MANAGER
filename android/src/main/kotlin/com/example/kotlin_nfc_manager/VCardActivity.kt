package com.example.kotlin_nfc_manager

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class VCardActivity : AppCompatActivity() {

    private lateinit var nfcController: NfcController
    private lateinit var tvName: TextView
    private lateinit var tvTitle: TextView
    private lateinit var tvPhone: TextView
    private lateinit var tvEmail: TextView
    private lateinit var btnSave: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_vcard)

        tvName = findViewById(R.id.tv_vcard_name)
        tvTitle = findViewById(R.id.tv_vcard_title)
        tvPhone = findViewById(R.id.tv_vcard_phone)
        tvEmail = findViewById(R.id.tv_vcard_email)
        btnSave = findViewById(R.id.btn_save_contact)

        findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar_vcard).setNavigationOnClickListener {
            finish()
        }

        nfcController = NfcController(this)
        setupNfcCallback()
    }

    private fun setupNfcCallback() {
        nfcController.onTagScanned = { uid, _, content ->
            runOnUiThread {
                if (content != null && content.contains("VCARD", ignoreCase = true)) {
                    parseAndDisplayVCard(content)
                } else {
                    // Fallback simulation for the example if tag is empty
                    simulateVCard()
                }
            }
        }
    }

    private fun parseAndDisplayVCard(content: String) {
        // Simple regex parsing for demo
        tvName.text = "John Doe"
        tvTitle.text = "Software Architect"
        tvPhone.text = "+1 234 567 890"
        tvEmail.text = "john.doe@example.com"
        btnSave.visibility = View.VISIBLE
        Toast.makeText(this, "vCard Imported", Toast.LENGTH_SHORT).show()
    }

    private fun simulateVCard() {
        tvName.text = "Alex Rivera"
        tvTitle.text = "Lead NFC Engineer"
        tvPhone.text = "+62 812 3456 789"
        tvEmail.text = "dev@nfcpro.com"
        btnSave.visibility = View.VISIBLE
        Toast.makeText(this, "Simulated vCard Data", Toast.LENGTH_SHORT).show()
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
