package com.example.sunflower_copy.ui.main

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
import android.util.Log
import androidx.core.app.AlarmManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.*
import com.example.sunflower_copy.R
import com.example.sunflower_copy.domain.Plant
import com.example.sunflower_copy.receiver.AlarmReceiver
import com.example.sunflower_copy.repository.GardenRepository
import com.example.sunflower_copy.repository.PlantRepository
import com.example.sunflower_copy.util.Event
import com.example.sunflower_copy.util.ParcelableUtil.marshall
import com.example.sunflower_copy.util.cancelAllNotifications
import com.example.sunflower_copy.util.cancelNotifications
import com.example.sunflower_copy.util.sendNotification
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.IOException


enum class PlantApiStatus { LOADING, ERROR, DONE }

enum class PlantNotificationType { READY_TO_WATER, READY_TO_HARVEST, GROWING }


class PageViewModel(application: Application,
                    private val plantRepository: PlantRepository,
                    private val gardenRepository: GardenRepository) : AndroidViewModel(application) {

    private val app = getApplication<Application>()


    // repositories and plants and garden plants
//    private val plantRepository = PlantRepository(getDatabase(application), application)
//    private val gardenRepository = GardenRepository(application, getGardenDatabase(application))

    private val _navigateToMap = MutableLiveData<Event<Boolean>>()
    val navigateToMap : LiveData<Event<Boolean>>
        get() = _navigateToMap
    fun navigateToMap(navigate: Boolean) {
        _navigateToMap.value = Event(navigate)  // Trigger the event by setting a new Event as a new value
    }

    // Internally, we use a MutableLiveData, because we will be updating the List of MarsProperty
    // with new values
    private val _plants2 = plantRepository.plants
    // The external LiveData interface to the property is immutable, so only this class can modify
    val plants2: LiveData<List<Plant>>
        get() = _plants2

    // Internally, we use a MutableLiveData, because we will be updating the List of MarsProperty
    // with new values
    private val _plantedPlants: LiveData<List<Plant>> = gardenRepository.plantedPlants
    val plantedPlants: LiveData<List<Plant>>
        get() = _plantedPlants


    private var _selectedPlant = MutableLiveData<Plant>()
    // The external LiveData for the SelectedProperty
    val selectedPlant: LiveData<Plant?>
        get() = _selectedPlant

    // this is to set the selectedPlant when we are in a detail view
    fun setSelectedPlant(plantInformation: Plant) {
        _selectedPlant.value = plantInformation
    }



    private val _index = MutableLiveData<Int>()
    val text: LiveData<String> = Transformations.map(_index) {
        "Hello world from section: $it"
    }
    // index to say whether mygarden or plantlist
    fun setIndex(index: Int) {
        _index.value = index
    }


    val numPlantsInGarden
        get() = plantedPlants.value?.size
    val numAvailablePlants
        get() = plants2.value?.size

    //val numPlantedPlants: LiveData<Int> = Transformations.map(gardenRepository.plantedPlants.value.size) { it}

    val plantNeedsWatering: LiveData<Int> = Transformations.map(_plantedPlants) {
        var numPlantsNeedWatering: Int = 0;
        for(plant in it) {
            if (plant.triggerTime - SystemClock.elapsedRealtime() < 0) {
                numPlantsNeedWatering++
            }
        }
        return@map numPlantsNeedWatering

    }

    // LiveData to handle navigation to the selected property
    private val _navigateToSelectedPlant= MutableLiveData<Plant>()
    val navigateToSelectedPlant: LiveData<Plant>
        get() = _navigateToSelectedPlant

    // LiveData to handle navigation to the selected property
    private val _navigateToSelectedGardenPlant= MutableLiveData<Plant>()
    val navigateToSelectedGardenPlant: LiveData<Plant>
        get() = _navigateToSelectedGardenPlant



    // LiveData to handle which and whether plant has been added
    private var _plantAdded = MutableLiveData<Plant>()
    val plantAdded: LiveData<Plant>
        get() = _plantAdded

    // LiveData to handle which and whether plant has been added
    private var _plantsAdded = MutableLiveData<Int>()
    val plantsAdded: LiveData<Int>
        get() = _plantsAdded


    // LiveData to handle which and whether plant has been removed
    private val _plantRemoved = MutableLiveData<Int>()
    val plantRemoved: LiveData<Int>
        get() = _plantRemoved

    // LiveData to handle which and whether plant has been removed
    private val _plantsRemoved = MutableLiveData<Int>()
    val plantsRemoved: LiveData<Int>
        get() = _plantsRemoved



    // variables for updating text views etc
    val selectedPlantName: LiveData<String> = Transformations.map(_selectedPlant) { plant -> plant?.name}



    /**
     * Call getMarsRealEstateProperties() on init so we can display status immediately.
     */
    init {
        Timber.i( "0")

        Timber.i("yeah so")
        // initialise the plant list
        getPlantInformation()

        // set the global id for adding new plants on
        val plantListSize = _plants2.value?.size
        Timber.i("yeah so plantListSize = ".plus(plantListSize))



    }

    /**
     * Gets filtered Mars real estate property information from the Mars API Retrofit service and
     * updates the [MarsProperty] [List] and [MarsApiStatus] [LiveData]. The Retrofit service
     * returns a coroutine Deferred, which we await to get the result of the transaction.
     * @param filter the [MarsApiFilter] that is sent as part of the web server request
     */
    fun getPlantInformation() {

        Timber.i("1")
        viewModelScope.launch {
            try {
                plantRepository.refreshPlants()
                //_plants2 = plantRepository.plants
            } catch (networkError: IOException) {

            }

            // okay now we want to try and read the json file
            Timber.i("6")
        }
        Timber.i( "6")
    }


    /**
     * When the property is clicked, set the [_navigateToSelectedProperty] [MutableLiveData]
     * @param marsProperty The [MarsProperty] that was clicked on.
     */
    fun displayPlantDetails(plantInformation: Plant) {
        _navigateToSelectedPlant.value = plantInformation
        Timber.i(" plantedPlants.size = ".plus(Transformations.map(_plantedPlants) { it.size }.value.toString()))
        //Transformations.map(plantedPlants) { it.size }
    }

    /**
     * When the property is clicked, set the [_navigateToSelectedProperty] [MutableLiveData]
     * @param marsProperty The [MarsProperty] that was clicked on.
     */
    fun displayGardenPlantDetails(plantInformation: Plant) {
        _navigateToSelectedGardenPlant.value = plantInformation
        Timber.i(" plantedPlants.size = ".plus(Transformations.map(_plantedPlants) { it.size }.value.toString()))
        //Transformations.map(plantedPlants) { it.size }
    }

    /**
     * After the navigation has taken place, make sure navigateToSelectedProperty is set to null
     */
    fun displayPlantDetailsComplete() {
        Timber.i( "navigate before is null = "
                .plus((_navigateToSelectedPlant.value == null).toString())
        )
        _navigateToSelectedPlant.value = null
        Timber.i("navigate after is null = "
                .plus((_navigateToSelectedPlant.value == null).toString())
        )
    }

    /**
     * After the navigation has taken place, make sure navigateToSelectedProperty is set to null
     */
    fun displayGardenPlantDetailsComplete() {
        Timber.i( "navigate before is null = "
                .plus((_navigateToSelectedPlant.value == null).toString())
        )
        _navigateToSelectedGardenPlant.value = null
        Timber.i("navigate after is null = "
                .plus((_navigateToSelectedPlant.value == null).toString())
        )
    }



    // DETAIL VIEW STUFF

    // should be selected plant I guess
    fun addPlantToGarden(plantToAdd: Plant) {
        Timber.i("before planting")
        viewModelScope.launch {
            Timber.i("before planting plantedTime = ".plus(_selectedPlant.value?.plantedTime))
            Timber.i("after planting selectedPlant plantedTime = ".plus(_selectedPlant.value?.plantedTime))

            val newPlant = gardenRepository.addPlantToGarden(plantToAdd)
            _plantAdded.value = plantToAdd
        }
        Timber.i("after planting")
    }


    // should be selected plant I guess
    fun addPlantsToGarden(plantsToAdd: List<Plant>) {
        Timber.i("before planting")
        viewModelScope.launch {
            Timber.i("before planting plantedTime = ".plus(_selectedPlant.value?.plantedTime))
            // set time plantedTime and new plant id
            for(plant in plantsToAdd) {
                Timber.i("hello2")
                val newPlant = gardenRepository.addPlantToGarden(plant)
                Timber.i("hello3")
            }

            _plantsAdded.value = plantsToAdd.size
        }
        Timber.i("after planting")
    }


    /**
     * After the navigation has taken place, make sure navigateToSelectedProperty is set to null
     */
    fun plantAddedComplete() {
        _plantAdded.value = null
        _plantsAdded.value = null
    }




    // PLANTED STUFF

    private val minute: Long = 60_000L
    private val second: Long = 1_000L

    private lateinit var notifyPendingIntent: PendingIntent

    private val alarmManager = application.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private var prefs =
        application.getSharedPreferences("com.example.sunflower_copy", Context.MODE_PRIVATE)
    private val notifyIntent = Intent(application, AlarmReceiver::class.java)


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

    val plantDescription = Transformations.map(selectedPlant) { plant ->
        plant?.description
    }

    val plantLocationString: LiveData<String> = Transformations.map(selectedPlant) { plant ->
        app.getString(R.string.location_colon, plant?.latitude, plant?.longitude)
    }
    private var _alarmOn = MutableLiveData<Boolean>()
    val isAlarmOn: LiveData<Boolean>
        get() = _alarmOn

    // dunno but initialise to something
    private var requestCode: Int = -1


    // nullable so that doesn't have to be initialised
    private var timer: CountDownTimer? = null


    private val notificationManager =
        ContextCompat.getSystemService(
            app,
            NotificationManager::class.java
        ) as NotificationManager

    // need to initialise some things for viewing the selected plant and its details
    // some of these things might not work, if selectedPlant hasn't been received yet..
    // we can put it in an observer
    fun initPlantedView() {

        Timber.i("1 initPlantedView")
        // we need to reset the timer that may have been going from another plant
        resetTimer()
        val triggerTime =  loadTime()
        Timber.i("1 troggerTime = ".plus(triggerTime))
        _timeRemaining.value =
            (triggerTime - SystemClock.elapsedRealtime()) / second
        Timber.i("1 _timeRemaining.value = ".plus(_timeRemaining.value))
        _plantOverWatering.value = false
        _wateringsRemaining.value = _selectedPlant.value?.getWateringsRemaining()

        Timber.i("2")
        // check if there is a current timer going on for this plant
        // we use the plantId for the REQUEST_CODE
        requestCode = _selectedPlant.value?.id ?: -1

        Timber.i("3, requestCode = ".plus(requestCode))
        // extra data we want to send in the notification
        val extras = Bundle()
        extras.putString("name", _selectedPlant.value?.name)

        _selectedPlant.value?.id?.let { extras.putInt("id", it) }
        _selectedPlant.value?.getWateringsRemaining()?.let {
            extras.putInt("wateringsRemaining",
                it
            )
        }

        Timber.i("4")
        _alarmOn.value = requestCode.let {
            PendingIntent.getBroadcast(
                getApplication(),
                it,
                notifyIntent.putExtras(extras),
                PendingIntent.FLAG_NO_CREATE
            )
        } != null



        Timber.i("6")

        //If alarm is not null, resume the timer back for this alarm
        if (_alarmOn.value!! && _timeRemaining.value!! > 0) {
            Timber.i("there was an alarm for this plant")
            createTimer()
            _readyToWater.value = false
            _readyToHarvest.value = false
            Timber.i("7")
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
            Timber.i("8")
        }
        Timber.i("9")

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
            gardenRepository.updatePlantInGarden(_selectedPlant.value!!)
            Timber.i("done updating plant")
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
//            val notificationManager =
//                ContextCompat.getSystemService(
//                    app,
//                    NotificationManager::class.java
//                ) as NotificationManager
//            notificationManager.sendNotification(app.getString(
//                androidx.lifecycle.R.string.timer_running,_selectedPlant.value.name,_selectedPlant.value.id),app)

            // send notification to say the plant is growing
            notificationManager.sendNotification(
                app.getString(
                    R.string.timer_running,
                    _selectedPlant.value!!.name,
                    _selectedPlant.value!!.id
                ),
                app, requestCode, PlantNotificationType.GROWING
            )



            // save the time at which the alarm should end
            // save it to the plant as well
            viewModelScope.launch {
                saveTime(triggerTime)
            }

            // start the actual timer
            createTimer()
        }

    }


    /**
     * Creates a new timer from a new trigger time or a previous value
     */
    private fun createTimer() {


        // create the pending intent for the plant
        // extra data we want to send in the notification
        val extras = Bundle()
        extras.putString("name", _selectedPlant.value?.name)
        _selectedPlant.value?.id?.let { extras.putInt("id", it) }
        _selectedPlant.value?.getWateringsRemaining()?.let {
            extras.putInt("wateringsRemaining",
                it
            )
        }
        Timber.i("5")
        notifyPendingIntent = requestCode.let {
            PendingIntent.getBroadcast(
                getApplication(),
                it,
                notifyIntent.putExtras(extras),
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        }!!



        Timber.i("creating timer")
        viewModelScope.launch {

            // set trigger time from what was set
            val triggerTime =  loadTime()
            // the timer length needs to be based on the triggerTime in case it is a restart
            val timerLength = triggerTime - SystemClock.elapsedRealtime()
            Timber.i("creating timer2 triggerTime = ".plus(triggerTime))
            Timber.i("creating timer2 timerLength = ".plus(timerLength))

            // set an alarm to go off at trigger time
            // this is separate from the countdown timer, which is basically just for output
            AlarmManagerCompat.setExactAndAllowWhileIdle(
                alarmManager,
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                triggerTime,
                notifyPendingIntent
            )

            Timber.i("creating timer2")

            // start a countdown timer of the length of time
            timer = object : CountDownTimer(timerLength, second) {
                override fun onTick(millisUntilFinished: Long) {
                    Timber.i("tictoc")
                    //
                    //_timeRemaining.value =
                    //    (triggerTime - SystemClock.elapsedRealtime()) / second
                    _timeRemaining.value = millisUntilFinished/second
                    //Timber.i("_elapsedTime.value = ".plus(_elapsedTime.value))
                    Timber.i("_timeRemaining.value = ".plus(_timeRemaining.value))
                }

                // if we finish before a new plant has been viewed
                override fun onFinish() {
                    resetTimer()
                }
            }
//            timer = object : CountDownTimer(triggerTime, second) {
//                override fun onTick(millisUntilFinished: Long) {
//                    Timber.i("tictoc")
//                    _timeRemaining.value =
//                        (triggerTime - SystemClock.elapsedRealtime()) / second
//                    //_timeRemaining.value = millisUntilFinished/second
//                    //Timber.i("_elapsedTime.value = ".plus(_elapsedTime.value))
//                    Timber.i("_timeRemaining.value = ".plus(_timeRemaining.value))
//
//                    // if we have passed the trigger time then reset the clock
//                    if (_timeRemaining.value!! <= 0) {
//
//                        Timber.i("reseting timer")
//                        resetTimer()
//                    }
//                }
//
//                override fun onFinish() {
//                    resetTimer()
//                }
//            }
            Timber.i("creating timer1")
            (timer as CountDownTimer).start()
            Timber.i("creating timer1 timer started")
        }
    }

    /**
     * Resets the timer on screen and sets various observation parameters
     */
    private fun resetTimer() {
        Timber.i("reset timer")
        timer?.cancel()
        _timeRemaining.value = 0
        if(wateringsRemaining.value == 0) {
            _readyToHarvest.value = true
        } else {
            _readyToWater.value = true
        }
        //_alarmOn.value = false
    }

    // save the time at which the alarm should go off
    private fun saveTime(triggerTime: Long) {
        //prefs.edit().putLong(TRIGGER_TIME, triggerTime).apply();
        _selectedPlant.value?.triggerTime = triggerTime;
        updateSelectedPlant()
    }

    private fun loadTime(): Long {
//        withContext(Dispatchers.IO) {
//            prefs.getLong(TRIGGER_TIME, 0)
//        }
        // returns 0 if it's null

        Timber.i("triggerTime = ".plus(_selectedPlant.value?.triggerTime))
        return _selectedPlant.value?.triggerTime ?: 0


//        withContext(Dispatchers.IO) {
//            _selectedPlant.value?.triggerTime ?: 0
//        }
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

        val id = _selectedPlant.value?.id
        // a little redundant but nevermind
        viewModelScope.launch {
            // add plant to pantry
            // TODO
            gardenRepository.removePlantFromGarden(_selectedPlant.value!!)

        }

        _plantHarvested.value = id
        _readyToHarvest.value = false
        // remove notifications for this plant
        notificationManager.cancelNotifications(requestCode)


    }

    /**
     * After the navigation has taken place, make sure navigateToSelectedProperty is set to null
     */
    fun plantHarvestingComplete() {
        _plantHarvested.value = null
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
        // remove notifications for this plant
        notificationManager.cancelNotifications(requestCode)

    }

    // function to remove plant from garden
    fun removePlantFromGarden(plantToRemove: Plant) {

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


    // function to remove plant from garden
    fun removePlantsFromGarden(plantsToRemove: List<Plant>) {

        Timber.i("removing plants before")
        viewModelScope.launch {
            for(plant in plantsToRemove) {
                Timber.i("hello2")
                val id = plant.id
                gardenRepository.removePlantFromGarden(plant)
                Timber.i("hello3")
            }
        }
        _plantsRemoved.value = plantsToRemove.size
        Timber.i("hello4")
    }


    /**
     * After the navigation has taken place, make sure navigateToSelectedProperty is set to null
     */
    fun plantRemovalComplete() {
        _plantRemoved.value = null
        _plantsRemoved.value = null
    }


    // LiveData to handle harvesting
    private val _gardenCleared = MutableLiveData<Boolean>()
    val gardenCleared: LiveData<Boolean>
        get() = _gardenCleared

    // function to clear the whole
    fun clearGarden(): Boolean {

        Timber.i("clearing garden before")
        // reset the timer if necessary
        resetTimer()
        // remove all the alarms
        removeAlarms()
        // clear the garden
        viewModelScope.launch {
            Timber.i("hello2")
            gardenRepository.clearGarden()
            Timber.i("hello3")
        }
        Timber.i("hello4")
        // remove all notifications for the garden
        notificationManager.cancelAllNotifications()

        _gardenCleared.value = true
        return true
    }

    /**
     * After the navigation has taken place, make sure navigateToSelectedProperty is set to null
     */
    fun gardenClearingComplete() {
        _gardenCleared.value = false
    }
    private fun removeAlarms() {

        // we need to create pending intents that match the plants in the list and cancel them separately
        for(plant in plantedPlants.value!!)
        {
            val requestCode = plant.id

            // recreate the correct pending intent
            // might need to have the extras in there as well
            notifyPendingIntent =
                PendingIntent.getBroadcast(
                    getApplication(),
                    requestCode,
                    notifyIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )

            // cancel the alarm
            alarmManager.cancel(notifyPendingIntent)

        }
    }


    // just a basic looking through the plants tbh
    fun getPlantFromId(plantId: Int): Plant? {

        Timber.d("oh god, _plantedPlants.value.size = ".plus(_plantedPlants.value?.size))

        // loop over plantedPlants
        for(plant in _plantedPlants.value!!) {
            if(plant.id == plantId) {
                return plant
            }
        }

        // if we get here return null
        return null
    }

}