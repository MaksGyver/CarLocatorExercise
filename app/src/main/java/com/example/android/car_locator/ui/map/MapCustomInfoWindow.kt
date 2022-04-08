package com.example.android.car_locator.ui.map

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.location.Geocoder
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.android.car_locator.R
import com.example.android.car_locator.models.room_entities.VehicleEntity
import com.example.android.car_locator.ui.setColorOfTextView
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import java.lang.Exception

class MapCustomInfoWindow(
    private val context: Context
) : GoogleMap.InfoWindowAdapter {

    companion object {
        const val TAG = "MapCustomInfoWindow"
    }

    var mWindow =
        (context as Activity).layoutInflater.inflate(R.layout.map_custom_info_window, null)

    private fun rendowWindowText(marker: Marker, view: View) {

        val tvName = view.findViewById<TextView>(R.id.tv_vehicle_name)
        val tvAddress = view.findViewById<TextView>(R.id.tv_vehicle_address)
        val tvColor = view.findViewById<TextView>(R.id.tv_vehicle_color)
        val ivVehiclePhoto = view.findViewById<ImageView>(R.id.iv_vehicle_photo)

        tvName.text = marker.title
        tvAddress.text = getAddress(marker.position)

        val vehicle: VehicleEntity = marker.tag as VehicleEntity
        try {
            setColorOfTextView(tvColor, Color.parseColor(vehicle.color))
            tvColor.text = ""
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing color")
        }
        val errorImageCar = context.getDrawable(R.drawable.ic_car)
        errorImageCar?.setTint(ContextCompat.getColor(context, R.color.light_grey))
        Picasso.with(context)
            .load(vehicle.photo)
            .placeholder(errorImageCar)
            .error(errorImageCar)
            .into(ivVehiclePhoto, object : Callback {

                override fun onSuccess() {
                    if (marker.isInfoWindowShown) {
                        marker.hideInfoWindow()
                        marker.showInfoWindow()
                    }
                }

                override fun onError() {
                    Log.e(TAG, "onError: An error occured")
                }
            })
    }

    private fun getAddress(coordinates: LatLng): String? {
        val geocoder = Geocoder(context)
        val list = geocoder.getFromLocation(coordinates.latitude, coordinates.longitude, 1)
        return list[0].getAddressLine(0)
    }

    override fun getInfoContents(marker: Marker): View {
        rendowWindowText(marker, mWindow)
        return mWindow
    }

    override fun getInfoWindow(marker: Marker): View? {
        rendowWindowText(marker, mWindow)
        return mWindow
    }
}
