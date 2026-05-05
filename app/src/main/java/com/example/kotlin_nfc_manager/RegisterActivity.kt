package com.example.kotlin_nfc_manager

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class RegisterActivity : AppCompatActivity() {

    private lateinit var nfcController: NfcController
    private lateinit var dbHelper: DatabaseHelper
    
    private lateinit var etName: TextInputEditText
    private lateinit var tvScannedUid: TextView
    private lateinit var btnSave: MaterialButton
    
    private var capturedUid: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)
        
        dbHelper = DatabaseHelper(this)
        nfcController = NfcController(this)
        
        etName = findViewById(R.id.et_name)
        tvScannedUid = findViewById(R.id.tv_scanned_uid)
        btnSave = findViewById(R.id.btn_save_registration)
        
        findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar_register).setNavigationOnClickListener {
            finish()
        }

        setupNfcCallback()
        setupSaveButton()
    }

    private fun setupNfcCallback() {
        nfcController.onTagScanned = { uid, _, _ ->
            runOnUiThread {
                capturedUid = uid
                tvScannedUid.text = uid
                Toast.makeText(this, "Tag Captured", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupSaveButton() {
        btnSave.setOnClickListener {
            val name = etName.text.toString().trim()
            val uid = capturedUid
            
            if (name.isEmpty()) {
                etName.error = "Name required"
                return@setOnClickListener
            }
            
            if (uid == null) {
                Toast.makeText(this, "Scan a tag first", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            
            val success = dbHelper.registerUser(name, uid)
            if (success) {
                Toast.makeText(this, "Registered: $name", Toast.LENGTH_LONG).show()
                finish()
            } else {
                Toast.makeText(this, "Registration failed", Toast.LENGTH_LONG).show()
            }
        }
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
