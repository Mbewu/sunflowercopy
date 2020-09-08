package com.example.sunflower_copy

import android.content.ClipData
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.sunflower_copy.domain.PlantInformation2
import com.example.sunflower_copy.util.Event

// this view model will be used by all fragments to communicate things
// like what function to call when you get there
class SharedViewModel : ViewModel() {

    // setup the garden polygon on the map
    // after navigation from title fragment
    private val _setupGardenOnMap = MutableLiveData<Event<Boolean>>()

    val setupGardenOnMap : LiveData<Event<Boolean>>
        get() = _setupGardenOnMap


    fun setupGardenOnMap(setupGarden: Boolean) {
        _setupGardenOnMap.value = Event(setupGarden)  // Trigger the event by setting a new Event as a new value
    }


    private val _plantOnMap = MutableLiveData<Event<PlantInformation2>>()

    val plantOnMap : LiveData<Event<PlantInformation2>>
        get() = _plantOnMap


    fun plantOnMap(plant: PlantInformation2) {
        _plantOnMap.value = Event(plant)  // Trigger the event by setting a new Event as a new value
    }


    // setup the garden polygon on the map
    // after navigation from title fragment
    // not safe but i think it's work
    var navigateToPlantOnMap = MutableLiveData<PlantInformation2>()






}
