package com.example.sunflower_copy.planted

import android.app.Application
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.sunflower_copy.domain.PlantInformation
import com.example.sunflower_copy.domain.PlantInformation2


/**
 * Simple ViewModel factory that provides the PlantInformation and context to the ViewModel.
 */
class PlantedViewModelFactory(
    private val plantInformation: PlantInformation2,
    private val application: Application) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {

        Log.i("PlantedViewModelFactory", "in detail0")
        if (modelClass.isAssignableFrom(PlantedViewModel::class.java)) {

            Log.i("PlantedViewModelFactory", "in detail1")
            return PlantedViewModel(plantInformation, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")

    }
}
