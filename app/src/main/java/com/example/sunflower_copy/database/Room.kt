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

