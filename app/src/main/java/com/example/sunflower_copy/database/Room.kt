package com.example.sunflower_copy.database

import android.content.Context
import android.icu.text.CaseMap
import androidx.lifecycle.LiveData
import androidx.room.*


@Dao
interface PlantDao {
    @get:Query("select * from databaseplant")
    val getPlants: LiveData<List<DatabasePlant>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll( plants: List<DatabasePlant>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert( plants: List<DatabasePlant>)

    @Query("DELETE FROM databaseplant")
    suspend fun clear()
}



//@Database(entities = [DatabasePlant::class], version = 1)
//abstract class PlantDatabase: RoomDatabase() {
//    abstract val plantDao: PlantDao
//}



@Dao
interface GardenDao {
//    @Query("select * from databasegarden")
//    suspend fun getPlants(): LiveData<List<DatabaseGarden>>

    @get:Query("select * from databasegarden")
    val getPlants: LiveData<List<DatabaseGarden>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert( plants: DatabaseGarden)

    @Update
    suspend fun update( plants: DatabaseGarden)

    // we return as an Int and not a LiveData<Int> so that we can access it in the code
    @Query("select id from databasegarden order by id desc limit 1")
    suspend fun getLargestId(): Int

    @Delete
    suspend fun delete( plants: DatabaseGarden)

    @Query("DELETE FROM databasegarden")
    suspend fun clear()

}



@Database(entities = [DatabaseGarden::class], version = 1)
abstract class GardenDatabase: RoomDatabase() {
    abstract val gardenDao: GardenDao
}








@Dao
interface GardenLayoutDao {
    @get:Query("select * from databasegardenlayout")
    val getGardenVertices: LiveData<List<DatabaseGardenLayout>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert( vertex: DatabaseGardenLayout)

    @Update
    suspend fun update( vertex: DatabaseGardenLayout)

    @Delete
    suspend fun delete( vertex: DatabaseGardenLayout)

    @Query("DELETE FROM databasegardenlayout")
    suspend fun clear()

}



@Database(entities = [DatabaseGardenLayout::class], version = 1)
abstract class GardenLayoutDatabase: RoomDatabase() {
    abstract val gardenLayoutDao: GardenLayoutDao
}

