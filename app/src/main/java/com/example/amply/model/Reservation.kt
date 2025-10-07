package com.example.amply.model

import com.google.gson.annotations.SerializedName

data class Reservation(
    @SerializedName("id")
    val id: String? = null,

    @SerializedName("reservationCode")
    val reservationCode: String,

    @SerializedName("fullName")
    val fullName: String,

    @SerializedName("nic")
    val nic: String? = null,

    @SerializedName("vehicleNumber")
    val vehicleNumber: String,

    @SerializedName("stationId")
    val stationId : String,

    @SerializedName("stationName")
    val stationName: String,

    @SerializedName("slotNo")
    val slotNo : Int,

    @SerializedName("reservationDate")
    val reservationDate: String,

    @SerializedName("startTime")
    val startTime: String,

    @SerializedName("endTime")
    val endTime : String,

    @SerializedName("status")
    val status: String = "Pending",

    @SerializedName("qrCode")
    val qrCode: String? = null
)
