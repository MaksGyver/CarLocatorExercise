package com.example.android.car_locator.models.json

import com.google.gson.annotations.SerializedName

data class Owner (

	@SerializedName("name") val name : String,
	@SerializedName("surname") val surname : String,
	@SerializedName("foto") val foto : String
)