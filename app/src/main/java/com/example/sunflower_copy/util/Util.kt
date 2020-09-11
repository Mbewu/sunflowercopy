package com.example.sunflower_copy.util

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.text.format.DateUtils
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sunflower_copy.domain.Plant
import timber.log.Timber
import java.text.SimpleDateFormat


var GLOBAL_PLANT_ID = 0

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

//fun hideSoftKeyboard(activity: Activity) {
//    activity.currentFocus?.let {
//        val inputMethodManager = ContextCompat.getSystemService(activity, InputMethodManager::class.java)!!
//        inputMethodManager.hideSoftInputFromWindow(it.windowToken, 0)
//    }
//}

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
    Timber.i("Hi span = ".plus(span))
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

