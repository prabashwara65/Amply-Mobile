package com.example.amply.ui.reservation


/**
 * Data class for Reservation
 */
data class Reservation(
    val id: Int,
    val userId: Int,
    val stationId: Int,
    val stationName: String,
    val reservationDate: String,
    val reservationTime: String, // Keep for backward compatibility
    val startTime: String,       // Add this
    val endTime: String,         // Add this
    val status: String,
    val createdAt: String
)

/**
 * Data class for Charging Station
 */
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