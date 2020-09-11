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
import android.text.format.DateUtils
import androidx.core.app.AlarmManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.*
import com.example.sunflower_copy.R
import com.example.sunflower_copy.util.convertLongToDateString
import com.example.sunflower_copy.domain.PlantInformation
import com.example.sunflower_copy.domain.PlantInformation2
import com.example.sunflower_copy.receiver.AlarmReceiver
import com.example.sunflower_copy.repository.GardenRepository
import com.example.sunflower_copy.util.sendNotification
import com.example.sunflower_copy.ui.main.PlantNotificationType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber


/**
 *  The [ViewModel] associated with the [PlantedFragment], containing information about the selected
 *  [PlantInformation].
 */
// makes app a variable or references
class PlantedViewModel(
    plantInformation: PlantInformation2,
    private val app: Application,
    private val gardenRepository: GardenRepository
) : AndroidViewModel(app) {



    // repository of garden plants
    //private val gardenRepository = GardenRepository(app,getGardenDatabase(app))

    // The internal MutableLiveData for the selected property
    private var _selectedPlant = MutableLiveData<PlantInformation2>()
    val selectedPlant: LiveData<PlantInformation2>
        get() = _selectedPlant

    // LiveData to handle navigation back to the home screen telling user the plant id that was removed
    private val _plantRemoved = MutableLiveData<Int>()
    val plantRemoved: LiveData<Int>
        get() = _plantRemoved

    // Initialize the _selectedProperty MutableLiveData
    init {
        _selectedPlant.value = plantInformation
    }

    // string for output
    val plantedTimeString = Transformations.map(selectedPlant) { plant ->
        "Time planted = ".plus(convertLongToDateString(plant.plantedTime))
    }




    // function to remove plant from garden
    fun removePlantFromGarden() {

        Timber.i("removing plant before")
        viewModelScope.launch {
            Timber.i("hello2")
            val id = _selectedPlant.value?.id
            gardenRepository.removePlantFromGarden(_selectedPlant.value!!)
            _plantRemoved.value = id
            Timber.i("hello3")
        }
        Timber.i("hello4")
    }

    // function to remove plant from garden
    fun removePlantFromGarden(plantToRemove: PlantInformation2) {

        Timber.i("removing plant before")
        viewModelScope.launch {
            Timber.i("hello2")
            val id = plantToRemove.id
            gardenRepository.removePlantFromGarden(plantToRemove)
            _plantRemoved.value = id
            Timber.i("hello3")
        }
        Timber.i("hello4")
    }

    /**
     * After the navigation has taken place, make sure navigateToSelectedProperty is set to null
     */
    fun plantRemovalComplete() {
        _plantRemoved.value = null
    }

    private val REQUEST_CODE = 0
    private val TRIGGER_TIME = "TRIGGER_AT"

    private val minute: Long = 60_000L
    private val second: Long = 1_000L

    private val notifyPendingIntent: PendingIntent

    private val alarmManager = app.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private var prefs =
        app.getSharedPreferences("com.example.sunflower_copy", Context.MODE_PRIVATE)
    private val notifyIntent = Intent(app, AlarmReceiver::class.java)


//    private val _elapsedTime = MutableLiveData<Long>()
//    val elapsedTime: LiveData<Long>
//        get() = _elapsedTime

//    val wateringsRemaining: LiveData<Int>
//        get() = _wateringsRemaining

    // LiveData for outputting waterings remaining
    private val _wateringsRemaining = MutableLiveData<Int>()
    val wateringsRemaining: LiveData<Int>
        get() = _wateringsRemaining

    // time remaining on clock
    private val _timeRemaining = MutableLiveData<Long>()
    val timeRemaining: LiveData<Long>
        get() = _timeRemaining

    // transformation for output of time remaining
    val timeRemainingString = Transformations.map(timeRemaining) { time ->
        DateUtils.formatElapsedTime(time)}

    // LiveData observation parameter to indicate when user is trying to water too soon
    private val _plantOverWatering = MutableLiveData<Boolean>()
    val plantOverWatering: LiveData<Boolean>
        get() = _plantOverWatering

    // LiveData observation parameter to indicate the plant is ready to be  watered
    private val _readyToWater = MutableLiveData<Boolean>()
    val readyToWater: LiveData<Boolean>
        get() = _readyToWater

    // LiveData observation parameter to indicate the plant is ready to be harvested
    private val _readyToHarvest = MutableLiveData<Boolean>()
    val readyToHarvest: LiveData<Boolean>
        get() = _readyToHarvest

    private var _alarmOn = MutableLiveData<Boolean>()
    val isAlarmOn: LiveData<Boolean>
        get() = _alarmOn

    private val requestCode: Int


    private lateinit var timer: CountDownTimer

    init {
        //_elapsedTime.value = 0
        _plantOverWatering.value = false
        _wateringsRemaining.value = _selectedPlant.value?.getWateringsRemaining()

        // check if there is a current timer going on for this plant
        // we use the plantId for the REQUEST_CODE
        requestCode = _selectedPlant.value?.id!!

        // extra data we want to send in the notification
        val extras = Bundle()
        extras.putString("name", _selectedPlant.value?.name)
        _selectedPlant.value?.id?.let { extras.putInt("id", it) }
        _selectedPlant.value?.getWateringsRemaining()?.let {
            extras.putInt("wateringsRemaining",
                it
            )
        }

        _alarmOn.value = requestCode?.let {
            PendingIntent.getBroadcast(
                getApplication(),
                it,
                notifyIntent.putExtras(extras),
                PendingIntent.FLAG_NO_CREATE
            )
        } != null

        notifyPendingIntent = requestCode?.let {
            PendingIntent.getBroadcast(
                getApplication(),
                it,
                notifyIntent.putExtras(extras),
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        }!!



        //If alarm is not null, resume the timer back for this alarm
        if (_alarmOn.value!!) {
            Timber.i("there was an alarm for this plant")
            createTimer()
            _readyToWater.value = false
            _readyToHarvest.value = false
        }
        else // we will maybe start a new timer or not
        {
            Timber.i("there was not an alarm for this plant")
            // initialise some variables
            _timeRemaining.value = 0
            if(_wateringsRemaining.value == 0) {
                _readyToHarvest.value = true
                _readyToWater.value = false
            } else {
                _readyToHarvest.value = false
                _readyToWater.value = true
            }
        }
    }

    // function called from click to water plant and start timer
    fun waterPlant() {
        Timber.i("trying to water plant, timeRemaining = ".plus(_timeRemaining.value))
        if(_timeRemaining.value!! > 0) {
            Timber.i("not watering")
            _plantOverWatering.value = true
        }
        else {
            Timber.i("waterings done before = ".plus(_selectedPlant.value?.wateringsDone))
            Timber.i("waterings done before = ".plus(_selectedPlant.value?.getWateringsRemaining()))
            Timber.i("waterings remaining before = ".plus(_wateringsRemaining.value))
            _selectedPlant.value?.wateringsDone = _selectedPlant.value?.wateringsDone?.plus(1)!!
            // update in database
            updateSelectedPlant()
            _wateringsRemaining.value = _selectedPlant.value?.getWateringsRemaining()
            Timber.i("waterings done after = ".plus(_selectedPlant.value?.wateringsDone))
            Timber.i("waterings done after = ".plus(_selectedPlant.value?.getWateringsRemaining()))
            Timber.i("waterings remaining after = ".plus(_wateringsRemaining.value))

            // start a new timer
            startTimer()
            _readyToWater.value = false
        }
    }

    // update the plant in repository in case of shut down or something
    private fun updateSelectedPlant() {

        Timber.i("updating plant")
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                gardenRepository.updatePlantInGarden(_selectedPlant.value!!)
                Timber.i("done updating plant")
            }
        }
        Timber.i("after updating plant")
    }

    /**
     * Creates a new timer
     */
    private fun startTimer() {

        Timber.i("creating timer")
        if (_selectedPlant.value?.wateringInterval != null) {
            Timber.i("creating timer1")

            Timber.i("creating timer2")
            // set trigger time in system time
            val triggerTime =  SystemClock.elapsedRealtime() + _selectedPlant.value!!.wateringInterval.times(
                second
            )
            Timber.i("creating timer2 triggerTime = ".plus(triggerTime))

            // send a notification to say plant is growing
            val notificationManager =
                ContextCompat.getSystemService(
                    app,
                    NotificationManager::class.java
                ) as NotificationManager
//            notificationManager.sendNotification(app.getString(
//                androidx.lifecycle.R.string.timer_running,_selectedPlant.value.name,_selectedPlant.value.id),app)

            notificationManager.sendNotification(
                app.getString(
                    R.string.timer_running, _selectedPlant.value!!.name, _selectedPlant.value!!.id
                ), app, requestCode, PlantNotificationType.GROWING
            )


            // set an alarm for the timer
            AlarmManagerCompat.setExactAndAllowWhileIdle(
                alarmManager,
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                triggerTime,
                notifyPendingIntent
            )


            // save the time at which the alarm should end
            // save it to the plant as well
            viewModelScope.launch {
                saveTime(triggerTime)
            }
        }

        // start the actual timer
        createTimer()
    }


    /**
     * Creates a new timer from a new trigger time or a previous value
     */
    private fun createTimer() {

        Timber.i("creating timer")
        viewModelScope.launch {
            Timber.i("creating timer2")
            // set trigger time from what was set
            val triggerTime =  loadTime()
            Timber.i("creating timer2 triggerTime = ".plus(triggerTime))
            timer = object : CountDownTimer(triggerTime, second) {
                override fun onTick(millisUntilFinished: Long) {
                    Timber.i("tictoc")
                    _timeRemaining.value =
                        (triggerTime - SystemClock.elapsedRealtime()) / second
                    //_timeRemaining.value = millisUntilFinished/second
                    //Timber.i("_elapsedTime.value = ".plus(_elapsedTime.value))
                    Timber.i("_timeRemaining.value = ".plus(_timeRemaining.value))

                    // if we have passed the trigger time then reset the clock
                    if (_timeRemaining.value!! <= 0) {
                        resetTimer()
                    }
                }

                override fun onFinish() {
                    resetTimer()
                }
            }
            Timber.i("creating timer1")
            timer.start()
            Timber.i("creating timer1 timer started")
        }
    }

    /**
     * Resets the timer on screen and sets various observation parameters
     */
    private fun resetTimer() {
        timer.cancel()
        _timeRemaining.value = 0
        if(wateringsRemaining.value == 0) {
            _readyToHarvest.value = true
        } else {
            _readyToWater.value = true
        }
        //_alarmOn.value = false
    }

    // save the time at which the alarm should go off
    private suspend fun saveTime(triggerTime: Long) =
        withContext(Dispatchers.IO) {
            prefs.edit().putLong(TRIGGER_TIME, triggerTime).apply();
            _selectedPlant.value?.triggerTime = triggerTime;
            updateSelectedPlant()
        }

    private suspend fun loadTime(): Long =
