package com.example.sunflower_copy.planted

import android.app.AlarmManager
import android.app.Application
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.os.SystemClock
import android.text.Spanned
import android.text.format.DateUtils
import android.text.util.Linkify
import androidx.core.app.AlarmManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.lifecycle.*
import androidx.work.ListenableWorker
import com.example.sunflower_copy.R
import com.example.sunflower_copy.util.convertLongToDateString
import com.example.sunflower_copy.domain.Plant
import com.example.sunflower_copy.receiver.AlarmReceiver
import com.example.sunflower_copy.repository.GardenRepository
import com.example.sunflower_copy.util.sendNotification
import com.example.sunflower_copy.ui.main.PlantNotificationType
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.coroutines.withContext
import timber.log.Timber


/**
 *  The [ViewModel] associated with the [PlantedFragment], containing information about the selected
 *  [Plant].
 */
// makes app a variable or references
class PlantedViewModel(
    private val app: Application,
    private val gardenRepository: GardenRepository
) : AndroidViewModel(app) {


    private var _selectedPlantId = MutableLiveData<Long>()

    private val _selectedPlant = _selectedPlantId.switchMap { selectedPlantId ->
        gardenRepository.observePlant(selectedPlantId)}
    val selectedPlant: LiveData<Plant?> = _selectedPlant


    private val _dataLoading = MutableLiveData<Boolean>()
    val dataLoading: LiveData<Boolean> = _dataLoading

    val wateringsRemainingString = _selectedPlant.map { it.getWateringsRemaining()}
    val maturationTimeString = MutableLiveData<String>()



    fun start(selectedPlantId: Long) {
        Timber.i("starting plantedViewModel1")
        if (_dataLoading.value == true) {
            return
        }

        // trigger loading of livedata observer
        _selectedPlantId.value = selectedPlantId
    }

}

