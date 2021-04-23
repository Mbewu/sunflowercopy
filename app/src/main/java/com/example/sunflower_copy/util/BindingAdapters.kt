package com.example.sunflower_copy.util

import android.text.format.DateUtils
import android.text.method.LinkMovementMethod
import android.text.util.Linkify
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.net.toUri
import androidx.core.text.HtmlCompat
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



/**
 * Binding adapter that handles the output of html text, e.g. italics and links
 */
@BindingAdapter("bind:htmlText")
fun bindHtmlText(textView: TextView, text: String?) {
    text?.let{
        // set the text and convert it to html
        textView.text = HtmlCompat.fromHtml(
            it,
            HtmlCompat.FROM_HTML_MODE_LEGACY
        )

        // let it have links
        Linkify.addLinks(textView, Linkify.WEB_URLS)
        textView.movementMethod = LinkMovementMethod.getInstance()
    }
}



/**
 * Binding adapter that handles the output of duration texts e.g. 1 min 20 sec
 */
@BindingAdapter("bind:durationText")
fun bindDurationText(textView: TextView, duration: Int?) {
    if (duration != null) {
        textView.text = convertIntSecondsToString(duration)
    }
}


/**
 * Binding adapter that handles the output of time and date e.g. 20 September 2021 at 10:30
 */
@BindingAdapter("bind:timeDateText")
fun bindTimeDateText(textView: TextView, time: Long?) {
    if (time != null) {
        textView.text = convertLongToDateString(time)
    }
}


/**
 * Binding adapter that handles the output of locations
 */
@BindingAdapter(value = ["bind:latitudeText","bind:longitudeText"], requireAll = true)
fun bindLocationText(textView: TextView, latitude: Double?, longitude: Double?) {
    if (latitude != null && longitude != null) {
        textView.text = convertLatLngToString(latitude,longitude)
    }
}



/**
 * When there is no Mars property data (data is null), hide the [RecyclerView], otherwise show it.
 */
@BindingAdapter("bind:listData")
fun bindRecyclerView(recyclerView: RecyclerView, data: List<Plant>?) {
    val adapter = recyclerView.adapter as PlantGridSearchAdapter
    adapter.submitList(data)
}



/**
 * Uses the Glide library to load an image by URL into an [ImageView]
 */
@BindingAdapter("bind:imageUrl")
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

