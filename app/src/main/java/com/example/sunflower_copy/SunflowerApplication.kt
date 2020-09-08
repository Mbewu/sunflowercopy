package com.example.sunflower_copy

import android.app.Application
import timber.log.Timber

class SunflowerApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
    }

    var GLOBAL_PLANT_ID = 0
}