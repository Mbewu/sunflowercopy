package com.example.sunflower_copy.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.sunflower_copy.domain.GardenVertex
import com.example.sunflower_copy.util.GLOBAL_PLANT_ID
import com.example.sunflower_copy.domain.PlantInformation2


/**
 * DatabasePlant represents a database of available plants
 */
@Entity(tableName = "databaseplant")
data class DatabasePlant constructor(
    @PrimaryKey
    val plantId: String,
    val name: String,
    val description: String,
    val growZoneNumber: Int,
    val wateringInterval: Int,
    val imageUrl: String)


/**
 * Map DatabasePlants to domain entities
 * Each time a plant is created in this way a new id is given
 */
fun List<DatabasePlant>.asDomainModel(): List<PlantInformation2> {
    return map {
        PlantInformation2(
            id = GLOBAL_PLANT_ID++,
            latinName = it.plantId,
            name = it.name,
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
    @PrimaryKey
    val id: Int,
    val latinName: String,
    val name: String,
    val description: String,
    val growZoneNumber: Int,
    val wateringInterval: Int,
    val imageUrl: String,
    val plantedTime: Long,
    val wateringsDone: Int,
    val triggerTime: Long,
    val latitude: Double,
    val longitude: Double)

/**
 * Map DatabaseVideos to domain entities
 */
fun List<DatabaseGarden>.asGardenDomainModel(): List<PlantInformation2> {
    return map {
        PlantInformation2(
            id = it.id,
            latinName = it.latinName,
            name = it.name,
            description = it.description,
            growZoneNumber = it.growZoneNumber,
            wateringInterval = it.wateringInterval,
            imageUrl = it.imageUrl,
            plantedTime = it.plantedTime,
            wateringsDone = it.wateringsDone,
            triggerTime = it.triggerTime,
            latitude = it.latitude,
            longitude = it.longitude)
    }
}

/**
 * Map domain PlantInformation2 to DataBaseGarden entities
 * One at a time
 */
fun PlantInformation2.asGardenDatabaseEntity(): DatabaseGarden {
    return  DatabaseGarden(
            id = this.id,
            latinName= this.latinName,
            name = this.name,
            description = this.description,
            growZoneNumber = this.growZoneNumber,
            wateringInterval = this.wateringInterval,
            imageUrl = this.imageUrl,
            plantedTime = this.plantedTime,
            wateringsDone = this.wateringsDone,
            triggerTime = this.triggerTime,
            latitude = this.latitude,
            longitude = this.longitude)
    }





/**** GARDEN ****/

/**
 * DatabaseGardenLayout represents a single garden with the locations of the vertices of the garden
 * in order
 */
@Entity(tableName = "databasegardenlayout")
data class DatabaseGardenLayout constructor(
    @PrimaryKey
    val id: Int,
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
 * Map domain PlantInformation2 to DataBaseGarden entities
 * One at a time
 */
fun GardenVertex.asGardenDatabaseEntity(): DatabaseGardenLayout {
    return  DatabaseGardenLayout(
        id = this.id,
        latitude = this.latitude,
        longitude = this.longitude)
}

