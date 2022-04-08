package com.example.android.car_locator.repository.local_db

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.android.car_locator.database.room_dao.LocalDatabaseDao
import com.example.android.car_locator.models.User
import com.example.android.car_locator.models.json.VehicleLocation
import com.example.android.car_locator.models.json.Vehicles
import com.example.android.car_locator.models.room_entities.OwnerEntity
import com.example.android.car_locator.models.room_entities.VehicleEntity
import com.example.android.car_locator.models.room_entities.VehicleLocationEntity

class LocalDbRepository(
    private val localDatabaseDao: LocalDatabaseDao
) {

    companion object {
        const val TAG = "LocalDbRepository"
    }

    private val _error = MutableLiveData("")
    val error: LiveData<String>
        get() = _error

    suspend fun updateUsers(list: List<User>) {
        validatedListOfUsers(list).forEach { user ->
            insertOwner(user.owner, user.userid)
            deleteVehicleIfNotOwnedAnymore(user.vehicles, user.userid)
            updateVehicles(user.vehicles, user.userid)
        }
    }

    suspend fun updateVehicleLocations(list: List<VehicleLocation>) {
        list.forEach { vehicleLocation ->
            if (vehicleLocation.lat != 0.0 && vehicleLocation.lat != 0.0) {
                localDatabaseDao.insertVehicleLocation(
                    VehicleLocationEntity(
                        vehicleId = vehicleLocation.vehicleId,
                        lat = vehicleLocation.lat,
                        lon = vehicleLocation.lon
                    )
                )
            } else {
                _error.postValue("No signal for vehicle: ${vehicleLocation.vehicleId}")
            }
        }
    }

    /**
     * returns list of users which are valid for further processing
     * currently verifies only userId, however can implement further verification as per requirements
     */
    private fun validatedListOfUsers(list: List<User>): List<User> =
        list.filter {
            (it.userid != 0L)
        }

    private suspend fun updateVehicles(
        vehiclesFromBackend: List<Vehicles>,
        ownerId: Long
    ) {
        vehiclesFromBackend.forEach {
            localDatabaseDao.insertVehicle(
                VehicleEntity(
                    vehicleId = it.vehicleid,
                    ownerId = ownerId,
                    make = it.make,
                    model = it.model,
                    year = it.year,
                    color = it.color,
                    vin = it.vin,
                    photo = it.foto
                )
            )
        }
    }

    /**
     * gets list of all cars which belong to current owner from database
     * and compares it against data from backend
     * if some cars do not belong to current owner anymore BUT still belong to same owner in localDb
     * the vehicle must be deleted
     */
    private suspend fun deleteVehicleIfNotOwnedAnymore(
        vehiclesFromBackend: List<Vehicles>,
        ownerId: Long
    ) {
        //getting list of all vehicles for current owner from local db
        val vehiclesFromDb = localDatabaseDao.getVehiclesByOwner(ownerId)
        //getting list of those vehicles which are not listed on backend for current user anymore
        val removedVehicles = vehiclesFromDb.filter { vehicleEntity ->
            vehiclesFromBackend.map { it.vehicleid }.contains(vehicleEntity.vehicleId)
        }
        //delete those vehicles where current owner is still specified as owner
        //as otherwise owner was changed and deletion of vehicle would be wrong
        removedVehicles.forEach {
            if (it.ownerId == ownerId) localDatabaseDao.deleteVehicle(it)
        }
    }

    private suspend fun insertOwner(
        owner: com.example.android.car_locator.models.json.Owner,
        ownerId: Long
    ) {
        localDatabaseDao.insertOwner(
            OwnerEntity(
                name = owner.name,
                surname = owner.surname,
                photoUrl = owner.foto,
                ownerId = ownerId
            )
        )
    }

    fun getListOfOwners() = localDatabaseDao.getListOfOwners()

    fun getListOfVehicles() = localDatabaseDao.getListOfVehicles()

    fun getVehicleLocations() =
        localDatabaseDao.getVehicleLocations()

    suspend fun getVehicleLocationById(id: Long) =
        localDatabaseDao.getVehicleLocationById(id)
}