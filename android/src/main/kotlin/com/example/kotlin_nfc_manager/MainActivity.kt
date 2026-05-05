package com.example.kotlin_nfc_manager

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast
import android.util.Log
import android.widget.TextView
import android.widget.ImageView
import android.graphics.Color
import android.widget.ScrollView

class MainActivity : AppCompatActivity() {

    private lateinit var nfcController: NfcController
    private lateinit var dbHelper: DatabaseHelper
    
    private lateinit var statusText: TextView
    private lateinit var statusIcon: ImageView
    private lateinit var uidLabel: TextView
    private lateinit var typeLabel: TextView
    private lateinit var contentLabel: TextView
    private lateinit var logText: TextView
    private lateinit var logScroll: ScrollView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        
        dbHelper = DatabaseHelper(this)
        
        statusText = findViewById(R.id.status_text)
        statusIcon = findViewById(R.id.status_icon)
        uidLabel = findViewById(R.id.uid_label)
        typeLabel = findViewById(R.id.type_label)
        contentLabel = findViewById(R.id.content_label)
        logText = findViewById(R.id.log_text)
        logScroll = findViewById(R.id.log_scroll)

        findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar_main).setNavigationOnClickListener {
            finish()
        }

        nfcController = NfcController(this)
        setupCallbacks()
        
        log("Security scan module active.")
    }

    private fun setupCallbacks() {
        nfcController.onTagScanned = { uid, cardType, content ->
            runOnUiThread {
                val registeredName = dbHelper.getUserNameByUid(uid)
                val parsedResult = NfcDataParser.parse(content)
                
                uidLabel.text = uid
                typeLabel.text = cardType
                contentLabel.text = parsedResult.displayValue
                
                if (registeredName != null) {
                    statusText.text = "Authorized: $registeredName"
                    statusText.setTextColor(resources.getColor(R.color.ios_green, null))
                    statusIcon.setColorFilter(resources.getColor(R.color.ios_green, null))
                    log("Verified Access: $registeredName scanned")
                } else {
                    statusText.text = parsedResult.label
                    statusText.setTextColor(resources.getColor(R.color.ios_blue, null))
                    statusIcon.setColorFilter(resources.getColor(R.color.ios_blue, null))
                    log("Tag detected - Type: $cardType")
                }
            }
        }

        nfcController.onErrorEvent = { message ->
            runOnUiThread {
                statusText.text = "Error"
                statusText.setTextColor(resources.getColor(R.color.ios_red, null))
                statusIcon.setColorFilter(resources.getColor(R.color.ios_red, null))
                log("Hardware Event: $message")
            }
        }

        nfcController.onLogEvent = { message ->
            runOnUiThread {
                log(message)
            }
        }
    }

    private fun log(message: String) {
        runOnUiThread {
            val currentText = logText.text.toString()
            val newLog = "$currentText\n> $message"
            logText.text = newLog
            
            // Auto-scroll to bottom
            logScroll.post {
                logScroll.fullScroll(ScrollView.FOCUS_DOWN)
            }
            Log.i("NFC_PRO", message)
        }
    }

    override fun onResume() {
        super.onResume()
        nfcController.startNfcSession()
        statusText.text = "Searching..."
        statusText.setTextColor(Color.GRAY)
        statusIcon.setColorFilter(resources.getColor(R.color.ios_blue, null))
        
        val pulse = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.pulse)
        statusIcon.startAnimation(pulse)
    }

    override fun onPause() {
        super.onPause()
        nfcController.stopNfcSession()
        statusIcon.clearAnimation()
    }
}