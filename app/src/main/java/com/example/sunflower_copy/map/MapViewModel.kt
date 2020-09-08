package com.example.sunflower_copy.map

import android.app.AlarmManager
import android.app.Application
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.sunflower_copy.R
import com.example.sunflower_copy.database.getGardenDatabase
import com.example.sunflower_copy.database.getGardenLayoutDatabase
import com.example.sunflower_copy.domain.GardenVertex
import com.example.sunflower_copy.domain.PlantInformation2
import com.example.sunflower_copy.receiver.AlarmReceiver
import com.example.sunflower_copy.repository.GardenRepository
import com.example.sunflower_copy.util.Event
import com.example.sunflower_copy.util.cancelAllNotifications
import com.example.sunflower_copy.util.getPlantColor
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.*


class MapViewModel(application: Application) : AndroidViewModel(application)  {

    private val app = getApplication<Application>()

    // get default initially, observer will handle changing to login user
    private var sharedPreferences =
        app.getSharedPreferences(
        app.getString(R.string.preference_file_key_base)
            .plus("_default"), Context.MODE_PRIVATE
    )

    // repositories garden plants
    private val gardenRepository = GardenRepository(
        application, getGardenDatabase(application),
        getGardenLayoutDatabase(application)
    )

    // Internally, we use a MutableLiveData, because we will be updating the List of MarsProperty
    // with new values
    private val _plantedPlants: LiveData<List<PlantInformation2>> = gardenRepository.plantedPlants
    val plantedPlants: LiveData<List<PlantInformation2>>
        get() = _plantedPlants

    // Internally, we use a MutableLiveData, because we will be updating the List of MarsProperty
    // with new values
    private val _gardenVertices: LiveData<List<GardenVertex>> = gardenRepository.gardenVertices
    val gardenVertices: LiveData<List<GardenVertex>>
        get() = _gardenVertices


    private val alarmManager = application.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    private val notificationManager =
        ContextCompat.getSystemService(
            app,
            NotificationManager::class.java
        ) as NotificationManager

    // LiveData to handle harvesting
    private val _gardenCleared = MutableLiveData<Boolean>()
    val gardenCleared: LiveData<Boolean>
        get() = _gardenCleared


    // Internally, we use a MutableLiveData, because we will be updating the List of MarsProperty
    // with new values
    private val _plantAdded = MutableLiveData<Event<PlantInformation2>>()
    val plantAdded: LiveData<Event<PlantInformation2>>
        get() = _plantAdded


    private var _gardenPolygon: Polygon? = null
    val gardenPolygon: Polygon?
        get() = _gardenPolygon

    private var tempGardenPolygon: Polygon? = null

    val polygonMarkerList: MutableList<Marker> = mutableListOf()

    private var activeMarker: Marker? = null
    private var activeMarkerOptions: MarkerOptions? = null
    private var markerList: MutableList<Marker> = mutableListOf()

    fun setSharedPreferences(newSharedPreferences: SharedPreferences) {
        sharedPreferences = newSharedPreferences
    }

    fun setTempGardenPolygonAlpha(alpha: Int) {

        tempGardenPolygon?.let {
            val prevColor = it.fillColor
            it.fillColor = Color.argb(
                alpha, Color.red(prevColor), Color.green(prevColor),
                Color.blue(prevColor)
            )
        }
    }


    fun setTempGardenPolygonHue(hue: Int) {

        tempGardenPolygon?.let {
            it.fillColor = Color.HSVToColor(
                Color.alpha(it.fillColor),
                floatArrayOf(hue.toFloat(), 1f, 1f)
            )
        }
    }

    // should be selected plant I guess
    fun addPlantToGarden(plantToAdd: PlantInformation2) {
        val location = activeMarker!!.position
        viewModelScope.launch {
            val newPlant = gardenRepository.addPlantToGarden(plantToAdd, location)
            _plantAdded.value = Event(newPlant)
        }
    }

