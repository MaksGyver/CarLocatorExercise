package com.example.android.car_locator.ui.map

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.android.car_locator.R
import com.example.android.car_locator.constants.CONSTANTS.DEFAULT_LOCATION
import com.example.android.car_locator.constants.CONSTANTS.MAP_REFRESH_INTERVAL
import com.example.android.car_locator.databinding.FragmentMapsBinding
import com.example.android.car_locator.models.room_entities.VehicleEntity
import com.example.android.car_locator.ui.SharedViewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.material.bottomsheet.BottomSheetDialog
import org.koin.androidx.viewmodel.ext.android.sharedViewModel


class MapsFragment : Fragment() {

    companion object {
        const val TAG = "MapsFragment"
    }

    private var mMap: GoogleMap? = null

    private var routeVisible = false

    private val viewModel by sharedViewModel<SharedViewModel>()

    private val timer: CountDownTimer = object : CountDownTimer(MAP_REFRESH_INTERVAL, 1000) {
        override fun onTick(millisUntilFinished: Long) {
        }
        override fun onFinish() {
            viewModel.currentOwnerId.value?.let {
                Log.d(TAG, "Cashing to DB")
                showToast("Refreshing locations")
                viewModel.cashLocationToLocalDb(it)
                start()
            }
        }
    }

    private var _binding: FragmentMapsBinding? = null
    private val binding get() = _binding!!

    private val fusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(
            requireActivity()
        )
    }

    private var line: Polyline? = null

    private val currentMarkers = mutableListOf<Marker>()

    @SuppressLint("MissingPermission")
    private val callback = OnMapReadyCallback { googleMap ->
        drawMarkers(googleMap, true)
    }

    private fun drawMarkers(googleMap: GoogleMap, moveCamera: Boolean) {
        mMap = googleMap
        currentMarkers.forEach{it.remove()}
        currentMarkers.clear()

        val listOfCurrentOwnerVehicles = viewModel.listOfVehicles.value?.filter {
            it.ownerId == viewModel.currentOwnerId.value
        }
        listOfCurrentOwnerVehicles?.forEach { vehicleEntity ->
            val vehicleLocation = viewModel.getVehicleLocationById(vehicleEntity.vehicleId)
            vehicleLocation?.let {
                val marker = mMap?.addMarker(
                    MarkerOptions()
                        .position(vehicleLocation)
                        .title(getVehicleName(vehicleEntity))
                )
                marker?.tag = vehicleEntity
                marker?.let {
                    currentMarkers.add(it)
                }
            }
        }
        mMap?.setOnMarkerClickListener { marker ->
            val vehicle: VehicleEntity = marker.tag as VehicleEntity
            viewModel.setCurrentVehicleId(vehicle.vehicleId)
            if (marker.isInfoWindowShown) {
                marker.hideInfoWindow()
            } else {
                marker.showInfoWindow()
                mMap?.moveCamera(CameraUpdateFactory.newLatLng(marker.position))
            }
            true
        }
        mMap?.apply {
            if (moveCamera) moveCamera(CameraUpdateFactory.newLatLng(listOfCurrentOwnerVehicles?.get(0)?.let {
                viewModel.getVehicleLocationById(it.vehicleId)
            } ?: DEFAULT_LOCATION))


            setInfoWindowAdapter(MapCustomInfoWindow(requireContext()))
            setOnInfoWindowClickListener {
                viewModel.drawRoute()
            }
        }
        if (routeVisible) drawRoute()
        requestLocationPermissions()
    }


    @SuppressLint("MissingPermission")
    private fun getDeviceLocation() {
        mMap?.isMyLocationEnabled = true
        mMap?.uiSettings?.isMyLocationButtonEnabled = true
        val locationResult = fusedLocationProviderClient.lastLocation
        locationResult.addOnCompleteListener(requireActivity()) { task ->
            if (task.isSuccessful) {
                if (task.result != null) {
                    Log.d(TAG, "Got device location")
                    viewModel.setLastKnownLocation(task.result)
                }
            } else {
                Log.e(TAG, "Exception: %s", task.exception)
            }
        }
    }

    private fun requestLocationPermissions() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            checkLocationPermission()
        } else {
            getDeviceLocation()
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    @SuppressLint("MissingPermission")
    private val requestLocationPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                getDeviceLocation()
            } else {

            }
        }

    private fun checkLocationPermission() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                Log.d(TAG, "Fine Location Granted")
            }
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)
            -> {
                val builder = AlertDialog.Builder(requireContext())
                builder.apply {
                    setTitle("Requesting your permission")
                    setMessage(
                        "Please provide location permission for route plotting function"
                    )
                    setPositiveButton("Yes, please") { _, _ ->
                        requestLocationPermissionLauncher.launch(
                            Manifest.permission.ACCESS_FINE_LOCATION
                        )
                    }
                    builder.setNegativeButton("No, thanks") { _, _ ->
                        Toast.makeText(
                            requireContext(),
                            "Permission request declined",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    show()
                }
            }
            else -> {
                requestLocationPermissionLauncher.launch(
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            }
        }
    }

    private fun getVehicleName(vehicle: VehicleEntity) =
        "${vehicle.make} - ${vehicle.model} - ${vehicle.year}"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
        attachObservers()
    }

    override fun onResume() {
        timer.start()
        super.onResume()
    }

    override fun onStop() {
        timer.cancel()
        super.onStop()
    }

    private fun drawRoute() {
        line?.remove()
        line = mMap?.addPolyline(viewModel.options)
        if (line != null) routeVisible = true
    }

    private fun attachObservers() {
        viewModel.routeLine.observe(viewLifecycleOwner) {
            drawRoute()
        }
        viewModel.error.observe(viewLifecycleOwner) { error ->
            if (error.isNotBlank()) {
                val view = layoutInflater.inflate(R.layout.layout_error, null)
                val dialog = BottomSheetDialog(requireActivity())
                    .apply {
                        setContentView(view)
                        setOnDismissListener {
                            viewModel.onErrorDismiss()
                        }
                    }

                view.findViewById<TextView>(R.id.tvError).text = error
                view.findViewById<Button>(R.id.btnOk).setOnClickListener {
                    dialog.dismiss()
                }

                dialog.show()
            }

        }
        viewModel.listOfVehicleLocations.observe(viewLifecycleOwner) {
            mMap?.let {
                drawMarkers(it, false)
            }
        }
    }

}