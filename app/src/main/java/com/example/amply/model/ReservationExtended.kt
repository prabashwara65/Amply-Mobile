package com.example.amply.model

import com.google.gson.annotations.SerializedName

data class ReservationExtended(
    @SerializedName("id") val id: String,
    @SerializedName("reservationCode") val reservationCode: String,
    @SerializedName("fullName") val fullName: String,
    @SerializedName("nic") val nic: String?,
    @SerializedName("vehicleNumber") val vehicleNumber: String,
    @SerializedName("stationId") val stationId: String,
    @SerializedName("stationName") val stationName: String,
    @SerializedName("slotNo") val slotNo: Int,
    @SerializedName("bookingDate") val bookingDate: String,
    @SerializedName("reservationDate") val reservationDate: String,
    @SerializedName("startTime") val startTime: String,
    @SerializedName("endTime") val endTime: String,
    @SerializedName("status") val status: String,
    @SerializedName("qrCode") val qrCode: String?,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("updatedAt") val updatedAt: String
)

