package com.example.sunflower_copy.repository

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.example.sunflower_copy.util.GLOBAL_PLANT_ID
import com.example.sunflower_copy.database.*
import com.example.sunflower_copy.domain.GardenVertex
import com.example.sunflower_copy.domain.PlantInformation2
import com.example.sunflower_copy.map.DEFAULT_LATLNG
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.lang.Math.random

/**
 * Repository for fetching devbyte videos from the network and storing them on disk
 */
class GardenRepository(application: Application,
                       private val database: GardenDatabase,
                       private val databaseLayout: GardenLayoutDatabase = getGardenLayoutDatabase(application)) {

    val plantedPlants: LiveData<List<PlantInformation2>> = Transformations.map(database.gardenDao.getPlants()) {
        it.asGardenDomainModel()
    }

    val gardenVertices: LiveData<List<GardenVertex>> =Transformations.map(databaseLayout.gardenLayoutDao.getGardenVertices()) {
        it.asGardenLayoutDomainModel()
        }


    init {

        // set the global plant id to the largest in the garden so far
        // use the main thread so that it is updated properly, even though it is IO
        // not allowed to though...
        val tempScope = CoroutineScope(Dispatchers.IO)
        Timber.i("before launch")
        tempScope.launch {
            Timber.i("before setting global plant id to: ".plus(GLOBAL_PLANT_ID) )

            GLOBAL_PLANT_ID = database.gardenDao.getLargestId()
            Timber.i("after setting global plant id to: ".plus(GLOBAL_PLANT_ID) )

        }
    }



    suspend fun addPlantToGarden(plantInformation2: PlantInformation2, location: LatLng? = null): PlantInformation2 {

        // create new plant (and id)
        val newPlant = PlantInformation2(plantInformation2)

        // set time plantedTime and new plant id
        val plantedTime = System.currentTimeMillis()
        newPlant.plantedTime = plantedTime

        // get the location to add
        var lat: Double = 0.0
        var lng: Double = 0.0
        // set the location
        if(location == null) {
            val gardenHeight = 51.73218 - 51.71891
            lat = DEFAULT_LATLNG.latitude + 0.5*(2*random()-1)*gardenHeight

            val gardenWidth = -1.22872 - (-1.24128)
            lng = DEFAULT_LATLNG.longitude + 0.5*(2*random()-1)*gardenWidth
        } else {
            lat = location.latitude
            lng = location.longitude
        }

        newPlant.latitude = lat
        newPlant.longitude = lng


        withContext(Dispatchers.IO) {

            database.gardenDao.insert(newPlant.asGardenDatabaseEntity())

        }

        return newPlant
    }

    suspend fun updatePlantInGarden(plantInformation2: PlantInformation2) {

        withContext(Dispatchers.IO) {
            Timber.i("insde update plant")
            database.gardenDao.update(plantInformation2.asGardenDatabaseEntity())
        }
    }

    suspend fun removePlantFromGarden(plantInformation2: PlantInformation2) {

        withContext(Dispatchers.IO) {

            database.gardenDao.delete(plantInformation2.asGardenDatabaseEntity())
        }
    }

    suspend fun clearGarden() {

        withContext(Dispatchers.IO) {
            database.clearAllTables()
        }
    }



    // clear the current garden layout and sets a new one
    suspend fun setGardenLayout(gardenVertices: List<GardenVertex>) {

        Timber.i("repo1")
        withContext(Dispatchers.IO) {
            // clear the table
            Timber.i("repo2")
            databaseLayout.clearAllTables()
            Timber.i("repo3")
            // add all the vertices one by one
            for(gardenVertex in gardenVertices) {
                Timber.i("repo33333")
                databaseLayout.gardenLayoutDao.insert(gardenVertex.asGardenDatabaseEntity())
            }
            Timber.i("repo4")

            Timber.i("repo5 num verts = ".plus(databaseLayout.gardenLayoutDao.getGardenVertices().value?.size))

        }
    }


}