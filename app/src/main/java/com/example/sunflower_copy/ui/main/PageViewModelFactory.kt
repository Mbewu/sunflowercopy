package com.example.sunflower_copy.ui.main

import android.app.Application
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.sunflower_copy.detail.DetailViewModel
import com.example.sunflower_copy.domain.PlantInformation
import timber.log.Timber

/**
 * Simple ViewModel factory that provides the PlantInformation and context to the ViewModel.
 */
class PageViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {

        Timber.i("in detail0")
        if (modelClass.isAssignableFrom(PageViewModel::class.java)) {

            Timber.i("in detail1")
            return PageViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")

    }
}