    // update the plant in repository in case of shut down or something
    private fun updatePlant(plantToUpdate: PlantInformation2) {

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                gardenRepository.updatePlantInGarden(plantToUpdate)
            }
        }
    }



    // function to clear the whole
    fun clearGarden(): Boolean {


        // remove all the alarms
        removeAlarms()
        // clear the garden
        viewModelScope.launch {
            gardenRepository.clearGarden()
        }

        // remove all notifications for the garden
        notificationManager.cancelAllNotifications()
        _gardenCleared.value = true
        return true
    }



    private fun removeAlarms() {

        val notifyIntent = Intent(app, AlarmReceiver::class.java)
        // we need to create pending intents that match the plants in the list and cancel them separately
        for(plant in plantedPlants.value!!)
        {
            val requestCode = plant.id

            // recreate the correct pending intent
            val notifyPendingIntent =
                PendingIntent.getBroadcast(
                    getApplication(),
                    requestCode,
                    notifyIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )

            // cancel the alarm
            alarmManager.cancel(notifyPendingIntent)

        }
    }

    /**
     * After the navigation has taken place, make sure navigateToSelectedProperty is set to null
     */
    fun gardenClearingComplete() {
        _gardenCleared.value = false
    }




    // to setup the garden, we want to draw an overlay? polygon? and delete the last one
    // let's first try this without user interaction
    fun setupGardenPolygon(map: GoogleMap) {


        // check to see if there is a garden layout in the database
        var gardenVerticesTemp: MutableList<GardenVertex>? = gardenVertices.value as MutableList<GardenVertex>?
        if(gardenVerticesTemp == null){
            gardenVerticesTemp = mutableListOf()
        }

        // set up our own
        if(gardenVerticesTemp.size == 0)
        {
            Timber.i("setting up new polygon")
            gardenVerticesTemp.add(GardenVertex(1, 51.719, -1.241))
            gardenVerticesTemp.add(GardenVertex(2, 51.719, -1.229))
            gardenVerticesTemp.add(GardenVertex(3, 51.732, -1.229))
            gardenVerticesTemp.add(GardenVertex(4, 51.732, -1.241))
        } else {
            Timber.i("getting polygon from file")

        }

        createTempPolygon(map, gardenVerticesTemp)

    }

    fun setupGardenPolygonFromMarkerList(map: GoogleMap) {

        // if no markers have been set, then use the existing polygon, so can just change color
        if(polygonMarkerList.size == 0) {
            tempGardenPolygon = _gardenPolygon
        } else {
            // check to see if there is a garden layout in the database
            val gardenVerticesTemp: MutableList<GardenVertex> = mutableListOf()

            // loop over markers to get the vertices
            for ((index, marker) in polygonMarkerList.withIndex()) {
                gardenVerticesTemp.add(
                    GardenVertex(
                        index + 1,
                        marker.position.latitude,
                        marker.position.longitude
                    )
                )
            }

            createTempPolygon(map, gardenVerticesTemp)
        }

    }

    private fun createTempPolygon(map: GoogleMap, gardenVerticesTemp: MutableList<GardenVertex>) {


        // set the old garden to invisible
        _gardenPolygon?.isVisible = false

        // add a new polygon
        val tempGardenPolygonOptions = PolygonOptions()
        for(gardenVertex in gardenVerticesTemp) {
            //gardenVertex.
            tempGardenPolygonOptions.add(LatLng(gardenVertex.latitude, gardenVertex.longitude))
        }
        tempGardenPolygonOptions.strokeColor(R.color.colorTertiaryText)
            .fillColor(R.color.colorPrimaryDark)

        // just a temporary polygon really added to the map, we can add it back later with its options
        tempGardenPolygon = map.addPolygon(tempGardenPolygonOptions)

    }

    // remove the temporary polygon and add the old one back
    fun cancelGardenSetup() {
        // remove the temp one
        tempGardenPolygon?.remove()

        // set the old one to visible
        gardenPolygon?.isVisible = true

        removePolygonMarkers()
        activeMarker?.remove()
    }

    private fun removePolygonMarkers() {
        // potentially remove the polygon markers
        for(marker in polygonMarkerList) {
            marker.remove()
        }
        polygonMarkerList.clear()

    }

    // the old one was already removed, so we just need to save the temp
    // and also update it in the repository
    private fun replaceGardenPolygon() {
        // remove the old one if there were two
        if(_gardenPolygon != tempGardenPolygon)
            _gardenPolygon?.remove()

        // replace it with the new
        _gardenPolygon = tempGardenPolygon

        // make sure visible
        _gardenPolygon?.isVisible = true

        // update the repository
        _gardenPolygon?.let { polygon ->

            val polygonVertices = mutableListOf<GardenVertex>()
            val polygonPoints = polygon.points
            // make a list of the vertices
            for((index, point) in polygonPoints.withIndex()) {
                polygonVertices.add(GardenVertex(index, point.latitude, point.longitude))
            }

            viewModelScope.launch {
                gardenRepository.setGardenLayout(polygonVertices)
            }
        }
    }



    // we will need to save the color and stuff to repository too
    fun reloadGardenLayout(map: GoogleMap, vertices: List<GardenVertex>) {

        // convert vertices to polygon points
        val polyPoints: MutableList<LatLng> = mutableListOf()
        for(vertex in vertices) {
            polyPoints.add(LatLng(vertex.latitude, vertex.longitude))
        }

        val gardenColorPreferenceKey = app.getString(R.string.preference_garden_color_key)
        val defaultGardenColor = app.getString(R.string.garden_color_default)
        val fillColor = sharedPreferences.getString(gardenColorPreferenceKey, defaultGardenColor)

        val fillColorInt = fillColor!!.toInt()

        // when reloading, we want to take the color from the preferences file
        if(_gardenPolygon != null) {
            //fillColorInt = _gardenPolygon!!.fillColor
            _gardenPolygon!!.remove()
        }

        // we'll have to make a new one
        val gardenPolygonOptions = PolygonOptions()
        for(point in polyPoints) {
            //gardenVertex.
            gardenPolygonOptions.add(point)
        }

        gardenPolygonOptions.strokeColor(R.color.colorTertiaryText)
            .fillColor(fillColorInt)

        // just a temporary polygon really added to the map, we can add it back later with its options
        _gardenPolygon = map.addPolygon(gardenPolygonOptions)


    }

    private fun saveGardenPolygonColor() {

        // get key
        val gardenColorPreferenceKey = app.getString(R.string.preference_garden_color_key)
        // get polygon color
        val gardenColor = _gardenPolygon?.fillColor

        // we will say that you can only edit the shared preferences if you are logged in
        if (gardenColor != null) {
            sharedPreferences.edit().putString(gardenColorPreferenceKey, gardenColor.toString()).apply()
        }

    }

    fun setExistingPlantMarkers(map: GoogleMap) {

        // hmm, not sure why, but can't see existing markers anymore, need to re add them
        // maybe before we were reading all of them?
        for(marker in markerList) {
            marker.remove()
        }
        markerList.clear()

        // need to loop over planted plants
        for (plant in _plantedPlants.value!!) {
            // get the position of the active marker
            val pos = LatLng(plant.latitude, plant.longitude)

            // add marker to map and list of markers
            val newMarker = map.addMarker(
                MarkerOptions()
                    .position(pos)
                    .title(plant.name)
                    .icon(BitmapDescriptorFactory.defaultMarker(getPlantColor(plant.name)))
            )

            newMarker.tag = plant
            markerList.add(newMarker)

        }
    }

    fun cancelMovePlants() {
        // we want to move all the plants back to where they were
        for(marker in markerList) {
            val plant = marker.tag as PlantInformation2
            val pos = LatLng(plant.latitude, plant.longitude)
            marker.position = pos
        }
        setStaticMarkers()
    }

    fun confirmMovePlants() {

        for(marker in markerList) {
            val pos = marker.position
            val plant = marker.tag as PlantInformation2

            plant.latitude = pos.latitude
            plant.longitude = pos.longitude

            updatePlant(plant)
        }
        setStaticMarkers()
    }


    fun setMovableMarkers() {
        for(marker in markerList) {
            marker.isDraggable = true
        }
    }

    private fun setStaticMarkers() {
        for(marker in markerList) {
            marker.isDraggable = false
        }
    }


    fun addDraggableActiveMarker(map: GoogleMap, latLng: LatLng) {

        // set the latitude and longitude
        val snippet = String.format(
            Locale.getDefault(),
            "Lat: %1$.5f, Long: %2$.5f",
            latLng.latitude,
            latLng.longitude
        )

        activeMarkerOptions =
            MarkerOptions()
                .position(latLng)
                .title(app.getString(R.string.dropped_pin))
                .snippet(snippet)
                .draggable(true)
        activeMarker = map.addMarker(activeMarkerOptions)

    }


    fun removeActiveMarker() {
        activeMarker?.remove()
    }

    // save the activeMarker permanently with plant info
    fun savePlantMarker(map: GoogleMap, plant: PlantInformation2) {
        if(activeMarker != null) {

            // get the position of the active marker
            val pos = LatLng(plant.latitude, plant.longitude)

            // add marker to map and list of markers
            val newMarker = map.addMarker(
                MarkerOptions()
                    .position(pos)
                    .title(plant.name)
                    .icon(BitmapDescriptorFactory.defaultMarker(getPlantColor(plant.name)))
            )

            newMarker.tag = plant
            markerList.add(newMarker)
        }
    }

    fun isActiveMarkerSet(): Boolean {
        return activeMarker == null
    }

    fun confirmGardenSetup() {

        replaceGardenPolygon()
        removePolygonMarkers()
        saveGardenPolygonColor()
        activeMarker?.remove()
    }

    fun addVertexToPolygon(map: GoogleMap,latLng: LatLng) {

        // add the active marker
        addDraggableActiveMarker(map,latLng)
        val vertexNumber = polygonMarkerList.size + 1
        val imgUrl = "https://maps.google.com/mapfiles/kml/paddle/"
            .plus(vertexNumber).plus(".png")
        Picasso.get()
            .load(imgUrl)
            .into(object : Target {

                override fun onBitmapLoaded(bitmap: Bitmap, from: Picasso.LoadedFrom) {
                    /* Save the bitmap or do something with it here */
                    // now we need to set the icon to a number
                    activeMarker?.setIcon(BitmapDescriptorFactory.fromBitmap(bitmap))
                }

                override fun onPrepareLoad(placeHolderDrawable: Drawable?) { }

                override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
                    Timber.d(e,"Hello, i'm failed the bitmap")
                }

            })

        activeMarker?.let { polygonMarkerList.add(it) }

    }
}