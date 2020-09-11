package com.example.sunflower_copy.repository

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.example.sunflower_copy.database.*
import com.example.sunflower_copy.domain.Plant
import com.example.sunflower_copy.util.wrapEspressoIdlingResource
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * Repository for fetching devbyte videos from the network and storing them on disk
 */
class PlantRepository(private val database: PlantDatabase,
                      application: Application,
                      private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO) {

    private val appContext = application

    val plants: LiveData<List<Plant>> = Transformations.map(database.plantDao().getPlants) {
        it.asDomainModel()
    }


    /**
     * Refresh the videos stored in the offline cache.
     *
     * This function uses the IO dispatcher to ensure the database insert database operation
     * happens on the IO dispatcher. By switching to the IO dispatcher using `withContext` this
     * function is now safe to call from any thread including the Main thread.
     *
     */
    suspend fun refreshPlants() {

//        wrapEspressoIdlingResource {
//            appContext.assets.open(PLANT_DATA_FILENAME).use { inputStream ->
//                JsonReader(inputStream.reader()).use { jsonReader ->
//                    val plantType = object : TypeToken<List<DatabasePlant>>() {}.type
//                    val plantList: List<DatabasePlant> = Gson().fromJson(jsonReader, plantType)
//
//                    Timber.i("Number of plants found = ".plus(plantList.size.toString()))
//
//                    //val database = getDatabase(appContext)
//                    Timber.i("PageViewModel")
//                    database.plantDao().insertAll(plantList)
//
//                    //database.plantDao.getPlants()
//                    Timber.i("9")
//                }
//                Timber.i("10")
//
//            }
//        }
    }


    suspend fun addPlantToGarden() {

        wrapEspressoIdlingResource {
            appContext.assets.open(PLANT_DATA_FILENAME).use { inputStream ->
                JsonReader(inputStream.reader()).use { jsonReader ->
                    val plantType = object : TypeToken<List<DatabasePlant>>() {}.type
                    val plantList: List<DatabasePlant> = Gson().fromJson(jsonReader, plantType)

                    Timber.i("Number of plants found = ".plus(plantList.size.toString()))

                    //val database = getDatabase(appContext)
                    Timber.i("PageViewModel")
                    database.plantDao().insertAll(plantList)

                    //database.plantDao.getPlants()
                    Timber.i("9")
                }
                Timber.i("10")

            }
        }
    }

    suspend fun clearPlants() {

        wrapEspressoIdlingResource {
            database.plantDao().clear()
        }
    }



}