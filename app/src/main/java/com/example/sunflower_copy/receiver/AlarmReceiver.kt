package com.example.sunflower_copy.receiver

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavDeepLinkBuilder
import com.example.sunflower_copy.MainActivity
import com.example.sunflower_copy.R
import com.example.sunflower_copy.SunflowerApplication
import com.example.sunflower_copy.domain.Plant
import com.example.sunflower_copy.repository.GardenRepository
import com.example.sunflower_copy.util.sendNotification
import com.example.sunflower_copy.ui.main.PlantNotificationType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber


class AlarmReceiver: BroadcastReceiver() {

    private lateinit var gardenRepository: GardenRepository

    override fun onReceive(context: Context, intent: Intent) {

        Timber.i("in alarm receiver")
        // get some data
        val extras = intent.extras
        val plantName = extras?.getString("name")
        val plantId = extras?.getLong("id")
        val requestCode = plantId!!.toInt()   //notification id is always the plantId
        val wateringsRemaining = extras?.getInt("wateringsRemaining")

        // TODO: Step 1.9 add call to sendNotification
        val notificationManager = ContextCompat.getSystemService(
            context,
            NotificationManager::class.java
        ) as NotificationManager

        val argumentBundle = Bundle()
        argumentBundle.putLong("selectedPlantId",plantId)
        val pendingIntent = NavDeepLinkBuilder(context)
            .setComponentName(MainActivity::class.java)
            .setGraph(R.navigation.navigation)
            .setDestination(R.id.plantedFragment)
            .setArguments(extras)
            .createPendingIntent()

        for(key in extras.keySet())
            Timber.d("key = $key")


        // toast and notification (don't really want a toast)
        if(wateringsRemaining > 0) {
            Toast.makeText(context, context.getString(
                R.string.timer_complete_watering,plantName,plantId), Toast.LENGTH_SHORT).show()
            notificationManager.sendNotification(
                context.getString(R.string.timer_complete_watering,plantName,plantId).toString(),
                context,requestCode,PlantNotificationType.READY_TO_WATER

            )
        }
        else {
            Toast.makeText(context, context.getString(
                R.string.timer_complete_harvesting,plantName,plantId), Toast.LENGTH_SHORT).show()
            notificationManager.sendNotification(
                context.getString(R.string.timer_complete_harvesting,plantName,plantId).toString(),
                context,requestCode,PlantNotificationType.READY_TO_HARVEST
            )
        }


        // update the repository, not sure if the repository will be here
        // seems to work fine tbh. this only works when in the background, which is fine.
        // when the program restarts everything will be loaded anyway.
        gardenRepository = (context.applicationContext as SunflowerApplication).gardenRepository

        // let us test getting a plant
        val tempScope = CoroutineScope(Dispatchers.IO)
        tempScope.launch {
            val plant = gardenRepository.getPlant(plantId)
            Timber.i("plant = $plant")
            Timber.i("plant wasGrowing before = ${plant.wasGrowing}")
            plant.isGrowing()
            Timber.i("plant wasGrowing after = ${plant.wasGrowing}")
            gardenRepository.updatePlantInGarden(plant)
        }


    }

}