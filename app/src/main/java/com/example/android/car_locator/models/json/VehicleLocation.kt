package com.example.android.car_locator.models.json

import com.google.gson.annotations.SerializedName
import java.sql.Timestamp

data class VehicleLocation(
    @SerializedName("vehicleid") val vehicleId : Long,
    @SerializedName("lat") val lat : Double,
    @SerializedName("lon") val lon : Double
)