package com.example.sunflower_copy.map

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import androidx.core.content.ContextCompat
import com.example.sunflower_copy.R
import com.example.sunflower_copy.databinding.CustomInfoContentsBinding
import com.example.sunflower_copy.databinding.MapPlantViewBinding
import com.example.sunflower_copy.domain.Plant
import com.example.sunflower_copy.util.convertLongToDateNoTimeString
import com.example.sunflower_copy.util.loadInfoWindowImage
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker

class PlantInfoWindowAdapter(
    private val context: Context
) : GoogleMap.InfoWindowAdapter {

    private val appCtx: Context = context.applicationContext

    // Return null here, so that getInfoContents() is called next.
    override fun getInfoWindow(arg0: Marker): View? {
        val marker = arg0
        if (marker.tag is Plant) {
            val binding = MapPlantViewBinding.inflate(LayoutInflater.from(context))
            val plant: Plant = marker.tag as Plant

            binding.plantNameAndId.text = appCtx.getString(
                R.string.plant_name_and_id,
                plant.name,
                plant.id
            )
            binding.plantedTime.text = appCtx.getString(
                R.string.date_planted_colon_old,
                convertLongToDateNoTimeString(plant.plantedTime)
            )
            binding.wateringsDone.text = appCtx.getString(
                R.string.waterings_done_colon_old,
                plant.wateringsDone
            )
            binding.location.text = appCtx.getString(
                R.string.location_colon_old,
                plant.latitude, plant.longitude
            )
            val status = binding.status
            val harvestImage = binding.harvestImage
            val growingImage = binding.growingImage
            val waterImage = binding.waterImage
            if (plant.isReadyToHarvest()) {
                status.text = appCtx.getString(R.string.ready_to_harvest)
                status.setTextColor(ContextCompat.getColor(appCtx, R.color.colorAccent))
                harvestImage.visibility = View.VISIBLE
                growingImage.visibility = View.INVISIBLE
                waterImage.visibility = View.INVISIBLE
            } else if (plant.isGrowing()) {
                status.text = appCtx.getString(R.string.growing)
                status.setTextColor(
                    ContextCompat.getColor(
                        appCtx,
                        R.color.colorPrimaryDark
                    )
                )
                harvestImage.visibility = View.INVISIBLE
                growingImage.visibility = View.VISIBLE
                waterImage.visibility = View.INVISIBLE
            } else {
                status.text = appCtx.getString(R.string.ready_to_water)
                status.setTextColor(ContextCompat.getColor(appCtx, R.color.colorWater))
                harvestImage.visibility = View.INVISIBLE
                growingImage.visibility = View.INVISIBLE
                waterImage.visibility = View.VISIBLE
            }

            // image
            // give a good value for a least one dimension to get a good sized image,
            // here we set the height because that is limited by the text
            // Picasso will keep the aspect ratio correct with 0 as one of the parameters
            val imageWidth = 0
            val imageHeight = 120
            loadInfoWindowImage(
                binding.plantImage,
                plant.imageUrl,
                marker,
                imageWidth,
                imageHeight
            )
            return binding.root
        } else { // the home marker
            val binding = CustomInfoContentsBinding.inflate(LayoutInflater.from(context))
            binding.title.text = marker.title
            binding.snippet.text = marker.snippet
            return binding.root
        }
    }

    override fun getInfoContents(marker: Marker): View? {
        return null
    }

}