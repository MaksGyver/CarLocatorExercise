package com.example.android.car_locator.models.room_entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.android.car_locator.constants.CONSTANTS
import org.jetbrains.annotations.NotNull

@Entity(tableName = CONSTANTS.VEHICLE_TABLE_NAME)
data class VehicleEntity(

    @NotNull
    @PrimaryKey(autoGenerate = false)
    val vehicleId: Long,

    @NotNull
    @ColumnInfo(name = "ownerId")
    val ownerId: Long,

    @ColumnInfo(name = "make")
    val make: String,

    @ColumnInfo(name = "model")
    val model: String,

    @ColumnInfo(name = "year")
    val year: Int,

    @ColumnInfo(name = "color")
    val color: String,

    @ColumnInfo(name = "vin")
    val vin: String,

    @ColumnInfo(name = "photo")
    val photo: String

)