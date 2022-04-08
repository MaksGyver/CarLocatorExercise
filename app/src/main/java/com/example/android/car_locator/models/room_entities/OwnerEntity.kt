package com.example.android.car_locator.models.room_entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.android.car_locator.constants.CONSTANTS
import org.jetbrains.annotations.NotNull


@Entity(tableName = CONSTANTS.OWNER_TABLE_NAME)
data class OwnerEntity(

    @NotNull
    @PrimaryKey(autoGenerate = false)
    val ownerId: Long,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "surname")
    val surname: String,

    @ColumnInfo(name = "photoUrl")
    val photoUrl: String
)