package com.example.sunflower_copy.map

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.example.sunflower_copy.R
import com.example.sunflower_copy.SunflowerApplication
import com.example.sunflower_copy.databinding.FragmentDialogAddPlantBinding
import com.example.sunflower_copy.databinding.FragmentDialogClearGardenBinding
import com.example.sunflower_copy.domain.PlantInformation2
import com.example.sunflower_copy.planted.PlantedViewModelFactory
import com.example.sunflower_copy.ui.main.PageViewModel
import timber.log.Timber
import java.util.*


class ClearGardenDialogMapFragment() : DialogFragment() {

    private lateinit var viewModel: MapViewModel

//    private val viewModel by viewModels<MapViewModel> {
//        MapViewModelFactory(requireActivity().application,
//            (requireContext().applicationContext as SunflowerApplication).gardenRepository)
//    }

    private lateinit var binding: FragmentDialogClearGardenBinding


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProvider(requireActivity()).get(MapViewModel::class.java)

        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_dialog_clear_garden, container, false)

        binding.titleText.text = getString(R.string.warning)
        binding.messageText.text = getString(R.string.clear_garden_dialog_message)
        binding.buttonPositive.text = getString(R.string.label_yes)
        binding.buttonNegative.text = getString(R.string.label_no)

        binding.buttonPositive.setOnClickListener() {
            viewModel.clearGarden()
            dismiss()
        }

        binding.buttonNegative.setOnClickListener{
            dismiss()
        }

        return binding.root

    }


    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }
}