package com.example.sunflower_copy.ui.main

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
import com.example.sunflower_copy.domain.Plant
import timber.log.Timber

class RemoveSelectedPlantsDialogFragment(selectedPlantListInput: List<Plant>) : DialogFragment() {

    //private lateinit var viewModel: PageViewModel

    private val viewModel by activityViewModels<PageViewModel> {
        PageViewModelFactory(requireActivity().application,
            (requireContext().applicationContext as SunflowerApplication).plantRepository,
            (requireContext().applicationContext as SunflowerApplication).gardenRepository)
    }
    private val selectedPlantList = selectedPlantListInput

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

        //viewModel = ViewModelProvider(requireActivity()).get(PageViewModel::class.java)

        val view = inflater.inflate(R.layout.fragment_dialog_clear_garden, container, false)

        val title = view.findViewById<TextView>(R.id.title_text)
        title.text = getString(R.string.warning)
        val numberOfPlants = selectedPlantList.size
        val message = view.findViewById<TextView>(R.id.message_text)
        Timber.i("numberOfPlants = $numberOfPlants")
        message.text = getString(R.string.dialog_text_remove_selected_plants, numberOfPlants)
        val yesButton = view.findViewById<Button>(R.id.button_positive)
        yesButton.text = getString(R.string.label_yes)
        val noButton = view.findViewById<Button>(R.id.button_negative)
        noButton.text = getString(R.string.label_no)


        yesButton.setOnClickListener() {
            Timber.i("in click")
            viewModel.removePlantsFromGarden(selectedPlantList)
            dismiss()
        }

        noButton.setOnClickListener() {
            Timber.i("in click")
            dismiss()
        }

        return view

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