package com.example.sunflower_copy.planted

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.example.sunflower_copy.R
import com.example.sunflower_copy.SunflowerApplication
import com.example.sunflower_copy.domain.PlantInformation2
import com.example.sunflower_copy.ui.main.PageViewModel
import com.example.sunflower_copy.ui.main.PageViewModelFactory
import timber.log.Timber
import java.util.*


class RemovePlantDialogFragment(selectedPlantInput: PlantInformation2) : DialogFragment() {

    //private lateinit var viewModel: PageViewModel

    private val viewModel by activityViewModels<PageViewModel> {
        PageViewModelFactory(requireActivity().application,
            (requireContext().applicationContext as SunflowerApplication).plantRepository,
            (requireContext().applicationContext as SunflowerApplication).gardenRepository)
    }

    private val selectedPlant = selectedPlantInput

    //    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//    }
//
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Timber.i("IN DIALOG create view")
        Timber.i("in detail6")

        val application = requireNotNull(activity).application
        Timber.i("in detail7")
        //val viewModelFactory = PlantedViewModelFactory(selectedPlant, application)
        Timber.i("in detail7")

        //viewModel = ViewModelProvider(viewModelStore, viewModelFactory).get(PlantedViewModel::class.java)
        //viewModel = ViewModelProvider(requireArguments()).get(PlantedViewModel::class.java)
        //viewModel = ViewModelProvider(requireActivity()).get(PlantedViewModel::class.java)

        //viewModel = ViewModelProvider(requireActivity()).get(PageViewModel::class.java)

        val v = inflater.inflate(R.layout.fragment_dialog_remove_plant, container, false)

        val message = v.findViewById<TextView>(R.id.message)
        val plantName = viewModel.selectedPlant.value?.name?.toLowerCase(Locale.ROOT)
        Timber.i("selectedPlant = ".plus(plantName))
        message.text = getString(R.string.dialog_text_remove_plant, plantName)
        val yesButton = v.findViewById<Button>(R.id.button_positive)
        yesButton.text = getString(R.string.label_yes)
        val noButton = v.findViewById<Button>(R.id.button_negative)
        noButton.text = getString(R.string.label_no)

        yesButton.setOnClickListener() {
            Timber.i("in click")
            viewModel.removePlantFromGarden()
            dismiss()
        }

        noButton.setOnClickListener() {
            Timber.i("in click")
            dismiss()
        }

        return v

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Timber.i("IN DIALOG view created")
        super.onViewCreated(view, savedInstanceState)
        //viewModel = ViewModelProvider(requireActivity()).get(SharedViewModel::class.java)

    }


    override fun onStart() {
        Timber.i("IN DIALOG on start")
        super.onStart()
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }
}