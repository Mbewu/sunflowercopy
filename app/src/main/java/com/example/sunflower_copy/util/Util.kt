package com.example.sunflower_copy.util

import android.annotation.SuppressLint
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Parcel
import android.os.Parcelable
import android.provider.MediaStore
import android.text.format.DateUtils
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sunflower_copy.R
import com.example.sunflower_copy.domain.Plant
import com.google.android.gms.maps.model.Marker
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*


var GLOBAL_PLANT_ID: Long = 0

fun getPlantColor(plantName: String): Float {

    return when(plantName) {
        "Apple" -> 0f
        "Beet" -> 20f
        "Cilantro" -> 40f
        "Tomato" -> 60f
        "Avocado" -> 80f
        "Pear" -> 100f
        "Eggplant" -> 120f
        "Grape" -> 140f
        "Mango" -> 160f
        "Orange" -> 180f
        "Sunflower" -> 200f
        "Watermelon" -> 220f
        "Hibiscus" -> 240f
        "Pink & White Lady's Slipper" -> 260f
        "Rocky Mountain Columbine" -> 280f
        "Yulan Magnolia" -> 300f
        "Bougainvillea" -> 320f
        else -> 340f
    }
}

/**
 * Take the Long milliseconds returned by the system and stored in Room,
 * and convert it to a nicely formatted string for display.
 *
 * EEEE - Display the long letter version of the weekday
 * MMM - Display the letter abbreviation of the nmotny
 * dd-yyyy - day in month and full year numerically
 * HH:mm - Hours and minutes in 24hr format
 */
@SuppressLint("SimpleDateFormat")
fun convertLongToDateString(systemTime: Long): String {
    return SimpleDateFormat("MMM dd, yyyy 'at' HH:mm")
        .format(systemTime).toString()
}

@SuppressLint("SimpleDateFormat")
fun convertLongToDateNoTimeString(systemTime: Long): String {
    return SimpleDateFormat("MMM dd, yyyy")
        .format(systemTime).toString()
}

fun convertIntSecondsToString(time: Int): String {
    val hours = time/3600
    val minutes = time/60 - hours*60
    val seconds = time - minutes*60 - hours*3600
    return when {
        hours > 0 -> "$hours hr $minutes min $seconds sec"
        minutes > 0 -> "$minutes min $seconds sec"
        else -> "$seconds sec"
    }
}

fun convertLatLngToString(latitude: Double, longitude: Double): String {
    return "(" + String.format("%.3f",latitude) + "," + String.format("%.3f",longitude) + ")"
}

fun hideKeyboard(activity: Activity) {
    val inputManager: InputMethodManager = activity
        .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

    // check if no view has focus:
    val currentFocusedView: View? = activity.currentFocus
    if (currentFocusedView != null) {
        inputManager.hideSoftInputFromWindow(
            currentFocusedView.windowToken,
            InputMethodManager.HIDE_NOT_ALWAYS
        )
    }
}

fun View.hideKeyboard() {
    val inputMethodManager = context!!.getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as? InputMethodManager
    inputMethodManager?.hideSoftInputFromWindow(this.windowToken, 0)
}


fun setupUI(view: View, activity: Activity) {

    // Set up touch listener for non-text box views to hide keyboard.
    if (view !is SearchView) {
        // we don't want it to do a click actually
        view.setOnTouchListener { v, event ->
            hideKeyboard(activity);
            //v.performClick();
            return@setOnTouchListener false
        }
    }

    //If a layout container, iterate over children and seed recursion.
    if (view is ViewGroup) {
        for (i in 0 until view.childCount) {
            val innerView = view.getChildAt(i)
            setupUI(innerView, activity)
        }
    }
}


fun calculateNoOfColumns(context: Context, columnWidthDp: Int): Int { // For example columnWidthdp=180
    val displayMetrics = context.resources.displayMetrics
    val screenWidthDp = displayMetrics.widthPixels / displayMetrics.density
    return (screenWidthDp / columnWidthDp + 0.5).toInt()

}


fun setRecyclerViewSpan(context: Context, recyclerView: RecyclerView) {

    // set the number of columns
    Timber.i("Hi")
    val activity = context as Activity
    val span = calculateNoOfColumns(context, 160)
    Timber.i("Hi span = $span")
    val layoutManager = GridLayoutManager(activity, span)

    // reset for the header
    layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
        override fun getSpanSize(position: Int) =  when (position) {
            0 -> span
            else -> 1
        }
    }


    recyclerView.layoutManager = layoutManager
}


object ParcelableUtil {
    fun marshall(parceable: Parcelable): ByteArray {
        val parcel = Parcel.obtain()
        parceable.writeToParcel(parcel, 0)
        val bytes = parcel.marshall()
        parcel.recycle()
        return bytes
    }

    fun unmarshall(bytes: ByteArray): Parcel {
        val parcel = Parcel.obtain()
        parcel.unmarshall(bytes, 0, bytes.size)
        parcel.setDataPosition(0) // This is extremely important!
        return parcel
    }

