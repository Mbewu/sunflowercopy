package com.example.sunflower_copy.domain

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class PlantInformation2Test {

    private lateinit var plant: PlantInformation2

    @Before
    fun setUp() {
        plant = PlantInformation2(0,"appelus","apple","it is an apple",0,0,"url")
    }

    @Test
    fun test_default_values() {
        assertEquals(0L,plant.plantedTime)
        assertEquals(0,plant.wateringsDone)
        assertEquals(0L,plant.triggerTime)
        assertEquals(.0,plant.latitude,1e-5)
        assertEquals(.0,plant.longitude,1e-5)
    }
}