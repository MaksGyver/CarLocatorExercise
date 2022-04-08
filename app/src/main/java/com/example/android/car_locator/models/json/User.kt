package com.example.android.car_locator.models

import com.example.android.car_locator.models.json.Owner
import com.example.android.car_locator.models.json.Vehicles
import com.google.gson.annotations.SerializedName

data class User (
    @SerializedName("userid") val userid : Long,
    @SerializedName("owner") val owner : Owner,
    @SerializedName("vehicles") val vehicles : List<Vehicles>
)