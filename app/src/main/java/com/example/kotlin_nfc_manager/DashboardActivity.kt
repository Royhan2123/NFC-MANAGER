package com.example.kotlin_nfc_manager

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView

class DashboardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_dashboard)

        setupMenu()
    }

    private fun setupMenu() {
        findViewById<MaterialCardView>(R.id.card_access_control).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        findViewById<MaterialCardView>(R.id.card_writer).setOnClickListener {
            startActivity(Intent(this, WriterActivity::class.java))
        }

        findViewById<MaterialCardView>(R.id.card_vcard).setOnClickListener {
            startActivity(Intent(this, VCardActivity::class.java))
        }

        findViewById<MaterialCardView>(R.id.card_cloner).setOnClickListener {
            startActivity(Intent(this, ClonerActivity::class.java))
        }

        findViewById<MaterialCardView>(R.id.card_emoney).setOnClickListener {
            startActivity(Intent(this, EMoneyActivity::class.java))
        }

        findViewById<MaterialCardView>(R.id.card_emulation).setOnClickListener {
            startActivity(Intent(this, EmulationActivity::class.java))
        }
    }
}
