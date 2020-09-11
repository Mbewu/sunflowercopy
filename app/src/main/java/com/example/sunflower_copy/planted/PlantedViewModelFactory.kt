package com.example.sunflower_copy.planted

import android.app.Application
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.sunflower_copy.domain.Plant
import com.example.sunflower_copy.repository.GardenRepository
import timber.log.Timber


/**
 * Simple ViewModel factory that provides the Plant and context to the ViewModel.
 */
class PlantedViewModelFactory(
    private val plantInformation: Plant,
    private val application: Application,
    private val gardenRepository: GardenRepository) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {

        Timber.i( "in detail0")
        if (modelClass.isAssignableFrom(PlantedViewModel::class.java)) {

            Timber.i( "in detail1")
            return PlantedViewModel(plantInformation, application,gardenRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")

    }
}
