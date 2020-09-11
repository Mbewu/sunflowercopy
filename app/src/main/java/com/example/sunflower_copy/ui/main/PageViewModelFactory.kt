package com.example.sunflower_copy.ui.main

import android.app.Application
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.sunflower_copy.detail.DetailViewModel
import com.example.sunflower_copy.repository.GardenRepository
import com.example.sunflower_copy.repository.PlantRepository
import timber.log.Timber

/**
 * Simple ViewModel factory that provides the Plant and context to the ViewModel.
 */
class PageViewModelFactory(
    private val application: Application,
    private val plantRepository: PlantRepository,
    private val gardenRepository: GardenRepository
) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {

        Timber.i("in detail0")
        if (modelClass.isAssignableFrom(PageViewModel::class.java)) {

            Timber.i("in detail1")
            return PageViewModel(application,plantRepository,gardenRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")

    }
}
