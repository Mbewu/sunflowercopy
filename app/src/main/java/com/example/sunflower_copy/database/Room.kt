package com.example.sunflower_copy.database

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.*


@Dao
interface PlantDao {
    @Query("select * from databaseplant")
    fun getPlants(): LiveData<List<DatabasePlant>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll( plants: List<DatabasePlant>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert( plants: List<DatabasePlant>)
}



@Database(entities = [DatabasePlant::class], version = 1)
abstract class PlantDatabase: RoomDatabase() {
    abstract val plantDao: PlantDao
}

private lateinit var INSTANCE_PLANTS: PlantDatabase

fun getDatabase(context: Context): PlantDatabase {

    synchronized(PlantDatabase::class.java) {
        if (!::INSTANCE_PLANTS.isInitialized) {
            INSTANCE_PLANTS = Room.databaseBuilder(context.applicationContext,
                PlantDatabase::class.java,
                "plants").build()
        }
    }
    return INSTANCE_PLANTS
}





@Dao
interface GardenDao {
    @Query("select * from databasegarden")
    fun getPlants(): LiveData<List<DatabaseGarden>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert( plants: DatabaseGarden)

    @Update
    fun update( plants: DatabaseGarden)

    // we return as an Int and not a LiveData<Int> so that we can access it in the code
    @Query("select id from databasegarden order by id desc limit 1")
    fun getLargestId(): Int

    @Delete
    fun delete( plants: DatabaseGarden)

}



@Database(entities = [DatabaseGarden::class], version = 1)
abstract class GardenDatabase: RoomDatabase() {
    abstract val gardenDao: GardenDao
}

private lateinit var INSTANCE_GARDEN: GardenDatabase

fun getGardenDatabase(context: Context): GardenDatabase {

    synchronized(GardenDatabase::class.java) {
        if (!::INSTANCE_GARDEN.isInitialized) {
            INSTANCE_GARDEN = Room.databaseBuilder(context.applicationContext,
                GardenDatabase::class.java,
                "garden").build()
        }
    }
    return INSTANCE_GARDEN
}






@Dao
interface GardenLayoutDao {
    @Query("select * from databasegardenlayout")
    fun getGardenVertices(): LiveData<List<DatabaseGardenLayout>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert( vertex: DatabaseGardenLayout)

    @Update
    fun update( vertex: DatabaseGardenLayout)

    @Delete
    fun delete( vertex: DatabaseGardenLayout)

    @Query("DELETE FROM databasegardenlayout")
    fun clear()

}



@Database(entities = [DatabaseGardenLayout::class], version = 1)
abstract class GardenLayoutDatabase: RoomDatabase() {
    abstract val gardenLayoutDao: GardenLayoutDao
}

private lateinit var INSTANCE_GARDEN_LAYOUT: GardenLayoutDatabase

fun getGardenLayoutDatabase(context: Context): GardenLayoutDatabase {

    synchronized(GardenLayoutDatabase::class.java) {
        if (!::INSTANCE_GARDEN_LAYOUT.isInitialized) {
            INSTANCE_GARDEN_LAYOUT = Room.databaseBuilder(context.applicationContext,
                GardenLayoutDatabase::class.java,
                "gardenLayout").build()
        }
    }
    return INSTANCE_GARDEN_LAYOUT
}

