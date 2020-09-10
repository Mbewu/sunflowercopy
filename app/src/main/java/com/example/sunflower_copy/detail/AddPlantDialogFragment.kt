package com.example.sunflower_copy.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import com.example.sunflower_copy.R
import com.example.sunflower_copy.SharedViewModel
import com.example.sunflower_copy.databinding.FragmentDialogAddPlantBinding
import com.example.sunflower_copy.ui.main.PageViewModel
import timber.log.Timber


class AddPlantDialogFragment : DialogFragment()  {

    private lateinit var binding: FragmentDialogAddPlantBinding
    private lateinit var viewModel: PageViewModel

    private val sharedViewModel: SharedViewModel by activityViewModels()

    override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
): View? {

        viewModel = ViewModelProvider(requireActivity()).get(PageViewModel::class.java)
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_dialog_add_plant,
            container,false
        )

        viewModel.getPlantInformation()
        val plantName = viewModel.selectedPlant.value?.name
        Timber.i("plant name = ".plus(viewModel.selectedPlant.value?.name))
        binding.dialogMessage.text = getString(R.string.dialog_text_add_plant, plantName)
        binding.buttonPositive.text = getString(R.string.label_yes)
        binding.buttonNegative.text = getString(R.string.label_no)

        binding.buttonPositive.setOnClickListener {
            viewModel.navigateToMap(true)
            sharedViewModel.plantOnMap(viewModel.selectedPlant.value!!)
            dismiss()
        }

        binding.buttonNegative.setOnClickListener {
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