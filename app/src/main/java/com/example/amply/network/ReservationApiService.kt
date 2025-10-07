package com.example.amply.network

import com.example.amply.model.Reservation
import retrofit2.Call
import retrofit2.http.*

interface ReservationApiService {

    //get all reservations
    @GET("api/v1/reservations")
    fun getAllReservations() : Call<List<Reservation>>

    //get reservation by ID
    @GET("api/v1/reservations/{id}")
    fun getReservationById(@Path("id") id: String): Call<Reservation>

    //create new reservation
    @POST("api/v1/reservations")
    fun createReservation(@Body reservation: Reservation): Call<Reservation>

    //update reservation
    @PUT("api/v1/reservations/{id}")
    fun updateReservation(@Path("id") id: String, @Body reservation: Reservation): Call<Void>

    // Delete a reservation by ID
    @DELETE("api/v1/reservations/{id}")
    fun deleteReservation(@Path("id") id: String): Call<Void>

//    //Get reservations by station ID
//    @GET("api/v1/reservations/station/{stationId}")
//    fun getReservationsByStationId(@Path("stationId") stationId: String): Call<List<Reservation>>
//
//    // Get reservations by station name
//    @GET("api/v1/reservations/station-name/{stationName}")
//    fun getReservationsByStationName(@Path("stationName") stationName: String): Call<List<Reservation>>

//    // Get reservation status & QR code
//    @GET("api/v1/reservations/{id}/status")
//    fun getReservationStatus(@Path("id") id: String): Call<Reservation>
}