//        withContext(Dispatchers.IO) {
//            prefs.getLong(TRIGGER_TIME, 0)
//        }
        // returns 0 if it's null
        withContext(Dispatchers.IO) {
            _selectedPlant.value?.triggerTime ?: 0
        }

    /**
     * After the user has been told not to overwater, reset to false, so can be triggered again
     */
    fun stoppedOverWatering() {
        _plantOverWatering.value = false
    }




    // LiveData to handle harvesting
    private val _plantHarvested = MutableLiveData<Int>()
    val plantHarvested: LiveData<Int>
        get() = _plantHarvested


    /**
     * Harvest plant. For now, remove it
     */
    fun harvestPlant() {

        // a little redundant but nevermind
        viewModelScope.launch {
            val id = _selectedPlant.value?.id
            // add plant to pantry
            // TODO
            gardenRepository.removePlantFromGarden(_selectedPlant.value!!)
            _plantHarvested.value = id
        }

        // reset so that ready to harvest thing goes away, probably unecessary
        //_readyToHarvest.value = false
    }

    /**
     * After the navigation has taken place, make sure navigateToSelectedProperty is set to null
     */
    fun harvestingComplete() {
        _readyToHarvest.value = false
    }

//    val displayPropertyPrice = Transformations.map(selectedPlant) {
//        app.applicationContext.getString(
//            when (it.isRental) {
//                true -> R.string.display_price_monthly_rental
//                false -> R.string.display_price
//            }, it.price)
//    }
//    // The displayPropertyPrice formatted Transformation Map LiveData, which displays the sale
//    // or rental price.
//    val displayPropertyPrice = Transformations.map(selectedPlant) {
//        app.applicationContext.getString(
//            when (it.isRental) {
//                true -> R.string.display_price_monthly_rental
//                false -> R.string.display_price
//            }, it.price)
//    }
//
//    // The displayPropertyType formatted Transformation Map LiveData, which displays the
//    // "For Rent/Sale" String
//    val displayPropertyType = Transformations.map(selectedPlant) {
//        app.applicationContext.getString(
//            R.string.display_type,
//            app.applicationContext.getString(
//                when(it.isRental) {
//                    true -> R.string.type_rent
//                    false -> R.string.type_sale
//                }))
//    }
}

