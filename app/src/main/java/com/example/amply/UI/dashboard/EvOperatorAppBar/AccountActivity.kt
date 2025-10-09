package com.example.amply.ui.dashboard.EvOperatorAppBar

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.amply.R
import com.example.amply.data.AuthDatabaseHelper
import com.example.amply.network.RetrofitClient
import com.example.amply.network.UserProfileApi
import com.example.amply.model.OwnerProfile
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AccountActivity : AppCompatActivity() {

    private lateinit var dbHelper: AuthDatabaseHelper
    private lateinit var etFullName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPhone: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnUpdate: Button

    private lateinit var userProfileApi: UserProfileApi

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_account) // reuse fragment layout

        dbHelper = AuthDatabaseHelper(this)
        userProfileApi = RetrofitClient.instance.create(UserProfileApi::class.java)

        etFullName = findViewById(R.id.etFullName)
        etEmail = findViewById(R.id.etEmail)
        etPhone = findViewById(R.id.etPhone)
        etPassword = findViewById(R.id.etPassword)
        btnUpdate = findViewById(R.id.btnUpdate)

        loadUserData()

        btnUpdate.setOnClickListener { updateUser() }
    }

    private fun loadUserData() {
        val email = dbHelper.getLoggedInUserEmail() ?: return
        etEmail.setText(email)
        etFullName.setText(dbHelper.getUserFullName(email))
        etPhone.setText(dbHelper.getUserPhone(email))
        etPassword.setText(dbHelper.getUserPassword(email))
    }

    private fun updateUser() {
        val email = etEmail.text.toString().trim()
        val fullName = etFullName.text.toString().trim()
        val phone = etPhone.text.toString().trim()
        val password = etPassword.text.toString().trim()
        val role = dbHelper.getUserRole(email)

        if (email.isEmpty() || fullName.isEmpty() || phone.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
            return
        }

        val user = OwnerProfile(
            nic = dbHelper.getUserNIC(email),
            fullName = fullName,
            email = email,
            phone = phone,
            password = password,
            role = role
        )

        // Optional: update MongoDB here
    }
}
