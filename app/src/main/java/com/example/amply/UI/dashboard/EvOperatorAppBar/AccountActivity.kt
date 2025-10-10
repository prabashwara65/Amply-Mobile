package com.example.amply.ui.dashboard.EvOperatorAppBar

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.amply.R
import com.example.amply.data.AuthDatabaseHelper
import com.example.amply.model.OwnerProfile
import com.example.amply.network.RetrofitClient
import com.example.amply.network.UserProfileApi
import com.google.gson.JsonParser
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AccountActivity : AppCompatActivity() {

    private lateinit var dbHelper: AuthDatabaseHelper
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etFullName: EditText
    private lateinit var etNic: EditText
    private lateinit var etPhone: EditText
    private lateinit var btnUpdate: Button
    private lateinit var btnStatus: Button
    private lateinit var btnRequestReactivate: Button

    private lateinit var userProfileApi: UserProfileApi
    private var currentUser: OwnerProfile? = null
    private var statusToSend: String = "active" // default status

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_account)

        dbHelper = AuthDatabaseHelper(this)
        userProfileApi = RetrofitClient.instance.create(UserProfileApi::class.java)

        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        etFullName = findViewById(R.id.etFullName)
        etNic = findViewById(R.id.etNic)
        etPhone = findViewById(R.id.etPhone)
        btnUpdate = findViewById(R.id.btnUpdate)
        btnStatus = findViewById(R.id.btnStatus)
        btnRequestReactivate = findViewById(R.id.btnRequestReactivate)

        btnRequestReactivate.visibility = View.GONE // hide initially

        loadUserData()

        btnStatus.setOnClickListener {
            // Mark locally as deactive
            statusToSend = "deactive"
            btnStatus.text = statusToSend
            btnStatus.isEnabled = false
            btnStatus.setBackgroundColor(resources.getColor(android.R.color.darker_gray))
            Toast.makeText(this, "Status set to deactive. Click Update to apply.", Toast.LENGTH_SHORT).show()

            // Show "Request Reactivation" button
            btnRequestReactivate.visibility = View.VISIBLE
        }

        btnRequestReactivate.setOnClickListener {
            // Only show Toast, no DB change
            Toast.makeText(this, "Reactivation request sent to admin", Toast.LENGTH_LONG).show()
            Log.d("AccountActivity", "Reactivation request sent for ${currentUser?.email}")
        }

        btnUpdate.setOnClickListener {
            updateUserDetails()
        }
    }

    private fun loadUserData() {
        val loggedEmail = dbHelper.getLoggedInUserEmail()
        if (loggedEmail == null) {
            Toast.makeText(this, "No logged-in user found", Toast.LENGTH_SHORT).show()
            return
        }

        userProfileApi.getAllUsers().enqueue(object : Callback<List<OwnerProfile>> {
            override fun onResponse(
                call: Call<List<OwnerProfile>>,
                response: Response<List<OwnerProfile>>
            ) {
                if (response.isSuccessful) {
                    val users = response.body() ?: emptyList()
                    currentUser = users.find { it.email.equals(loggedEmail, ignoreCase = true) }
                    currentUser?.let { populateFields(it) } ?: run {
                        Toast.makeText(this@AccountActivity, "User not found on server", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@AccountActivity, "Failed to fetch users from server", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<OwnerProfile>>, t: Throwable) {
                Toast.makeText(this@AccountActivity, "Error fetching users: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun populateFields(user: OwnerProfile) {
        etEmail.setText(user.email)
        etPassword.setText(user.password)
        etFullName.setText(user.fullName)
        etNic.setText(user.nic)
        etPhone.setText(user.phone)

        statusToSend = user.status.lowercase()
        btnStatus.text = statusToSend
        btnStatus.isEnabled = statusToSend != "deactive"
        btnStatus.setBackgroundColor(
            if (statusToSend == "deactive") resources.getColor(android.R.color.darker_gray)
            else resources.getColor(android.R.color.holo_green_dark)
        )

        // Show request reactivation button if user is already deactive
        btnRequestReactivate.visibility = if (statusToSend == "deactive") View.VISIBLE else View.GONE
    }

    private fun updateUserDetails() {
        val newEmail = etEmail.text.toString().trim()
        val newPassword = etPassword.text.toString().trim()
        val newFullName = etFullName.text.toString().trim()
        val newNic = etNic.text.toString().trim()
        val newPhone = etPhone.text.toString().trim()

        if (newEmail.isEmpty() || newPassword.isEmpty() || newFullName.isEmpty() || newNic.isEmpty() || newPhone.isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
            return
        }

        if (currentUser == null) {
            Toast.makeText(this, "No user loaded to update", Toast.LENGTH_SHORT).show()
            return
        }

        val updatedUser = OwnerProfile(
            nic = newNic,
            fullName = newFullName,
            email = newEmail,
            password = newPassword,
            phone = newPhone,
            status = statusToSend,
            role = currentUser!!.role
        )

        Log.d("AccountActivity", "Sending update -> Email: $newEmail, Status: $statusToSend")

        userProfileApi.updateUser(currentUser!!.nic, updatedUser)
            .enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@AccountActivity, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                        currentUser = updatedUser
                        btnRequestReactivate.visibility = if (statusToSend == "deactive") View.VISIBLE else View.GONE
                    } else {
                        val errorBody = response.errorBody()?.string()
                        var errorMessage = "Unknown error occurred"
                        errorBody?.let {
                            try {
                                val json = JsonParser.parseString(it).asJsonObject
                                if (json.has("message")) {
                                    errorMessage = json.get("message").asString
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                        Toast.makeText(this@AccountActivity, "Update failed: $errorMessage", Toast.LENGTH_LONG).show()
                        Log.d("AccountActivity", "Error body: $errorBody")
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Toast.makeText(this@AccountActivity, "Network error: ${t.message}", Toast.LENGTH_LONG).show()
                    Log.d("AccountActivity", "Network error: ${t.message}")
                }
            })
    }
}
