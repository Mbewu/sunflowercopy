package com.example.sunflower_copy.map

import android.app.Application
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.sunflower_copy.domain.Plant
import com.example.sunflower_copy.planted.PlantedViewModel
import com.example.sunflower_copy.repository.GardenRepository
import timber.log.Timber

/**
 * Simple ViewModel factory that provides the PlantInformation and context to the ViewModel.
 */
class MapViewModelFactory(private val application: Application,
                        private val gardenRepository: GardenRepository) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {

        Timber.i( "in detail0")
        if (modelClass.isAssignableFrom(MapViewModel::class.java)) {

            Timber.i( "in detail1")
            return MapViewModel(application, gardenRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")

    }
}