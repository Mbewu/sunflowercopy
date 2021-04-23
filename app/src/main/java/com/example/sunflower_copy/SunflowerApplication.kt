package com.example.sunflower_copy

import android.app.Application
import com.example.sunflower_copy.repository.GardenRepository
import com.example.sunflower_copy.repository.PlantRepository
import timber.log.Timber

class SunflowerApplication : Application() {

    val gardenRepository: GardenRepository
        get() = ServiceLocator.provideGardenRepository(this)

    val plantRepository: PlantRepository
        get() = ServiceLocator.providePlantRepository(this)

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
    }

}