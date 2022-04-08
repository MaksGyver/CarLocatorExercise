package com.example.android.car_locator.models.json

import com.google.gson.annotations.SerializedName


data class Vehicles (

	@SerializedName("vehicleid") val vehicleid : Long,
	@SerializedName("make") val make : String,
	@SerializedName("model") val model : String,
	@SerializedName("year") val year : Int,
	@SerializedName("color") val color : String,
	@SerializedName("vin") val vin : String,
	@SerializedName("foto") val foto : String
)