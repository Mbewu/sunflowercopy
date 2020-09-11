package com.example.sunflower_copy.domain

import android.os.Parcelable
import android.os.SystemClock
import androidx.room.ColumnInfo
import androidx.room.PrimaryKey
import com.example.sunflower_copy.util.GLOBAL_PLANT_ID
import com.squareup.moshi.Json
import kotlinx.android.parcel.Parcelize


/**
 * Domain objects are plain Kotlin data classes that represent the things in our app. These are the
 * objects that should be displayed on screen, or manipulated by the app.
 */

@Parcelize
data class Plant(
    @PrimaryKey @ColumnInfo(name = "id") var id: Int,
    val name: String,
    val latinName: String = "",
    val description: String = "",
    val growZoneNumber: Int = 0,
    val wateringInterval: Int = 0,
    val imageUrl: String = "",
    var plantedTime: Long = 0,
    var wateringsDone: Int = 0,
    var triggerTime: Long = 0,
    var latitude: Double = .0,
    var longitude: Double = .0) : Parcelable {

    override fun toString() = name

    fun getMaturationTime() = wateringInterval*growZoneNumber

    fun getWateringsRemaining() = growZoneNumber - wateringsDone

    fun getTimeRemaining() = triggerTime - SystemClock.elapsedRealtime()

    // should return true is plant is growing
    fun isGrowing() = (getTimeRemaining() > 0)

    // should return true plant is ready to water
    fun isReadyToWater() = (getTimeRemaining() <= 0)
            && (getWateringsRemaining() > 0)

    // should return true plant is ready to harvest
    fun isReadyToHarvest() = (getTimeRemaining() <= 0)
            && (getWateringsRemaining() == 0)

    // copy constructor with new global plant id
    // hmm, not sure it should copy everything like waterings and stuff, but okay
    constructor(copy: Plant) :
            this(++GLOBAL_PLANT_ID,
                copy.name,
                copy.latinName,
                copy.description,
                copy.growZoneNumber,
                copy.wateringInterval,
                copy.imageUrl,
                copy.plantedTime,
                copy.wateringsDone,
                copy.triggerTime,
                copy.latitude,
                copy.longitude)

    // blank constructor
    constructor() : this(0,
        "",
        "",
        "",
        0,
        0,
        "",
        0,
        0,
        0,
        .0,
    .0)


}

@Parcelize
data class GardenVertex(
    @PrimaryKey @ColumnInfo(name = "id") val id: Int,
    val latitude: Double = .0,
    val longitude: Double = .0) : Parcelable