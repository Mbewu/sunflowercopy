package com.example.sunflower_copy.util

import android.text.format.DateUtils
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.net.toUri
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.sunflower_copy.R
import com.example.sunflower_copy.domain.Plant
import com.example.sunflower_copy.ui.main.PlantApiStatus
import com.example.sunflower_copy.ui.main.PlantGridSearchAdapter
import com.google.android.gms.maps.model.Marker
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import timber.log.Timber
import java.io.File
import java.lang.Exception


//
///**
// * When there is no Mars property data (data is null), hide the [RecyclerView], otherwise show it.
// */
//@BindingAdapter("listData")
//fun bindRecyclerView(recyclerView: RecyclerView, data: List<Plant>?) {
//    val adapter = recyclerView.adapter as PlantGridAdapter
//    adapter.submitList(data)
//}

var previousImageUrl: String? = null



/**
 * When there is no Mars property data (data is null), hide the [RecyclerView], otherwise show it.
 */
@BindingAdapter("listData")
fun bindRecyclerView(recyclerView: RecyclerView, data: List<Plant>?) {
    val adapter = recyclerView.adapter as PlantGridSearchAdapter
    adapter.submitList(data)
}



/**
 * Uses the Glide library to load an image by URL into an [ImageView]
 */
@BindingAdapter("imageUrl")
fun bindImage(imgView: ImageView, imgUrl: String?) {
    imgUrl?.let {
        val imgUri = imgUrl.toUri().buildUpon().scheme("https").build()
        val glide = false
        if(glide) {
            Glide.with(imgView.context)
                .load(imgUri)
                .apply(
                    RequestOptions()
                        .placeholder(R.drawable.loading_animation)
                        .error(R.drawable.ic_broken_image)
                )
                .into(imgView)
        } else {
            //Picasso.get().load(imgUri).into(imgView)
            Picasso.get().load(imgUri).fit().centerCrop()
                .placeholder(R.drawable.loading_animation)
                .error(R.drawable.ic_broken_image)
                .into(imgView)
        }
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



/**
 * Uses the Glide library to load an image by URL into an [ImageView]
 */
//@BindingAdapter("imgUrl")
fun bindImageMaps(imgView: ImageView, imgUrl: String?, marker: Marker, imageWidth: Int, imageHeight: Int) {

    Timber.i("image (width,height) = (".plus(imageWidth).plus(",").plus(imageHeight).plus(")"))
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


/**
 * This binding adapter displays the [MarsApiStatus] of the network request in an image view.  When
 * the request is loading, it displays a loading_animation.  If the request has an error, it
 * displays a broken image to reflect the connection error.  When the request is finished, it
 * hides the image view.
 */
@BindingAdapter("marsApiStatus")
fun bindStatus(statusImageView: ImageView, status: PlantApiStatus?) {
    when (status) {
        PlantApiStatus.LOADING -> {
            statusImageView.visibility = View.VISIBLE
            statusImageView.setImageResource(R.drawable.loading_animation)
        }
        PlantApiStatus.ERROR -> {
            statusImageView.visibility = View.VISIBLE
            statusImageView.setImageResource(R.drawable.ic_connection_error)
        }
        PlantApiStatus.DONE -> {
            statusImageView.visibility = View.GONE
        }
    }
}


/**
 * Converts milliseconds to formatted mm:ss
 *
 * @param value, time in milliseconds.
 */
@BindingAdapter("elapsedTime")
fun TextView.setElapsedTime(value: Long) {
    val seconds = value / 1000
    text = if (seconds < 60) seconds.toString() else DateUtils.formatElapsedTime(seconds)
}
