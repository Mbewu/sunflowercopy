package com.example.sunflower_copy.repository

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.example.sunflower_copy.util.GLOBAL_PLANT_ID
import com.example.sunflower_copy.database.*
import com.example.sunflower_copy.domain.GardenVertex
import com.example.sunflower_copy.domain.Plant
import com.example.sunflower_copy.map.DEFAULT_LATLNG
import com.example.sunflower_copy.util.wrapEspressoIdlingResource
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.*
import timber.log.Timber
import java.lang.Math.random

/**
 * Repository for fetching devbyte videos from the network and storing them on disk
 */
class GardenRepository(application: Application,
                       private val database: GardenDatabase,
                       private val databaseLayout: GardenLayoutDatabase,
                       private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO) {

    val plantedPlants: LiveData<List<Plant>> = Transformations.map(database.gardenDao.getPlants) {
        it.asGardenDomainModel()
    }

    val gardenVertices: LiveData<List<GardenVertex>> =Transformations.map(databaseLayout.gardenLayoutDao.getGardenVertices) {
        it.asGardenLayoutDomainModel()
        }


    init {

        // set the global plant id to the largest in the garden so far
        // use the main thread so that it is updated properly, even though it is IO
        // not allowed to though... we are starting our own coroutine scope here
        // tbh, it should be possible to do without this but yeah...
        Timber.i("before launch")
        val tempScope = CoroutineScope(ioDispatcher)
        tempScope.launch {
            Timber.i("before setting global plant id to: ".plus(GLOBAL_PLANT_ID) )

            GLOBAL_PLANT_ID = database.gardenDao.getLargestId()
            Timber.i("after setting global plant id to: ".plus(GLOBAL_PLANT_ID) )

        }
    }



    suspend fun addPlantToGarden(Plant: Plant, location: LatLng? = null): Plant {

        wrapEspressoIdlingResource {
            // create new plant (and id)
            val newPlant = Plant(Plant)

            // set time plantedTime and new plant id
            val plantedTime = System.currentTimeMillis()
            newPlant.plantedTime = plantedTime

            // get the location to add
            var lat: Double = 0.0
            var lng: Double = 0.0
            // set the location
            if (location == null) {
                val gardenHeight = 51.73218 - 51.71891
                lat = DEFAULT_LATLNG.latitude + 0.5 * (2 * random() - 1) * gardenHeight

                val gardenWidth = -1.22872 - (-1.24128)
                lng = DEFAULT_LATLNG.longitude + 0.5 * (2 * random() - 1) * gardenWidth
            } else {
                lat = location.latitude
                lng = location.longitude
            }

            newPlant.latitude = lat
            newPlant.longitude = lng


            database.gardenDao.insert(newPlant.asGardenDatabaseEntity())

            return newPlant
        }
    }

    suspend fun updatePlantInGarden(Plant: Plant) {

        wrapEspressoIdlingResource {
            Timber.i("insde update plant")
            database.gardenDao.update(Plant.asGardenDatabaseEntity())
        }
    }

    suspend fun removePlantFromGarden(Plant: Plant) {

        wrapEspressoIdlingResource {
            database.gardenDao.delete(Plant.asGardenDatabaseEntity())
        }
    }

    suspend fun clearGarden() {

        wrapEspressoIdlingResource {
            database.gardenDao.clear()
        }
    }



    // clear the current garden layout and sets a new one
    suspend fun setGardenLayout(gardenVertices: List<GardenVertex>) {

        wrapEspressoIdlingResource {
            Timber.i("repo1")
            // clear the table
            Timber.i("repo2")
            databaseLayout.gardenLayoutDao.clear()
            Timber.i("repo3")
            // add all the vertices one by one
            for (gardenVertex in gardenVertices) {
                Timber.i("repo33333")
                databaseLayout.gardenLayoutDao.insert(gardenVertex.asGardenDatabaseEntity())
            }
            Timber.i("repo4")

            Timber.i("repo5 num verts = ".plus(databaseLayout.gardenLayoutDao.getGardenVertices.value?.size))

        }
    }


}