package com.example.sunflower_copy.util

import android.content.Context
import android.os.SystemClock
import com.example.sunflower_copy.domain.Plant
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.BDDMockito
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.powermock.api.mockito.PowerMockito
import org.powermock.core.classloader.annotations.PrepareForTest
import org.powermock.modules.junit4.PowerMockRunner
import java.sql.DriverManager

@RunWith(PowerMockRunner::class)
@PrepareForTest(SystemClock::class)
class UtilTest {

    @Mock
    private lateinit var context: Context

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
    }

    @Test
    fun numPlantsGrowing_noGrowing_returnsZero() {

        PowerMockito.mockStatic(SystemClock::class.java)
        BDDMockito.given(SystemClock.elapsedRealtime()).willReturn(1L);


        // setup some plants
        val plant1 = Plant(0,"apple")
        val plant2 = Plant(1,"apple")
        val plant3 = Plant(2,"apple")

        // the plants will all have a triggerTime of zero and so shouldn't be growing

        val plants = listOf<Plant>(plant1,plant2,plant3)

        val numGrowing = numPlantsGrowing(plants)

        assertEquals(numGrowing,0)


//        PowerMockito.verifyStatic();
//        val time = SystemClock.elapsedRealtime();
//
//        assertEquals(time,1L)
    }



    @Test
    fun numPlantsGrowing_threeGrowing_returnsThree() {

        PowerMockito.mockStatic(SystemClock::class.java)
        BDDMockito.given(SystemClock.elapsedRealtime()).willReturn(0L);


        // setup some plants
        val plant1 = Plant(0,"apple")
        val plant2 = Plant(1,"apple")
        val plant3 = Plant(2,"apple")
        plant1.triggerTime = 1
        plant2.triggerTime = 1
        plant3.triggerTime = 1
        // the plants will all have a triggerTime of zero and so shouldn't be growing

        val plants = listOf<Plant>(plant1,plant2,plant3)

        val numGrowing = numPlantsGrowing(plants)

        assertEquals(numGrowing,3)

    }



    @Test
    fun numPlantsGrowing_twoGrowing_returnsTwo() {

        PowerMockito.mockStatic(SystemClock::class.java)
        BDDMockito.given(SystemClock.elapsedRealtime()).willReturn(0L);


        // setup some plants
        val plant1 = Plant(0,"apple")
        val plant2 = Plant(1,"apple")
        val plant3 = Plant(2,"apple")
        plant1.triggerTime = 0
        plant2.triggerTime = 1
        plant3.triggerTime = 1
        // the plants will all have a triggerTime of zero and so shouldn't be growing

        val plants = listOf<Plant>(plant1,plant2,plant3)

        val numGrowing = numPlantsGrowing(plants)

        assertEquals(numGrowing,2)

    }
}