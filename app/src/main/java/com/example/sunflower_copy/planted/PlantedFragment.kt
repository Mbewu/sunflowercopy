package com.example.sunflower_copy.planted


import android.app.Activity.RESULT_OK
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.ImageDecoder
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.method.LinkMovementMethod
import android.text.method.TransformationMethod
import android.text.util.Linkify
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.core.text.HtmlCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.*
import androidx.lifecycle.*
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.sunflower_copy.R
import com.example.sunflower_copy.SharedViewModel
import com.example.sunflower_copy.SunflowerApplication
import com.example.sunflower_copy.databinding.FragmentPlantedBinding
import com.example.sunflower_copy.domain.Plant
import com.example.sunflower_copy.ui.main.PageViewModel
import com.example.sunflower_copy.ui.main.PageViewModelFactory
import com.example.sunflower_copy.util.bindImage
import com.example.sunflower_copy.util.convertLongToDateString
import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.properties.Delegates

/**
 * This [Fragment] shows the planteded information about a selected piece of Mars real estate.
 * It sets this information in the [PlantedViewModel], which it gets as a Parcelable property
 * through Jetpack Navigation's SafeArgs.
 */
class PlantedFragment : Fragment() {

    private lateinit var binding: FragmentPlantedBinding
//    private lateinit var viewModel: PlantedViewModel
//    private lateinit var viewModelFactory: PlantedViewModelFactory

//    private lateinit var viewModel: PageViewModel
//    private lateinit var viewModelFactory: PageViewModelFactory


    private val viewModel by activityViewModels<PageViewModel> {
        PageViewModelFactory(requireActivity().application,
            (requireContext().applicationContext as SunflowerApplication).plantRepository,
            (requireContext().applicationContext as SunflowerApplication).gardenRepository)
    }
    private lateinit var selectedPlant: Plant

    private val sharedViewModel: SharedViewModel by activityViewModels()

