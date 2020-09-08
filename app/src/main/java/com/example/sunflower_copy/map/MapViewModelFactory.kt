package com.example.sunflower_copy.map

import android.app.Application
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.sunflower_copy.domain.PlantInformation2
import com.example.sunflower_copy.planted.PlantedViewModel

/**
 * Simple ViewModel factory that provides the PlantInformation and context to the ViewModel.
 */
class MapViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {

        Log.i("MapViewModelFactory", "in detail0")
        if (modelClass.isAssignableFrom(MapViewModel::class.java)) {

            Log.i("PlantedViewModelFactory", "in detail1")
            return MapViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")

    }
}