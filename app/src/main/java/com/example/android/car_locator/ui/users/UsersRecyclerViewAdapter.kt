package com.example.android.car_locator.ui.users

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.android.car_locator.R
import com.example.android.car_locator.models.room_entities.OwnerEntity
import com.example.android.car_locator.models.room_entities.VehicleEntity
import com.example.android.car_locator.ui.SharedViewModel
import com.example.android.car_locator.ui.setColorOfTextView
import java.lang.Exception

class UsersRecyclerViewAdapter(
    private val ownerList: List<OwnerEntity>,
    private val vehiclesList: List<VehicleEntity>,
    private val context: Context,
    private val childFrag: View?,
    private val viewModel: SharedViewModel
) :
    RecyclerView.Adapter<UsersRecyclerViewAdapter.MyViewHolder>() {

    companion object {
        const val TAG = "UsersRecyclerViewAdapter"
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): UsersRecyclerViewAdapter.MyViewHolder {
        var view = LayoutInflater.from(parent.context)
            .inflate(R.layout.user_record_layout, parent, false)
        return UsersRecyclerViewAdapter.MyViewHolder(view)
    }

    private fun fillVehicleFields(
        holder: UsersRecyclerViewAdapter.MyViewHolder,
        currentOwnerVehicles: List<VehicleEntity>,
        position: Int
    ) {
        holder.apply {
            tvVehicleMakeValue.text = currentOwnerVehicles[position].make
            tvModelValue.text = currentOwnerVehicles[position].model
            tvYearValue.text = currentOwnerVehicles[position].year.toString()
            tvVinValue.text = currentOwnerVehicles[position].vin
            setColorOfTextView(tvColorValue, Color.parseColor(currentOwnerVehicles[position].color))
            val errorImageCar = context.getDrawable(R.drawable.ic_car)
            errorImageCar?.setTint(ContextCompat.getColor(context, R.color.light_grey))
            ivVehiclePhoto.load(currentOwnerVehicles[position].photo) {
                error(errorImageCar)
                fallback(errorImageCar)
            }
        }
    }

    override fun onBindViewHolder(holder: UsersRecyclerViewAdapter.MyViewHolder, position: Int) {
        val ownerId = ownerList[position].ownerId
        val currentOwnerVehicles = vehiclesList.filter { it.ownerId == ownerId }
        holder.apply {
            try {
                etVehicles.setText("${currentOwnerVehicles[0].make} - ${currentOwnerVehicles[0].year}")
                fillVehicleFields(holder, currentOwnerVehicles, 0)
                attachDropDownAdapter(currentOwnerVehicles, etVehicles)
                etVehicles.setOnItemClickListener { parent, view, position, id ->
                    fillVehicleFields(holder, currentOwnerVehicles, position)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error getting vehicle data: " + e.message.toString())
            }
            val errorImageOwner = context.getDrawable(R.drawable.ic_no_photo)
            errorImageOwner?.setTint(ContextCompat.getColor(context, R.color.light_grey))
            ivOwnerPhoto.load(ownerList[position].photoUrl) {
                error(errorImageOwner)
                fallback(errorImageOwner)
            }
            tvNameValue.text = ownerList[position].name
            tvSurnameValue.text = ownerList[position].surname
            tvNameLabel.text = "Name"
            tvSurnameLabel.text = "Surname"
            tvVehicleMakeLabel.text = "Maker"
            tvModelLabel.text = "Model"
            tvYearLabel.text = "Year"
            tvVinLabel.text = "Vin number"
            tvColorLabel.text = "Color"
            ivMap.setOnClickListener {
                try {
                    viewModel.cashLocationToLocalDb(ownerId)
                } catch (e: Exception) {
                    Log.e(TAG, "Error cashing vehicle location: ${e.message.toString()}")
                }
                viewModel.setCurrentOwnerId(ownerId)
                childFrag?.findNavController()
                    ?.navigate(R.id.action_navigation_users_to_maps)
            }
        }
    }

    private fun attachDropDownAdapter(list: List<VehicleEntity>, dropDown: AutoCompleteTextView) {
        val adapter = ArrayAdapter(
            context,
            R.layout.dropdown_vehicle_item,
            list.map { "${it.make} - ${it.year}" }
        )
        dropDown.setAdapter(adapter)
    }

    override fun getItemCount(): Int {
        return ownerList.size
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNameValue: TextView = itemView.findViewById(R.id.tv_name_value)
        val tvSurnameValue: TextView = itemView.findViewById(R.id.tv_surname_value)
        val tvNameLabel: TextView = itemView.findViewById(R.id.tv_name_label)
        val tvSurnameLabel: TextView = itemView.findViewById(R.id.tv_surname_label)
        val tvVehicleMakeValue: TextView = itemView.findViewById(R.id.tv_vehicle_make_value)
        val tvVehicleMakeLabel: TextView = itemView.findViewById(R.id.tv_vehicle_make_label)
        val tvModelValue: TextView = itemView.findViewById(R.id.tv_vehicle_model_value)
        val tvModelLabel: TextView = itemView.findViewById(R.id.tv_vehicle_model_label)
        val tvYearValue: TextView = itemView.findViewById(R.id.tv_vehicle_year_value)
        val tvYearLabel: TextView = itemView.findViewById(R.id.tv_vehicle_year_label)
        val tvVinValue: TextView = itemView.findViewById(R.id.tv_vin_value)
        val tvVinLabel: TextView = itemView.findViewById(R.id.tv_vin_label)
        val tvColorValue: TextView = itemView.findViewById(R.id.tv_color_value)
        val tvColorLabel: TextView = itemView.findViewById(R.id.tv_color_label)
        val etVehicles: AutoCompleteTextView = itemView.findViewById(R.id.etVehicles)
        val ivOwnerPhoto: ImageView = itemView.findViewById(R.id.iv_owner_photo)
        val ivVehiclePhoto: ImageView = itemView.findViewById(R.id.iv_vehicle_photo)
        val ivMap: ImageView = itemView.findViewById(R.id.iv_map)
        val noOwnerPhoto = R.drawable.ic_no_photo
    }
}