    fun <T> unmarshall(bytes: ByteArray, creator: Parcelable.Creator<T>): T {
        val parcel = unmarshall(bytes)
        val result = creator.createFromParcel(parcel)
        parcel.recycle()
        return result
    }
}



fun numPlantsGrowing(plants: List<Plant>): Int {
    var numPlants = 0
    // loop over and count the number of plants to water
    for (plant in plants) {
        if(plant.isGrowing()) {
            numPlants++
        }
    }
    return numPlants
}

fun numPlantsToWater(plants: List<Plant>): Int {
    var numPlants = 0
    // loop over and count the number of plants to water
    for (plant in plants) {
        if(plant.isReadyToWater()) {
            numPlants++
        }
    }
    return numPlants
}

fun numPlantsToHarvest(plants: List<Plant>): Int {
    var numPlants = 0
    // loop over and count the number of plants to water
    for (plant in plants) {
        if(plant.isReadyToHarvest()) {
            numPlants++
        }
    }
    return numPlants
}




/**
 * Uses the Glide library to load an image by URL into an [ImageView]
 */
fun loadInfoWindowImage(imgView: ImageView, imgUrl: String?, marker: Marker, imageWidth: Int, imageHeight: Int) {

    Timber.i("image (width,height) = ($imageWidth,$imageHeight)")
    imgUrl?.let {
        val imgUri = imgUrl.toUri().buildUpon().scheme("https").build()
        Picasso.get().load(imgUri)
            //.fit()    // this causes it not to load properly
            .resize(imageWidth,imageHeight)
            .centerCrop()
            .noFade()
            .placeholder(R.drawable.loading_animation)
            .error(R.drawable.ic_broken_image)
            //.into(imgView)
            .into(imgView, MarkerCallback(marker))
    }

}


class MarkerCallback internal constructor(marker: Marker?) :
    Callback {
    var marker: Marker? = null
    override fun onError(e: Exception?) {
        Log.e(javaClass.simpleName, "Error loading thumbnail!")
    }

    override fun onSuccess() {
        Timber.i("hello, in onSuccess1")
        if (marker != null && marker!!.isInfoWindowShown) {
            Timber.i("hello, in onSuccess2")
            marker!!.hideInfoWindow()
            marker!!.showInfoWindow()
        }
    }

    init {
        Timber.i("hello, in init")
        this.marker = marker
    }
}




fun loadImage(imgView: ImageView, imgFile: File){
    Picasso.get().load(imgFile)
        //.fit()    // this causes it not to load properly
        //.resize(imageWidth,imageHeight)
        //.centerCrop()
        //.noFade()
        .placeholder(R.drawable.loading_animation)
        .error(R.drawable.ic_broken_image)
        //.into(imgView)
        .into(imgView)
}



fun dispatchTakeAndSavePictureIntentSimple(fragment: Fragment, fileNameBase: String, intentLauncher: ActivityResultLauncher<Uri>) {

    Timber.i("hi")
    Timber.i("hi2")
    // Create the File where the photo should go
    val photoFile: File? = try {
        Timber.i("hi2a")
        createImageFile(fragment.requireActivity(),fileNameBase)
    } catch (ex: IOException) {
        // Error occurred while creating the File
        Timber.e("Error, failed to create file for picture with exception: $ex")
        null
    }
    Timber.i("hi3")

    Timber.i("hi, file = $photoFile")
    // Continue only if the File was successfully created

    photoFile?.also {
        Timber.i("hi4a")
        try {
            val photoURI: Uri = FileProvider.getUriForFile(
                fragment.requireActivity(),
                "com.example.android.fileprovider",
                it
            )
            //takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            //fragment.startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE_AND_SAVE)
            intentLauncher.launch(photoURI)
        } catch (ex: IOException) {
            Timber.e("error with uri, $ex")
        }
        Timber.i("hi4b")
    }
    Timber.i("hi5")
}






@Throws(IOException::class)
@SuppressLint("SimpleDateFormat")
fun createImageFile(activity: Activity, fileNameBase: String): File {
    // Create an image file name
    Timber.i("hi1")
    val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
    Timber.i("hi2")
    val storageDir: File? = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    Timber.i("hi3, dir = $storageDir")
    return File.createTempFile(
        "JPEG_${fileNameBase}_${timeStamp}_", /* prefix */
        ".jpg", /* suffix */
        storageDir /* directory */
    )
}



fun createChannel(activity: Activity, channelId: String, channelName: String) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val notificationChannel = NotificationChannel(
            channelId,
            channelName,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
                setShowBadge(false)
            }

        notificationChannel.enableLights(true)
        notificationChannel.lightColor = Color.RED
        notificationChannel.enableVibration(true)
        notificationChannel.description = activity.getString(R.string.sunflower_notification_channel_description)

        val notificationManager = activity.getSystemService(
            NotificationManager::class.java
        )
        notificationManager.createNotificationChannel(notificationChannel)

    }
    // TODO: Step 1.6 END create a channel
}