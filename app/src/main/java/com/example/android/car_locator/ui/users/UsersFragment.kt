package com.example.android.car_locator.ui.users

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.android.car_locator.R
import com.example.android.car_locator.databinding.FragmentUsersBinding
import com.example.android.car_locator.models.room_entities.OwnerEntity
import com.example.android.car_locator.models.room_entities.VehicleEntity
import com.example.android.car_locator.ui.SharedViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class UsersFragment: Fragment() {

    companion object {
        const val TAG = "UsersFragment"
    }

    private val viewModel by sharedViewModel<SharedViewModel>()

    private var _binding: FragmentUsersBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUsersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.dailyCashOfUserData()
        attachObservers()
    }

    private fun implementRecyclerView(owners: List<OwnerEntity>, vehicles: List<VehicleEntity>) {
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
            adapter = UsersRecyclerViewAdapter(owners, vehicles, requireContext(), view, viewModel)
        }
    }

    private fun attachObservers(){
        viewModel.listOfOwners.observe(viewLifecycleOwner) { listOfOwners ->
           implementRecyclerView(listOfOwners, viewModel.listOfVehicles.value ?: listOf())
        }
        viewModel.listOfVehicles.observe(viewLifecycleOwner) { listOfVehicles ->
            implementRecyclerView(viewModel.listOfOwners.value ?: listOf(), listOfVehicles)
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
    }

}