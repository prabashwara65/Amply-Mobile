package com.example.amply.model


import com.google.gson.annotations.SerializedName

data class ChargingStation(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("address") val address: String,
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("type") val type: String, // AC/DC
    @SerializedName("availableSlots") val availableSlots: Int,
    @SerializedName("totalSlots") val totalSlots: Int,
    @SerializedName("status") val status: String
)
