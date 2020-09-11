package com.example.sunflower_copy.title

import android.app.Application
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.sunflower_copy.detail.DetailViewModel
import com.example.sunflower_copy.domain.Plant

class LoginViewModelFactory(private val application: Application): ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T = LoginViewModel(application) as T
}

