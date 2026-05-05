package com.example.kotlin_nfc_manager

import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class WriterActivity : AppCompatActivity() {

    private lateinit var nfcController: NfcController
    private lateinit var etContent: EditText
    private lateinit var tvStatus: TextView
    private lateinit var btnPrepare: MaterialButton
    
    private var isWriteMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_writer)

        etContent = findViewById(R.id.et_write_content)
        tvStatus = findViewById(R.id.tv_write_status)
        btnPrepare = findViewById(R.id.btn_write_tag)

        findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar_writer).setNavigationOnClickListener {
            finish()
        }

        nfcController = NfcController(this)
        setupNfcCallback()

        btnPrepare.setOnClickListener {
            val content = etContent.text.toString()
            if (content.isEmpty()) {
                Toast.makeText(this, "Please enter some content", Toast.LENGTH_SHORT).show()
            } else {
                isWriteMode = true
                tvStatus.text = "READY: Tap your NFC tag now..."
                tvStatus.setTextColor(resources.getColor(R.color.ios_green, null))
                btnPrepare.isEnabled = false
            }
        }
    }

    private fun setupNfcCallback() {
        nfcController.onTagScanned = { _, _, _ ->
            if (isWriteMode) {
                runOnUiThread {
                    val content = etContent.text.toString()
                    val success = nfcController.writeNdef(content)
                    
                    if (success) {
                        tvStatus.text = "SUCCESS: Data written to tag!"
                        tvStatus.setTextColor(resources.getColor(R.color.ios_green, null))
                        Toast.makeText(this, "Write Successful", Toast.LENGTH_LONG).show()
                    } else {
                        tvStatus.text = "FAILED: Could not write to tag."
                        tvStatus.setTextColor(resources.getColor(R.color.ios_red, null))
                    }
                    
                    isWriteMode = false
                    btnPrepare.isEnabled = true
                }
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
