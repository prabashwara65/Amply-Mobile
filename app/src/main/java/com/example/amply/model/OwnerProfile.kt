package com.example.amply.model

import com.google.gson.annotations.SerializedName

data class OwnerProfile(
    @SerializedName("nic")
    val nic: String,

    @SerializedName("fullName")
    val fullName: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("password")
    val password: String,

    @SerializedName("phone")
    val phone: String,

    @SerializedName("status")
    val status: String = "active",

    @SerializedName("role")
    val role: String = "EvOwner"
)
