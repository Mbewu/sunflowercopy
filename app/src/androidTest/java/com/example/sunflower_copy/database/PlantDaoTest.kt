package com.example.sunflower_copy.database

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.sunflower_copy.domain.Plant
import com.example.sunflower_copy.util.getValue
import kotlinx.coroutines.runBlocking
import org.hamcrest.Matchers
import org.junit.*
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PlantDaoTest {
    private lateinit var database: PlantDatabase
    private lateinit var plantDao: PlantDao
    private val plantA = DatabasePlant("Appelus", "Apple")
    private val plantB = DatabasePlant("Pearus", "Pear")
    private val plantC = DatabasePlant("Beeticus", "Beet")

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun createDb() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, PlantDatabase::class.java).build()
        plantDao = database.plantDao

        // Insert plants in order A, B, C
        plantDao.insertAll(listOf(plantA, plantB, plantC))
    }

    @After
    fun closeDb() {
        database.close()
    }

    @Test
    fun testGetPlants() {
        val plantList = getValue(plantDao.getPlants())
        Assert.assertThat(plantList.size, Matchers.equalTo(3))

        // Ensure plant list is sorted by name
        Assert.assertThat(plantList[0], Matchers.equalTo(plantA))
        Assert.assertThat(plantList[1], Matchers.equalTo(plantB))
        Assert.assertThat(plantList[2], Matchers.equalTo(plantC))
    }
}