    lateinit var currentPhotoPath: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val application = requireNotNull(activity).application
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_planted,
            container,
            false
        )

        //viewModelFactory = PageViewModelFactory(application)
        //viewModel = ViewModelProvider(requireActivity(), viewModelFactory).get(PageViewModel::class.java)
        selectedPlant = PlantedFragmentArgs.fromBundle(requireArguments()).selectedPlant

        binding.lifecycleOwner = this


        binding.viewModel = viewModel
        viewModel.setSelectedPlant(selectedPlant)
        // only init once that selected plant is known
        viewModel.initPlantedView()
        setMostRecentImages()
        setTextViews()

        // Specify the fragment view as the lifecycle owner of the binding.
        // This is used so that the binding can observe LiveData updates
        binding.lifecycleOwner = viewLifecycleOwner

        Timber.i( "in planted7")

        // create channel for notifications
        createChannel(
            getString(R.string.sunflower_notification_channel_id),
            getString(R.string.sunflower_notification_channel_name)
        )


        setObservers()



        setHasOptionsMenu(true)

        Timber.i("before the end")
        return binding.root
    }


    private fun setObservers() {


        // observer opening images
        val imgViewList = listOf<ImageView>(
            binding.mostRecentImage1,
            binding.mostRecentImage2,
            binding.mostRecentImage3,
            binding.mostRecentImage4
        )

        for((index, imgView) in imgViewList.withIndex()) {
            imgView.setOnClickListener {

                // navigate to fullscreen
                if(recentImageFiles.size > index) {
                    recentImageFiles[index]

                    // get the location information
                    this.findNavController().navigate(
                        PlantedFragmentDirections.actionPlantedFragmentToFullscreenImageFragment(recentImageFiles[index].name)
                    )
                }
            }
        }


        // observer for floating action button to take a picture
        binding.fabCamera.setOnClickListener {

            Timber.i("taking picture after click")
            dispatchTakePictureIntent()
        }

        // observer for floating action button to go to the map
        binding.fabMaps.setOnClickListener {

            // set the navigateToPlantOnMap event
            sharedViewModel.navigateToPlantOnMap.value = selectedPlant

            // get the location information
            this.findNavController().navigate(
                PlantedFragmentDirections.actionPlantedFragmentToMapFragment()
            )

        }


        // observer for floating action button to plant
        binding.fabRemove.setOnClickListener {

            val removePlantDialogFragment: DialogFragment = RemovePlantDialogFragment(selectedPlant)
            val fm: FragmentManager = requireActivity().supportFragmentManager

            removePlantDialogFragment.show(fm, "remove_plant_dialog")

            //viewModel.removePlantFromGarden()
        }


        // observer for when user has removed a plant
        // displays a toast and navigates up
        viewModel.plantRemoved.observe(viewLifecycleOwner, Observer { plantRemoved ->
            Timber.i("hello1")
            if (plantRemoved != null) {

                //val application = requireNotNull(activity).application

                Toast.makeText(
                    activity,
                    selectedPlant.name.plus(" #").plus(selectedPlant.id)
                        .plus(" removed from garden."),
                    Toast.LENGTH_SHORT
                ).show()

                Timber.i("plant removed = ".plus(plantRemoved))

                // Try and navigate up and back to the garden
                this.findNavController().navigateUp()
                viewModel.plantRemovalComplete()
                Timber.i("hello3")
            }

            Timber.i("hello plantedRemoved after = ".plus(plantRemoved))

        })



        // observer for when user is trying to overwater
        // displays a toast
        viewModel.plantOverWatering.observe(viewLifecycleOwner, Observer { plantOverWatering ->
            Timber.i("hello1")
            if (plantOverWatering) {

                //val application = requireNotNull(activity).application

                Toast.makeText(
                    activity, selectedPlant.name.plus(" #")
                        .plus(selectedPlant.id).plus(" doesn't need watering right now."),
                    Toast.LENGTH_SHORT
                ).show()

                // set overwatering flag to false
                viewModel.stoppedOverWatering()
            }
        })


        // observer for when plant is ready for watering
        viewModel.readyToWater.observe(viewLifecycleOwner, Observer { readyToWater ->
            Timber.i("hello1")
            if (readyToWater) {

                binding.timeUntilNextWatering.visibility = View.INVISIBLE
                binding.timeRemaining.visibility = View.GONE
                binding.readyToWater.visibility = View.VISIBLE

            } else {

                binding.timeUntilNextWatering.visibility = View.VISIBLE
                binding.timeRemaining.visibility = View.VISIBLE
                binding.readyToWater.visibility = View.GONE


            }
        })


        // observer for when plant is ready to harvest
        viewModel.readyToHarvest.observe(viewLifecycleOwner, Observer { readyToHarvest ->
            Timber.i("hello1")
            if (readyToHarvest) {

                binding.timeUntilNextWatering.visibility = View.INVISIBLE
                binding.timeRemaining.visibility = View.GONE
                binding.readyToWater.visibility = View.GONE

                binding.readyToHarvest.visibility = View.VISIBLE
                binding.fabHarvest.visibility = View.VISIBLE
                binding.fabWater.visibility = View.GONE

            }   // doesn't really matter because we're leaving this plant behind
//            else {
//
//                binding.timeUntilNextWatering.visibility = View.VISIBLE
//                binding.timeRemaining.visibility = View.VISIBLE
//                binding.readyToWater.visibility = View.GONE
//                binding.harvestFab.visibility = View.GONE
//                binding.waterFab.visibility = View.VISIBLE
//
//
//            }
        })


        // observer for when user has removed a plant
        // displays a toast and navigates up
        viewModel.plantHarvested.observe(viewLifecycleOwner, Observer { plantHarvested ->
            Timber.i("hello1")
            if (plantHarvested != null) {

                //val application = requireNotNull(activity).application

                Toast.makeText(
                    activity, selectedPlant.name.plus(" #")
                        .plus(selectedPlant.id).plus(" harvested from your garden!"),
                    Toast.LENGTH_SHORT
                ).show()

                Timber.i("plant harvested = ".plus(plantHarvested))

                // Try and navigate up and back to the garden
                this.findNavController().navigateUp()
                viewModel.plantHarvestingComplete()
                Timber.i("hello3")
            }

        })
    }


    fun onPlant(view: View) {
        Snackbar.make(view, "Plant added to garden.", Snackbar.LENGTH_LONG)
            .setAction("Action", null).show()
        //binding.viewModel.addPlantToGarden()
        //binding.viewModel.doSomething()


    }

    private var  target: MutableList<Target> = mutableListOf()
    private var  recentImageFiles: MutableList<File> = mutableListOf()

    private fun setMostRecentImages() {
        // file format:
        // /storage/3634-6537/Android/data/com.example.sunflower_copy/files/Pictures/JPEG_PlantName_id_20200831_124303_2239149835998839519.jpg
        // okay we need to get all the filenames for this plant
        Environment.DIRECTORY_PICTURES
        val path = requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString()
        Log.d("Files", "Path: $path")
        val directory = File(path)
        val files = directory.listFiles()
        Log.d("Files", "Size: " + files.size)

        val selectedPlantImages = mutableListOf<File>()
        val dates = mutableListOf<Int>()
        for (file in files) {
            Log.d("Files", "FileName:" + file.name)
            val parts = file.name.split("_")
            for(word in parts)
                Log.d("Files", "Part:" + word)
            // make sure it is big enough to reference them
            if(parts.size >3)
            if(parts[1] == selectedPlant.name && parts[2].toInt() == selectedPlant.id) {
                selectedPlantImages.add(file)
                dates.add(parts[3].toInt())
            }
        }


        if(selectedPlantImages.size == 0) {
            binding.mostRecentImageLayout.visibility = View.GONE
        } else {
            binding.mostRecentImageLayout.visibility = View.VISIBLE
            // sort by date, and i'm guessing the second argument is time??
            for (file in selectedPlantImages) {
                val date = file.lastModified()
                Timber.i("file last modified = ".plus(date))
            }

            Collections.sort(selectedPlantImages, object : Comparator<File> {
                override fun compare(file1: File, file2: File): Int {
                    val k = file1.lastModified() - file2.lastModified()
                    return when {
                        (k > 0) -> -1
                        (k == 0L) -> 0
                        else -> 1
                    }
                }
            })

            Timber.i("sorted images:")
            for (file in selectedPlantImages) {
                Timber.i(file.name)
            }

            val imgViewList = listOf<ImageView>(
                binding.mostRecentImage1,
                binding.mostRecentImage2,
                binding.mostRecentImage3,
                binding.mostRecentImage4
            )


            Timber.i("hi")
            // clear the target again
            target.clear()
            recentImageFiles.clear()
            // depends if we are adding a new image, actually just load them each time
            for (i in 0..3) {
                if (i < selectedPlantImages.size) {

                    recentImageFiles.add(selectedPlantImages[i])
                    target.add(object : Target {

                        override fun onBitmapLoaded(bitmap: Bitmap, from: Picasso.LoadedFrom) {
                            /* Save the bitmap or do something with it here */
                            Timber.i("hello, i'm loading the bitmap")
                            imgViewList[i].setImageBitmap(bitmap)
                            Timber.i("hello, i'm loading the bitmap2")
                        }

                        override fun onPrepareLoad(placeHolderDrawable: Drawable?) {

                            Timber.i("hello, i'm preparing the bitmap")
                        }

                        override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {

                            Timber.i("hello, i'm failed the bitmap $e")
                        }

                    })



                    val imageWidth = 130
                    val imageHeight = 0
                    Picasso.get()
                        .load(selectedPlantImages[i])
                        //.load(imgUrl)
                        .resize(imageWidth, imageHeight)
                        .placeholder(R.drawable.loading_animation)
                        .error(R.drawable.ic_broken_image)
                        //.into(imgViewList[i])
                        .into(target[i])

//                      other options
//                    Timber.i("image $i")
//                    // convert the new file
//                    Timber.i("hihun2")
//                    val source = ImageDecoder.createSource(
//                        requireActivity().contentResolver,
//                        Uri.fromFile(selectedPlantImages[i])
//                    )
//                    Timber.i("hihun6")
//                    val imageBitmap = ImageDecoder.decodeBitmap(source)
//                    imgViewList[i].setImageBitmap(imageBitmap)


                }
            }
            Timber.i("hi baby")
        }

    }




    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        Timber.i("hi1")
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        Timber.i("hi2")
        val storageDir: File? = requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        Timber.i("hi3, dir = ".plus(storageDir))
        return File.createTempFile(
            "JPEG_".plus(selectedPlant.name).plus("_").plus(selectedPlant.id)
                .plus("_${timeStamp}_"), /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            Timber.i("hi4")
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }

    val REQUEST_IMAGE_CAPTURE_AND_SAVE = 2
    val REQUEST_IMAGE_CAPTURE = 1

    private fun dispatchTakePictureIntent() {
//        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
//            takePictureIntent.resolveActivity(requireActivity().packageManager)?.also {
//                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
//            }
//        }

        Timber.i("hi")
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            Timber.i("hi1")
            takePictureIntent.resolveActivity(requireActivity().packageManager)?.also {
                Timber.i("hi2")
                // Create the File where the photo should go
                val photoFile: File? = try {
                    Timber.i("hi2a")
                    createImageFile()
                } catch (ex: IOException) {
                    // Error occurred while creating the File
                    Timber.e("Error, picture taking failed with exception: $ex")
                    null
                }
                Timber.i("hi3")

                Timber.i("hi, file =".plus(photoFile))
                // Continue only if the File was successfully created

                photoFile?.also {
                    Timber.i("hi4a")
                    try {
                        val photoURI: Uri = FileProvider.getUriForFile(
                            requireActivity(),
                            "com.example.android.fileprovider",
                            it
                        )
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE_AND_SAVE)
                    } catch (ex: IOException) {
                        Timber.e("error with uri, $ex")
                    }
                    Timber.i("hi4b")
                }
                Timber.i("hi5")
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        Timber.i("hibabe1")
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            try {
                Timber.i("hibabe2, data = ".plus(data))
                Timber.i("hibabe2, data.extras = ".plus(data?.extras))
                Timber.i("hibabe2, data?.extras?.get(\"data\") = ".plus(data?.extras?.get("data")))
                val imageBitmap = data?.extras?.get("data") as Bitmap
                Timber.i("hibabe3")
                binding.mostRecentImage1.setImageBitmap(imageBitmap)
                Timber.i("hibabe4")
            } catch (ex: IOException) {
                Timber.e("error here, $ex")
            }
        } else if (requestCode == REQUEST_IMAGE_CAPTURE_AND_SAVE && resultCode == RESULT_OK) {
            Timber.i("hihun1")
            val file = File(currentPhotoPath)
            setMostRecentImages()
            //updateMostRecentImages(file)
        }
    }

    private fun createChannel(channelId: String, channelName: String) {
        // TODO: Step 1.6 START create a channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                channelId,
                channelName,
                // TODO: Step 2.4 change importance
                NotificationManager.IMPORTANCE_HIGH
            )// TODO: Step 2.6 disable badges for this channel
                .apply {
                    setShowBadge(false)
                }

            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.RED
            notificationChannel.enableVibration(true)
            notificationChannel.description = getString(R.string.sunflower_notification_channel_description)

            val notificationManager = requireActivity().getSystemService(
                NotificationManager::class.java
            )
            notificationManager.createNotificationChannel(notificationChannel)

        }
        // TODO: Step 1.6 END create a channel
    }






    // these need to be linked to the livedata
    // well, for now, they will be called when the livedata changes
    private fun setTextViews() {

        Timber.i("setting textViews")
        Timber.i("selectedPlant.value?.id = ".plus(selectedPlant.id))
        Timber.i("viewModel.selectedPlant.value?.id = ".plus(viewModel.selectedPlant.value?.id))

        // image
        val imageView: ImageView = binding.mainPhotoImage
        //imageView.setImageURI() = getString(R.string.latin_plant_name_insert,selectedPlant.latinName)
        bindImage(imageView, selectedPlant.imageUrl)

        // plant name
        val textViewPlantName: TextView = binding.plantName
        val plantNameText = getString(
            R.string.plant_name_colon,
            selectedPlant.name,
            selectedPlant.latinName
        )
        textViewPlantName.text = HtmlCompat.fromHtml(
            plantNameText,
            HtmlCompat.FROM_HTML_MODE_LEGACY
        )

        // plant ID
        val textViewPlantId: TextView = binding.plantId
        textViewPlantId.text = getString(R.string.plant_id_colon, selectedPlant.id)

        // time planted
        val textViewTimePlanted: TextView = binding.plantedTime
        textViewTimePlanted.text = getString(
            R.string.time_planted_colon,
            selectedPlant.plantedTime?.let { convertLongToDateString(it) }
        )


        // watering interval
        val textViewWateringInterval: TextView = binding.wateringInterval
        textViewWateringInterval.text = getString(
            R.string.watering_interval_colon,
            selectedPlant.wateringInterval
        )


        // maturation time
        val textViewMaturationTime: TextView = binding.maturationTime
        val maturationMinutes = selectedPlant.getMaturationTime()?.div(60)
        val maturationSeconds = maturationMinutes?.times(60)?.let {
            selectedPlant.getMaturationTime()
                ?.minus(it)
        }
        textViewMaturationTime.text = getString(
            R.string.maturation_time_colon,
            maturationMinutes,
            maturationSeconds
        )

//          //these don't update, you have to do it in an observer or in the xml (afaik)
//        // waterings remaining
//        val textViewWateringsRemaining: TextView = binding.wateringsRemaining
//        textViewWateringsRemaining.text = getString(R.string.waterings_remaining_colon,
//            viewModel.wateringsRemaining.value
//        )
//
//        // elapsed time s
//        val textViewTimeRemaining: TextView = binding.timeRemaining
//        textViewTimeRemaining.text = "poo".plus(convertLongToElapsedTimeString(viewModel.timeRemaining.value!!))


        // plant description
        val textViewDescription: TextView = binding.plantDescription
        textViewDescription.text = selectedPlant.description?.let {
            HtmlCompat.fromHtml(
                it,
                HtmlCompat.FROM_HTML_MODE_LEGACY
            )
        }
        Linkify.addLinks(textViewDescription, Linkify.WEB_URLS);
        textViewDescription.movementMethod = LinkMovementMethod.getInstance();

    }

//    private fun onAddPlantToGarden() {
//        binding.viewModel.addPlantToGarden()
//    }
}