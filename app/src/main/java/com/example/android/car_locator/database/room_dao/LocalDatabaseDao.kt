package com.example.android.car_locator.database.room_dao

import androidx.room.*
import com.example.android.car_locator.constants.CONSTANTS
import com.example.android.car_locator.models.json.VehicleLocation
import com.example.android.car_locator.models.room_entities.OwnerEntity
import com.example.android.car_locator.models.room_entities.VehicleEntity
import com.example.android.car_locator.models.room_entities.VehicleLocationEntity
import kotlinx.coroutines.flow.Flow
import java.security.acl.Owner

@Dao
interface LocalDatabaseDao {

    @Insert (onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOwner(ownerEntity: OwnerEntity)

    @Insert (onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVehicle(vehicleEntity: VehicleEntity)

    @Insert (onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVehicleLocation(vehicleLocationEntity: VehicleLocationEntity)

    @Query("SELECT * FROM ${CONSTANTS.VEHICLE_TABLE_NAME} WHERE ownerId = :ownerId")
    suspend fun getVehiclesByOwner(ownerId: Long): List<VehicleEntity>

    @Delete
    suspend fun deleteVehicle(it: VehicleEntity)

    @Query("SELECT * FROM ${CONSTANTS.OWNER_TABLE_NAME} ORDER BY surname DESC")
    fun getListOfOwners(): Flow<List<OwnerEntity>>

    @Query("SELECT * FROM ${CONSTANTS.VEHICLE_TABLE_NAME} ORDER BY make DESC")
    fun getListOfVehicles(): Flow<List<VehicleEntity>>

    @Query("SELECT * FROM ${CONSTANTS.VEHICLE_LOCATION_TABLE_NAME} WHERE vehicleId = :vehicleId LIMIT 1")
    suspend fun getVehicleLocationById(vehicleId: Long): VehicleLocationEntity

    @Query("SELECT * FROM ${CONSTANTS.VEHICLE_LOCATION_TABLE_NAME}")
    fun getVehicleLocations(): Flow<List<VehicleLocationEntity>>
}

