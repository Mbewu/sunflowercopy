package com.example.sunflower_copy.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.sunflower_copy.domain.GardenVertex
import com.example.sunflower_copy.util.GLOBAL_PLANT_ID
import com.example.sunflower_copy.domain.Plant


/**
 * DatabasePlant represents a database of available plants
 */
@Entity(tableName = "databaseplant")
data class DatabasePlant constructor(
    @PrimaryKey
    val plantId: String = "",
    val name: String = "",
    val description: String = "",
    val growZoneNumber: Int = 1,
    val wateringInterval: Int = 1,
    val imageUrl: String = "")


/**
 * Map DatabasePlants to domain entities
 * Each time a plant is created in this way a new id is given
 */
fun List<DatabasePlant>.asDomainModel(): List<Plant> {
    return map {
        Plant(
            id = GLOBAL_PLANT_ID++,
            name = it.name,
            latinName = it.plantId,
            description = it.description,
            growZoneNumber = it.growZoneNumber,
            wateringInterval = it.wateringInterval,
            imageUrl = it.imageUrl)
    }
}


/**** GARDEN ****/

/**
 * DatabaseVideo represents a video entity in the database.
 */
@Entity(tableName = "databasegarden")
data class DatabaseGarden constructor(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val name: String,
    val latinName: String,
    val description: String,
    val growZoneNumber: Int,
    val wateringInterval: Int,
    val imageUrl: String,
    val plantedTime: Long,
    val wateringsDone: Int,
    val triggerTime: Long,
    val latitude: Double,
    val longitude: Double,
    val wasGrowing: Boolean)

/**
 * Map DatabaseVideos to domain entities
 */
fun List<DatabaseGarden>.asGardenDomainModel(): List<Plant> {
    return map {
        it.asGardenDomainModel()
    }
}


/**
 * Map DatabaseVideos to domain entities
 */

fun DatabaseGarden.asGardenDomainModel(): Plant {
    return Plant(
            id = this.id,
            name = this.name,
            latinName = this.latinName,
            description = this.description,
            growZoneNumber = this.growZoneNumber,
            wateringInterval = this.wateringInterval,
            imageUrl = this.imageUrl,
            plantedTime = this.plantedTime,
            wateringsDone = this.wateringsDone,
            triggerTime = this.triggerTime,
            latitude = this.latitude,
            longitude = this.longitude,
            wasGrowing = this.wasGrowing)
}

/**
 * Map domain Plant to DataBaseGarden entities
 * One at a time
 */
fun Plant.asGardenDatabaseEntity(): DatabaseGarden {
    return  DatabaseGarden(
            id = this.id,
            name = this.name,
            latinName= this.latinName,
            description = this.description,
            growZoneNumber = this.growZoneNumber,
            wateringInterval = this.wateringInterval,
            imageUrl = this.imageUrl,
            plantedTime = this.plantedTime,
            wateringsDone = this.wateringsDone,
            triggerTime = this.triggerTime,
            latitude = this.latitude,
            longitude = this.longitude,
            wasGrowing = this.wasGrowing)
    }





/**** GARDEN ****/

/**
 * DatabaseGardenLayout represents a single garden with the locations of the vertices of the garden
 * in order
 */
@Entity(tableName = "databasegardenlayout")
data class DatabaseGardenLayout constructor(
    @PrimaryKey
    val id: Long,
    val latitude: Double,
    val longitude: Double)

/**
 * Map DatabaseVideos to domain entities
 */
fun List<DatabaseGardenLayout>.asGardenLayoutDomainModel(): List<GardenVertex> {
    return map {
        GardenVertex(
            id = it.id,
            latitude = it.latitude,
            longitude = it.longitude)
    }
}

/**
 * Map domain Plant to DataBaseGarden entities
 * One at a time
 */
fun GardenVertex.asGardenDatabaseEntity(): DatabaseGardenLayout {
    return  DatabaseGardenLayout(
        id = this.id,
        latitude = this.latitude,
        longitude = this.longitude)
}

