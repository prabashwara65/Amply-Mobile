package com.example.amply.repository

import android.content.Context
import com.example.amply.database.ReservationDatabaseHelper
import com.example.amply.model.Reservation
import com.example.amply.network.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class ReservationRepository (context: Context) {

    private val dbHelper = ReservationDatabaseHelper(context)
    private val apiService = ApiClient.reservationApiService

    //get all reservations(local first)
    fun getAllReservations(
        onSuccess: (List<Reservation>) -> Unit,
        onError: (String) -> Unit
    ) {
        //1.fetch from local database
        val localReservations = dbHelper.getAllReservations()
        if(localReservations.isNotEmpty()){
            onSuccess(localReservations)
        }

        //2.fetch from API to sync
        apiService.getAllReservations().enqueue(object : Callback<List<Reservation>> {
            override fun onResponse(
                call: Call<List<Reservation>>,
                response: Response<List<Reservation>>
            ) {
                if(response.isSuccessful && response.body() != null){
                    val remoteReservations = response.body()!!

                    //update local database
                    remoteReservations.forEach { reservation -> dbHelper.addReservation(reservation)}
                        onSuccess(remoteReservations)
                    } else {
                        onError("Failed to fetch reservations: ${response.code()}")
                    }
                }
                override fun onFailure(call: Call<List<Reservation>>, t: Throwable) {
                    onError("Network error: ${t.message}")
                }
        })
    }

    //Add new reservation
    fun addReservation(
        reservation: Reservation,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        //Insert Locally
        val added = dbHelper.addReservation(reservation)
        if(!added) {
            onError("Failed to save locally")
            return
        }

        //sync with backend
        apiService.createReservation(reservation).enqueue(object: Callback<Reservation>{
            override fun onResponse(call: Call<Reservation>, response: Response<Reservation>){
                if (response.isSuccessful){
                    onSuccess()
                } else {
                    onError("Failed to create on server: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<Reservation>, t: Throwable) {
                onError("Network error: ${t.message}")
            }
            })
    }

    //update reservation
    fun updateReservation(
        reservation: Reservation,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        // Update local DB
        val updated = dbHelper.updateReservationDetails(reservation)
        if (!updated) {
            onError("Failed to update locally")
            return
        }

        // Update backend
        apiService.updateReservation(reservation.reservationCode, reservation).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    onSuccess()
                } else {
                    onError("Failed to update on server: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                onError("Network error: ${t.message}")
            }
        })
    }

    //delete reservation
    fun deleteReservation(
        reservationCode: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        // Delete locally
        val deleted = dbHelper.deleteReservation(reservationCode)
        if (!deleted) {
            onError("Failed to delete locally")
            return
        }

        // Delete from backend
        apiService.deleteReservation(reservationCode).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    onSuccess()
                } else {
                    onError("Failed to delete on server: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                onError("Network error: ${t.message}")
            }
        })
    }
}