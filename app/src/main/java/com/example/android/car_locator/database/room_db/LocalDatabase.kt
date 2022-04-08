package com.example.android.car_locator.database.room_db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.android.car_locator.constants.CONSTANTS
import com.example.android.car_locator.database.room_dao.LocalDatabaseDao
import com.example.android.car_locator.models.room_entities.OwnerEntity
import com.example.android.car_locator.models.room_entities.VehicleEntity
import com.example.android.car_locator.models.room_entities.VehicleLocationEntity

@Database(
    version = CONSTANTS.DATABASE_VERSION,
    entities = [OwnerEntity::class, VehicleEntity::class, VehicleLocationEntity::class]
)

abstract class LocalDatabase: RoomDatabase() {
    abstract fun localDatabaseDao(): LocalDatabaseDao
}