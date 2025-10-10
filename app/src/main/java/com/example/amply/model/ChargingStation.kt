package com.example.amply.model

data class ChargingStation(
    val id: Int,
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val type: String,
    val availableSlots: Int,
    val totalSlots: Int,
    val status: String
)
