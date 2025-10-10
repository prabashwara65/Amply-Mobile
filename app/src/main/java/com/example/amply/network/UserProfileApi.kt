package com.example.amply.network

import com.example.amply.model.OwnerProfile
import retrofit2.Call
import retrofit2.http.*

interface UserProfileApi {

    @GET("api/v1/userprofiles")
    fun getAllUsers(): Call<List<OwnerProfile>>

    @GET("api/v1/userprofiles")
    fun getUserByEmail(@Query("email") email: String): Call<List<OwnerProfile>>


    @GET("api/v1/userprofiles/{nic}")
    fun getUserByNIC(@Path("nic") nic: String): Call<OwnerProfile>

    @POST("api/v1/userprofiles")
    fun createUserProfile(@Body user: OwnerProfile): Call<Void>

    @PUT("api/v1/userprofiles/{nic}")
    fun updateUser(@Path("nic") nic: String, @Body user: OwnerProfile): Call<Void>

    @DELETE("api/v1/userprofiles/{nic}")
    fun deleteUser(@Path("nic") nic: String): Call<Void>

    @PUT("api/v1/userprofiles/{nic}/deactivate")
    fun deactivateUser(@Path("nic") nic: String): Call<Void>

    @PUT("api/v1/userprofiles/{nic}/request-reactivate")
    fun requestReactivateUser(@Path("nic") nic: String): Call<Void>

    @PUT("api/v1/userprofiles/{nic}/activate")
    fun activateUser(@Path("nic") nic: String): Call<Void>
}
