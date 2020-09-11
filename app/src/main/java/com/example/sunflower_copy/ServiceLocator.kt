package com.example.sunflower_copy

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.room.Room
import com.example.sunflower_copy.database.*
import com.example.sunflower_copy.repository.GardenRepository
import com.example.sunflower_copy.repository.PlantRepository

object ServiceLocator {

    private var gardenDatabase: GardenDatabase? = null
    @Volatile
    var gardenRepository: GardenRepository? = null
        @VisibleForTesting set


    private var plantDatabase: PlantDatabase? = null
    @Volatile
    var plantRepository: PlantRepository? = null
        @VisibleForTesting set

    private val lock = Any()

    @VisibleForTesting
    fun resetGardenRepository() {
        synchronized(lock) {
//            runBlocking {
//                TasksRemoteDataSource.deleteAllTasks()
//            }
            // Clear all data to avoid test pollution.
            gardenDatabase?.apply {
                clearAllTables()
                close()
            }
            gardenDatabase = null
            gardenRepository = null
        }
    }

    @VisibleForTesting
    fun resetPlantRepository() {
        synchronized(lock) {
//            runBlocking {
//                TasksRemoteDataSource.deleteAllTasks()
//            }
            // Clear all data to avoid test pollution.
            plantDatabase?.apply {
                clearAllTables()
                close()
            }
            plantDatabase = null
            plantRepository = null
        }
    }

    fun provideGardenRepository(context: Context): GardenRepository {
        synchronized(this) {
            return gardenRepository ?: createGardenRepository(context)
        }
    }

    fun providePlantRepository(context: Context): PlantRepository {
        synchronized(this) {
            return plantRepository ?: createPlantRepository(context)
        }
    }

    private fun createGardenRepository(context: Context): GardenRepository {
        val application: SunflowerApplication = context as SunflowerApplication
        val newRepo = GardenRepository(
            application,
            getGardenDatabase(application),
            getGardenLayoutDatabase(application))

        //val newRepo = DefaultTasksRepository(TasksRemoteDataSource, createTaskLocalDataSource(context))
        gardenRepository = newRepo
        return newRepo
    }

    private fun createPlantRepository(context: Context): PlantRepository {
        val application: SunflowerApplication = context as SunflowerApplication
        val newRepo = PlantRepository(
            getDatabase(application),
            application)

        //val newRepo = DefaultTasksRepository(TasksRemoteDataSource, createTaskLocalDataSource(context))
        plantRepository = newRepo
        return newRepo
    }


    private lateinit var INSTANCE_GARDEN: GardenDatabase

    private fun getGardenDatabase(context: Context): GardenDatabase {

        synchronized(GardenDatabase::class.java) {
            if (!::INSTANCE_GARDEN.isInitialized) {
                INSTANCE_GARDEN = Room.databaseBuilder(context.applicationContext,
                    GardenDatabase::class.java,
                    "garden").build()
            }
        }
        return INSTANCE_GARDEN
    }


    private lateinit var INSTANCE_PLANTS: PlantDatabase

    private fun getDatabase(context: Context): PlantDatabase {

        synchronized(PlantDatabase::class.java) {
            if (!::INSTANCE_PLANTS.isInitialized) {
                INSTANCE_PLANTS = Room.databaseBuilder(context.applicationContext,
                    PlantDatabase::class.java,
                    "plants").build()
            }
        }
        return INSTANCE_PLANTS
    }


    private lateinit var INSTANCE_GARDEN_LAYOUT: GardenLayoutDatabase

    private fun getGardenLayoutDatabase(context: Context): GardenLayoutDatabase {

        synchronized(GardenLayoutDatabase::class.java) {
            if (!::INSTANCE_GARDEN_LAYOUT.isInitialized) {
                INSTANCE_GARDEN_LAYOUT = Room.databaseBuilder(context.applicationContext,
                    GardenLayoutDatabase::class.java,
                    "gardenLayout").build()
            }
        }
        return INSTANCE_GARDEN_LAYOUT
    }


//    private fun createTaskLocalDataSource(context: Context): TasksDataSource {
//        val database = database ?: createDataBase(context)
//        return TasksLocalDataSource(database.taskDao())
//    }

//    private fun createGardenDataBase(context: Context): GardenDatabase {
//        val result = Room.databaseBuilder(
//            context.applicationContext,
//            GardenDatabase::class.java, "garden"
//        ).build()
//        gardenDatabase = result
//        return result
//    }
//
//
//    private fun createPlantDataBase(context: Context): PlantDatabase {
//        val result = Room.databaseBuilder(
//            context.applicationContext,
//            PlantDatabase::class.java, "plants"
//        ).build()
//        plantDatabase = result
//        return result
//    }
}