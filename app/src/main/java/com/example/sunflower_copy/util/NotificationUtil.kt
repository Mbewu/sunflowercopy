package com.example.sunflower_copy.util

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import androidx.core.app.NotificationCompat
import com.example.sunflower_copy.MainActivity
import com.example.sunflower_copy.R
import com.example.sunflower_copy.ui.main.PlantNotificationType


// Notification ID.
private val NOTIFICATION_ID = 0
private val REQUEST_CODE = 0
private val FLAGS = 0

// TODO: Step 1.1 extension function to send messages (GIVEN)
/**
 * Builds and delivers the notification.
 *
 * @param context, activity context.
 */
fun NotificationManager.sendNotification(messageBody: String, applicationContext: Context,
                                        notificationId: Int, notificationType: PlantNotificationType,
                                        pendingIntent: PendingIntent? = null) {
    // Create the content intent for the notification, which launches
    // this activity
    // TODO: Step 1.11 create intent
    val contentIntent = Intent(applicationContext, MainActivity::class.java)

    // we now can send in the pending intent if desired
    val contentPendingIntent= pendingIntent
        ?: // TODO: Step 1.12 create PendingIntent
        PendingIntent.getActivity(
            applicationContext,
            notificationId,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

    // TODO: Step 2.0 add style
    // choose what kind of notification it is
    // - growing
    // - needs watering
    // - ready to harvest
    val sunflowerImage = when (notificationType) {
        PlantNotificationType.GROWING ->
            BitmapFactory.decodeResource(applicationContext.resources,
            R.mipmap.growing_notification_round)

        PlantNotificationType.READY_TO_HARVEST ->
            BitmapFactory.decodeResource(applicationContext.resources,
            R.mipmap.harvest_notification_round)

        PlantNotificationType.READY_TO_WATER ->
            BitmapFactory.decodeResource(applicationContext.resources,
            R.mipmap.water_notification_round)
    }

    // TODO: Step 1.2 get an instance of NotificationCompat.Builder
    // Build the notification
    val builder = NotificationCompat.Builder(
        applicationContext,
        applicationContext.getString(R.string.sunflower_notification_channel_id)
    )

        .setSmallIcon(R.drawable.sunflower_resized_small_notification)
        .setContentTitle(applicationContext
            .getString(R.string.notification_title))
        .setContentText(messageBody)

        .setContentIntent(contentPendingIntent)
        .setAutoCancel(true)

        .setLargeIcon(sunflowerImage)

        .setPriority(NotificationCompat.PRIORITY_HIGH)



    // TODO: Step 1.4 call notify
    notify(notificationId, builder.build())
}

// TODO: Step 1.14 Cancel all notifications
/**
 * Cancels all notifications.
 *
 */
fun NotificationManager.cancelNotifications(requestCode: Int) {
    cancel(requestCode)
}

fun NotificationManager.cancelAllNotifications() {
    cancelAll()
}