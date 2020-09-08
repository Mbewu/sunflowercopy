package com.example.sunflower_copy.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.example.sunflower_copy.R
import com.example.sunflower_copy.domain.PlantInformation2
import com.example.sunflower_copy.planted.PlantedViewModelFactory
import timber.log.Timber
import java.util.*


class ClearGardenDialogFragment() : DialogFragment() {

    private lateinit var viewModel: PageViewModel


    //    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//    }
//
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Timber.i("IN clear DIALOG create view")
        Timber.i("in detail6")

        viewModel = ViewModelProvider(requireActivity()).get(PageViewModel::class.java)

        val v = inflater.inflate(R.layout.fragment_dialog_clear_garden, container, false)

        val title = v.findViewById<TextView>(R.id.dialog_title_clear_garden)
        title.text = getString(R.string.warning)
        val message = v.findViewById<TextView>(R.id.dialog_message_clear_garden)
        message.text = getString(R.string.clear_garden_dialog_message)
        val yesButton = v.findViewById<Button>(R.id.btnPositiveRemove)
        yesButton.text = getString(R.string.label_yes)
        val noButton = v.findViewById<Button>(R.id.btnNegativeRemove)
        noButton.text = getString(R.string.label_no)

        yesButton.setOnClickListener() {
            Timber.i("in click")
            viewModel.clearGarden()
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