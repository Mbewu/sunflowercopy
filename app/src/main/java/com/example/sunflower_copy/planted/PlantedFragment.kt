package com.example.sunflower_copy.planted


import android.app.NotificationChannel
import android.app.NotificationManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.*
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.example.sunflower_copy.R
import com.example.sunflower_copy.SharedViewModel
import com.example.sunflower_copy.SunflowerApplication
import com.example.sunflower_copy.databinding.FragmentPlantedBinding
import com.example.sunflower_copy.domain.Plant
import com.example.sunflower_copy.ui.main.PageViewModel
import com.example.sunflower_copy.ui.main.PageViewModelFactory
import com.example.sunflower_copy.util.*
import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import timber.log.Timber
import java.io.File
import java.util.*

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

    private val args: PlantedFragmentArgs by navArgs()

    private val plantedViewModel by viewModels<PlantedViewModel> {
        PlantedViewModelFactory(requireActivity().application,
            (requireContext().applicationContext as SunflowerApplication).gardenRepository)
    }


    private val viewModel by activityViewModels<PageViewModel> {
        PageViewModelFactory(requireActivity().application,
            (requireContext().applicationContext as SunflowerApplication).plantRepository,
            (requireContext().applicationContext as SunflowerApplication).gardenRepository)
    }
    private lateinit var selectedPlant: Plant

    private val sharedViewModel: SharedViewModel by activityViewModels()

    lateinit var currentPhotoPath: String

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { isSuccess ->
        Timber.i("HelloBaby")
        if (isSuccess) {
            setMostRecentImages()
        }
    }
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
        selectedPlant = args.selectedPlant

        binding.lifecycleOwner = this


        binding.viewModel = viewModel
        viewModel.setSelectedPlant(selectedPlant)
        // only init once that selected plant is known
        viewModel.initPlantedView()
        setMostRecentImages()

        binding.plantedViewModel = plantedViewModel
        plantedViewModel.start(selectedPlant.id)

        // Specify the fragment view as the lifecycle owner of the binding.
        // This is used so that the binding can observe LiveData updates
        binding.lifecycleOwner = viewLifecycleOwner

        Timber.i( "in planted7")



        setObservers()



        setHasOptionsMenu(true)

        Timber.i("before the end")
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //this.setupRefreshLayout(viewDataBinding.refreshLayout)
        Timber.i("starting plantedViewModel")
        plantedViewModel.start(args.selectedPlant.id)


        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)

        binding.toolbar
            .setupWithNavController(navController, appBarConfiguration)

        (activity as AppCompatActivity).supportActionBar?.hide()
        (activity as AppCompatActivity).setSupportActionBar(binding.toolbar)

        (activity as AppCompatActivity).supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_back_circle);

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
            val filenameBase = "${selectedPlant.name}_${selectedPlant.id}"
            dispatchTakeAndSavePictureIntentSimple(this,filenameBase,cameraLauncher)
        }

        // observer for floating action button to go to the map
        binding.fabMaps.setOnClickListener {
            // get the location information
            this.findNavController().navigate(
                PlantedFragmentDirections.actionPlantedFragmentToMapFragment().setSelectedPlantId(selectedPlant.id)
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
                    "${selectedPlant.name} #${selectedPlant.id} removed from garden.",
                    Toast.LENGTH_SHORT
                ).show()

                Timber.i("plant removed = $plantRemoved")

                // Try and navigate up and back to the garden
                this.findNavController().navigateUp()
                viewModel.plantRemovalComplete()
                Timber.i("hello3")
            }

            Timber.i("hello plantedRemoved after = $plantRemoved")

        })



        // observer for when user is trying to overwater
        // displays a toast
        viewModel.plantOverWatering.observe(viewLifecycleOwner, Observer { plantOverWatering ->
            Timber.i("hello1")
            if (plantOverWatering) {

                //val application = requireNotNull(activity).application

                Toast.makeText(
                    activity,
                    "${selectedPlant.name} #${selectedPlant.id} doesn't need watering right now.",
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
                    activity,
                    "${selectedPlant.name} #${selectedPlant.id} harvested from your garden!",
                    Toast.LENGTH_SHORT
                ).show()

                Timber.i("plant harvested = $plantHarvested")

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
            if(parts[1] == selectedPlant.name && parts[2].toLong() == selectedPlant.id) {
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
                Timber.i("file last modified = $date")
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



}