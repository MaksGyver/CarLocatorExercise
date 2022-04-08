package com.example.android.car_locator.ui

import android.graphics.Color
import android.location.Location
import android.util.Log
import androidx.lifecycle.*
import androidx.lifecycle.Observer
import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import com.example.android.car_locator.constants.CONSTANTS.DATE_FORMAT
import com.example.android.car_locator.models.room_entities.VehicleLocationEntity
import com.example.android.car_locator.repository.back_end.RetrofitApi
import com.example.android.car_locator.repository.local_db.LocalDbRepository
import com.example.android.car_locator.repository.preferences.AppPreferences
import com.example.android.car_locator.ui.map.MapsFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.onStart
import retrofit2.HttpException
import java.net.URL
import java.util.*

class SharedViewModel(
    private val retrofitApi: RetrofitApi,
    private val preferences: AppPreferences,
    private val localDbRepository: LocalDbRepository
) : ViewModel() {

    companion object {
        const val TAG = "SharedViewModel"
    }

    private var _options = PolylineOptions()
    val options
        get() = _options

    private val _error = MutableLiveData("")
    val error: LiveData<String>
        get() = _error

    /**
     * observing error from database
     */
    private val dbErrorObserver = Observer<String> {
        _error.value = it
    }

    private val _routeLine = MutableLiveData<Polyline?>(null)
    val routeLine: LiveData<Polyline?>
        get() = _routeLine

    val listOfOwners = localDbRepository.getListOfOwners()
        .onStart { emit(listOf()) }
        .asLiveData(context = viewModelScope.coroutineContext + Dispatchers.IO)

    val listOfVehicles = localDbRepository.getListOfVehicles()
        .onStart { emit(listOf()) }
        .asLiveData(context = viewModelScope.coroutineContext + Dispatchers.IO)

    val listOfVehicleLocations = localDbRepository.getVehicleLocations()
        .onStart { emit(listOf()) }
        .asLiveData(context = viewModelScope.coroutineContext + Dispatchers.IO)

    private val _currentOwnerId = MutableLiveData<Long>()
    val currentOwnerId: LiveData<Long>
        get() = _currentOwnerId

    private val _currentVehicleId = MutableLiveData<Long>(0L)
    val currentVehicleId: LiveData<Long>
        get() = _currentVehicleId

    private val _lastKnownLocation = MutableLiveData<Location?>(null)
    val lastKnownLocation: LiveData<Location?>
        get() = _lastKnownLocation

    fun setLastKnownLocation(location: Location) {
        _lastKnownLocation.value = location
    }

    fun setCurrentOwnerId(id: Long) {
        _currentOwnerId.value = id
    }

    fun setCurrentVehicleId(id: Long) {
        _currentVehicleId.value = id
    }

    /**
     * Fetches from backend to local database if not done Today
     */
    fun dailyCashOfUserData() {
        val lastFetchDate = DATE_FORMAT.format(preferences.getLastUsersFetchDate())
        val todayDate = DATE_FORMAT.format(Date())
        if (lastFetchDate != todayDate) {
            cashUsersToLocalDb()
        } else {
            Log.d(TAG, "One day has not yet passed since last update")
        }
    }

    fun cashLocationToLocalDb(ownerId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = retrofitApi.getUserVehiclesLocations(ownerId)
                if (response.isSuccessful) {
                    response.body()?.data?.let {
                        localDbRepository.updateVehicleLocations(it)
                        Log.d(TAG, "Cashed vehicle locations")
                    }
                } else {
                    Log.d(TAG, "Error")
                }
            } catch (e: HttpException) {
                _error.postValue("Error reading data from server")
                Log.d(TAG, "Exception ${e.message}")
            } catch (e: Throwable) {
                _error.postValue("Error reading data from server")
                Log.d(TAG, "Ooops: Something else went wrong ${e.message.toString()}")
            }
        }
    }

    fun onErrorDismiss() {
        _error.postValue("")
    }

    private fun cashUsersToLocalDb() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
            val response = retrofitApi.getUsers()
                if (response.isSuccessful) {
                    Log.d(TAG, "Success")
                    response.body()?.data?.let { localDbRepository.updateUsers(it) }
                    preferences.updateLastUsersFetchDate()
                } else {
                    Log.d(TAG, "Error")
                }
            } catch (e: HttpException) {
                _error.postValue("Can't get data from the server")
                Log.d(TAG, "Exception ${e.message}")
            } catch (e: Throwable) {
                _error.postValue("Can't get data from the server")
                Log.d(TAG, "Ooops: Something else went wrong ${e.message.toString()}")
            }
        }
    }

    fun getVehicleLocationById(vehicleId: Long): LatLng? {
        var result: LatLng?
        runBlocking(Dispatchers.IO) {
            val location: VehicleLocationEntity? = localDbRepository.getVehicleLocationById(vehicleId)
            result = if (location != null) LatLng(location.lat, location.lon)
            else null
        }
        return result
    }

    fun drawRoute() {
        lastKnownLocation.value?.let { lastKnownLocation ->
            val deviceLatLang =
                LatLng(lastKnownLocation.latitude, lastKnownLocation.longitude)
            val vehicleLatLng =
                getVehicleLocationById(currentVehicleId.value ?: 0)
            _options = PolylineOptions()
            _options.color(Color.RED)
            _options.width(5f)
            val url = getURL(deviceLatLang, vehicleLatLng!!)
            Log.d(MapsFragment.TAG, url)
            CoroutineScope(Dispatchers.IO).launch {
                val result = URL(url).readText()
                Log.d(MapsFragment.TAG, "Result: $result")
                val parser: Parser = Parser.default()
                val stringBuilder: StringBuilder = StringBuilder(result)
                val json: JsonObject = parser.parse(stringBuilder) as JsonObject
                val routes = json.array<JsonObject>("routes")
                val points = routes!!["legs"]["steps"][0] as JsonArray<JsonObject>
                val polypts =
                    points.flatMap { decodePoly(it.obj("polyline")?.string("points")!!)!! }
                _options.add(deviceLatLang)
                for (point in polypts) _options.add(point)
                _options.add(vehicleLatLng)
                _routeLine.postValue(null)
            }
        }
    }

    private fun decodePoly(encoded: String): List<LatLng>? {
        val poly: MutableList<LatLng> = ArrayList()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0
        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].toInt() - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat
            shift = 0
            result = 0
            do {
                b = encoded[index++].toInt() - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng
            val p = LatLng(
                lat.toDouble() / 1E5,
                lng.toDouble() / 1E5
            )
            poly.add(p)
        }
        return poly
    }

    private fun getURL(from: LatLng, to: LatLng): String {
        val origin = "origin=" + from.latitude + "," + from.longitude
        val dest = "destination=" + to.latitude + "," + to.longitude
        val sensor = "sensor=false"
        val key = "key=AIzaSyD2WJAL8aAqeJzSoui6onWdsDF6v9vQP10"
        val params = "$origin&$dest&$sensor&$key"
        return "https://maps.googleapis.com/maps/api/directions/json?$params"
    }

    init {
        localDbRepository.error.observeForever(dbErrorObserver)
    }

    override fun onCleared() {
        localDbRepository.error.removeObserver(dbErrorObserver)
        super.onCleared()
    }
}