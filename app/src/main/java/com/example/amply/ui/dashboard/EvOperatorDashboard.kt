package com.example.amply.ui.dashboard

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.amply.R
import com.example.amply.ui.EVOperatorApp.QRScannerActivity

class EvOperatorDashboard : AppCompatActivity() {

    private lateinit var btnStartScanning: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ev_operator_dashboard)

        initializeViews()
        setupClickListeners()
    }

    private fun initializeViews() {
        btnStartScanning = findViewById(R.id.btnStartScanning)
    }

    private fun setupClickListeners() {
        btnStartScanning.setOnClickListener {
            startQRScanner()
        }
    }

    private fun startQRScanner() {
        val intent = Intent(this, QRScannerActivity::class.java)
        startActivity(intent)
    }
}