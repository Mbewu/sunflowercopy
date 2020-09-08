package com.example.sunflower_copy.ui.main

import android.os.SystemClock
import com.example.sunflower_copy.util.convertLongToDateString
import com.example.sunflower_copy.domain.PlantInformation2
import java.text.SimpleDateFormat
import java.util.*

class PlantAndGardenPlantingsViewModel(plantInformation2: PlantInformation2) {
    private val plant = plantInformation2
//    private val gardenPlanting = plantings.gardenPlantings[0]
//
//    val waterDateString: String = dateFormat.format(gardenPlanting.lastWateringDate.time)
//    val wateringInterval
//        get() = plant.wateringInterval
    val imageUrl
        get() = plant.imageUrl
    val plantName
        get() = plant.name
    val latinName
        get() = plant.latinName
    //val plantedTimeString: String = dateFormat.format(plant.plantedTime)
    val plantedTimeString: String = convertLongToDateString(plant.plantedTime)

    val id
        get() = plant.id

    // return true if it has been planted
    val isPlanted
        get() = plant.plantedTime > 0

    val plantNameAndId
        get() = if(isPlanted) { plant.name.plus(" #").plus(plant.id) } else {plantName}


    val readyToWater
        get() = (plant.triggerTime < SystemClock.elapsedRealtime() && plant.getWateringsRemaining() > 0
                && plant.plantedTime > 0)

    val readyToHarvest
        get() = (plant.triggerTime < SystemClock.elapsedRealtime() && plant.getWateringsRemaining() == 0)

    val growing
        get() = (plant.triggerTime > SystemClock.elapsedRealtime())

    companion object {
        private val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.US)
    }
}