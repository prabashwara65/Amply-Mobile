package com.example.amply.ui.dashboard

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.amply.R

class BackOfficeDashboard : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_back_office_dashboard)

        val tvTitle = findViewById<TextView>(R.id.tvTitle)
        tvTitle.text = "Welcome to Back Office Dashboard"
    }
}