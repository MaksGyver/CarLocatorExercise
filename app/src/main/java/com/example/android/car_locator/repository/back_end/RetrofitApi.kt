package com.example.android.car_locator.repository.back_end

import com.example.android.car_locator.models.json.UserResponse
import com.example.android.car_locator.models.json.VehicleLocation
import com.example.android.car_locator.models.json.VehicleLocationResponse
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query


interface RetrofitApi {

    // GET - http://mobi.connectedcar360.net/api/?op=list

    @GET("api/?op=list")
    suspend fun getUsers(): Response<UserResponse>


    // http://mobi.connectedcar360.net/api/?op=getlocations&userid={userid}
    @GET("api/?op=getlocations&userid")
    suspend fun getUserVehiclesLocations(@Query("userid") userId: Long): Response<VehicleLocationResponse>

}