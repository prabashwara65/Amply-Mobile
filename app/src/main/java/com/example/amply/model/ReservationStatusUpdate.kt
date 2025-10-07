package com.example.amply.model

import com.google.gson.annotations.SerializedName

data class ReservationStatusUpdate(
    @SerializedName("status")
    val status: String
)
