package com.example.android.car_locator.models.room_entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.android.car_locator.constants.CONSTANTS
import org.jetbrains.annotations.NotNull

/**
 * Separate entity as history of locations can be implemented later on if necessary
 */
@Entity(tableName = CONSTANTS.VEHICLE_LOCATION_TABLE_NAME)
data class VehicleLocationEntity(

    @NotNull
    @PrimaryKey
    @ColumnInfo(name = "vehicleId")
    val vehicleId: Long,

    @ColumnInfo(name = "lat")
    val lat: Double,

    @ColumnInfo(name = "lon")
    val lon: Double,

)