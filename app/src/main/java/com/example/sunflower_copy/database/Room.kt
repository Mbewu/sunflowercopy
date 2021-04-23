package com.example.sunflower_copy.database

import android.content.Context
import android.icu.text.CaseMap
import androidx.lifecycle.LiveData
import androidx.room.*


@Dao
interface PlantDao {
    @get:Query("SELECT * FROM databaseplant")
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
//    @Query("SELECT * FROM databasegarden")
//    suspend fun getPlants(): LiveData<List<DatabaseGarden>>

    @get:Query("SELECT * FROM databasegarden")
    val getPlants: List<DatabaseGarden>

//    @Query("SELECT * FROM databasegarden WHERE id = :plantId")
//    fun getPlant(plantId: Int): LiveData<List<DatabaseGarden>>

    @Query("SELECT * FROM databasegarden WHERE id = :plantId")
    suspend fun getPlant(plantId: Long): DatabaseGarden

    @get:Query("SELECT * FROM databasegarden")
    val observePlants: LiveData<List<DatabaseGarden>>

    @Query("SELECT * FROM databasegarden WHERE id = :plantId")
    fun observePlant(plantId: Long): LiveData<DatabaseGarden>



    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert( plant: DatabaseGarden): Long

    @Update
    suspend fun update( plant: DatabaseGarden)

    // we return as an Int and not a LiveData<Int> so that we can access it in the code
    @Query("SELECT id FROM databasegarden ORDER BY id DESC LIMIT 1")
    suspend fun getLargestId(): Long

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
    @get:Query("SELECT * FROM databasegardenlayout")
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

