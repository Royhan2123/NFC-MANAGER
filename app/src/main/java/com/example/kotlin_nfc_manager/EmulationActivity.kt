package com.example.kotlin_nfc_manager

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class EmulationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_emulation)

        findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar_emulation).setNavigationOnClickListener {
            finish()
        }
    }
}
