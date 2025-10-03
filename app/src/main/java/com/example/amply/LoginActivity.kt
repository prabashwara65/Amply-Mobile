package com.example.amply

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class LoginActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        // Handle system bars padding
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        dbHelper = DatabaseHelper(this)

        // Match IDs from XML
        val etUsername = findViewById<EditText>(R.id.username)
        val etPassword = findViewById<EditText>(R.id.password)
        val btnLogin = findViewById<Button>(R.id.loginBtn)

        btnLogin.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter both username and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Validate credentials
            if (dbHelper.validateUser(username, password)) {
                Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
                // Navigate to home activity or main dashboard
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Invalid username or password", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
