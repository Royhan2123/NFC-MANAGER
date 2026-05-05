package com.example.kotlin_nfc_manager

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class EMoneyActivity : AppCompatActivity() {

    private lateinit var nfcController: NfcController
    private lateinit var tvBalance: TextView
    private lateinit var tvCardNumber: TextView
    private lateinit var historyContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_emoney)

        tvBalance = findViewById(R.id.tv_card_balance)
        tvCardNumber = findViewById(R.id.tv_card_number)
        historyContainer = findViewById(R.id.history_container)

        findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar_emoney).setNavigationOnClickListener {
            finish()
        }

        nfcController = NfcController(this)
        setupNfcCallback()
    }

    private fun setupNfcCallback() {
        nfcController.onTagScanned = { uid, cardType, _ ->
            runOnUiThread {
                // Simulate APDU reading process
                if (cardType.contains("ISO-DEP", ignoreCase = true)) {
                    readSmartCardData(uid)
                } else {
                    simulateSmartCard(uid)
                }
            }
        }
    }

    private fun readSmartCardData(uid: String) {
        // In a real app, you would send APDU commands here using nfcController.transceiveApdu()
        // Example: SELECT PPSE, then Read Balance record
        tvBalance.text = "Rp 125.500"
        tvCardNumber.text = "6032 12** **** ${uid.takeLast(4)}"
        addHistoryItem("Toll Road", "- Rp 15.000", "Today, 10:24")
        addHistoryItem("Indomaret", "- Rp 35.000", "Yesterday, 18:45")
        Toast.makeText(this, "Balance Updated", Toast.LENGTH_SHORT).show()
    }

    private fun simulateSmartCard(uid: String) {
        tvBalance.text = "Rp 75.000"
        tvCardNumber.text = "9988 77** **** ${uid.takeLast(4)}"
        historyContainer.removeAllViews()
        addHistoryItem("Busway", "- Rp 3.500", "Just now")
        Toast.makeText(this, "Simulated Card Read", Toast.LENGTH_SHORT).show()
    }

    private fun addHistoryItem(title: String, amount: String, date: String) {
        val view = layoutInflater.inflate(android.R.layout.simple_list_item_2, null)
        view.findViewById<TextView>(android.R.id.text1).apply {
            text = title
            setTextColor(resources.getColor(R.color.white, null))
        }
        view.findViewById<TextView>(android.R.id.text2).apply {
            text = "$amount • $date"
            setTextColor(resources.getColor(R.color.ios_label_secondary, null))
        }
        historyContainer.addView(view, 0)
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
