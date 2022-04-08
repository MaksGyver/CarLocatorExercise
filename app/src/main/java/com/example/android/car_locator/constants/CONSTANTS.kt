package com.example.android.car_locator.constants

import com.google.android.gms.maps.model.LatLng
import java.text.SimpleDateFormat

object CONSTANTS {

    //UTILS
    val DATE_FORMAT = SimpleDateFormat("dd/MM/yyyy")
    val TIME_FORMAT = SimpleDateFormat("HH:mm:ss")

    //LOCAL DATABASE
    const val LOCAL_DATABASE_NAME = "car_locator_db"
    const val DATABASE_VERSION = 1
    const val OWNER_TABLE_NAME = "owners"
    const val VEHICLE_TABLE_NAME = "vehicles"
    const val VEHICLE_LOCATION_TABLE_NAME = "locations"

    //MAP
    val DEFAULT_LOCATION = LatLng(56.946285, 24.105078)
    const val MAP_REFRESH_INTERVAL = 30_000L
}