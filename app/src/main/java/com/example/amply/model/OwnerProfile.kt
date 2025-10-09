package com.example.amply.model

import com.google.gson.annotations.SerializedName

data class OwnerProfile(
    @SerializedName("NIC")
    val nic: String,

    @SerializedName("FullName")
    val fullName: String,

    @SerializedName("Email")
    val email: String,

    @SerializedName("Password")
    val password: String,

    @SerializedName("Phone")
    val phone: String,

    @SerializedName("Status")
    val status: String = "active",

    @SerializedName("Role")
    val role: String = "EvOwner"
)