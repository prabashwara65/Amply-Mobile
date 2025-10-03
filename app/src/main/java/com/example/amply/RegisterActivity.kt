package com.example.amply

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class RegisterActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)

        // Handle system bars padding
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        dbHelper = DatabaseHelper(this)

        // Match IDs from your XML
        val etUsername = findViewById<EditText>(R.id.username)
        val etPassword = findViewById<EditText>(R.id.password)
        val etConfirmPassword = findViewById<EditText>(R.id.confirmPassword)
        val btnRegister = findViewById<Button>(R.id.registerBtn)

        btnRegister.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()

            // Validate fields
            if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Check if user exists
            if (dbHelper.checkUser(username)) {
                Toast.makeText(this, "Username already exists", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Add user
            val success = dbHelper.addUser(username, password)
            if (success) {
                Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show()
                finish() // Close activity after successful registration
            } else {
                Toast.makeText(this, "Registration failed", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
