package com.example.sunflower_copy.map

import android.app.Activity
import android.content.Context.MODE_PRIVATE
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat.getColor
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import com.example.sunflower_copy.R
import com.example.sunflower_copy.SharedViewModel
import com.example.sunflower_copy.databinding.CustomInfoContentsBinding
import com.example.sunflower_copy.databinding.FragmentMapBinding
import com.example.sunflower_copy.databinding.MapPlantViewBinding
import com.example.sunflower_copy.domain.PlantInformation2
import com.example.sunflower_copy.title.LoginViewModel
import com.example.sunflower_copy.title.LoginViewModelFactory
import com.example.sunflower_copy.util.bindImageMaps
import com.example.sunflower_copy.util.convertLongToDateString
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import timber.log.Timber
import java.lang.IllegalStateException


class MapFragment : Fragment(), OnMapReadyCallback {

    private lateinit var map: GoogleMap

    private lateinit var binding: FragmentMapBinding
    private lateinit var viewModel: MapViewModel
    private lateinit var viewModelFactory: MapViewModelFactory
    private val sharedViewModel: SharedViewModel by activityViewModels()
    private val loginViewModel: LoginViewModel by activityViewModels {
        LoginViewModelFactory(requireActivity().application)
    }


    private var cameraPosition: CameraPosition? = null
    private var locationPermissionGranted = false
    private var homeLatLng: LatLng? = null

    // The entry point to the Fused Location Provider, tool to find current location
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private var lastKnownLocation: Location? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val application = requireNotNull(activity).application
        // Inflate the layout for this fragment
        binding = FragmentMapBinding.inflate(inflater)
        binding.lifecycleOwner = this

        viewModelFactory = MapViewModelFactory(application)
        viewModel =
            ViewModelProvider(requireActivity(), viewModelFactory).get(MapViewModel::class.java)
        binding.viewModel = viewModel

        // a lot of stuff is in onMapReady
        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireActivity())


        // get the map fragment
        val mapFrag =
            childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment


        mapFrag.getMapAsync(this)

        // we call this here to changed things like the shared preferences before the map is created
        observeAuthenticationState()

        setHasOptionsMenu(true)
        return binding.root

    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.map_options_menu, menu)

    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.aboutFragment -> {
                NavigationUI.onNavDestinationSelected(
                    item, requireView().findNavController()
                )
            }
            R.id.move_plants -> {
                // we don't need to navigate, just enable selected
                movePlants()
                true
            }
            R.id.mapFragment -> {
                // we don't need to navigate, just enable selected
                setupGardenDialog()
                true
            }
            R.id.clear_garden -> {
                clearGarden()
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupGardenDialog() {
        // Display the dialog.
        AlertDialog.Builder(requireActivity())
            .setTitle(R.string.setup_garden_dialog_title)
            .setMessage(R.string.setup_garden_dialog_message)
            .apply {
                setPositiveButton(R.string.label_yes) { _, _ -> setupGarden() }
                setNegativeButton(R.string.label_no) { _, _ ->   }
            }
            .show()
    }


    private fun clearGarden(): Boolean {
        val clearGardenDialogMapFragment: DialogFragment = ClearGardenDialogMapFragment()
        val fm: FragmentManager = requireActivity().supportFragmentManager

        clearGardenDialogMapFragment.show(fm, "remove_plant_dialog")
        return true
    }


    // map initialisation stuff
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        locationPermissionGranted = getLocationPermission(requireActivity())
        lastKnownLocation = updateLocationUI(requireActivity(), map, locationPermissionGranted)
        if(sharedViewModel.navigateToPlantOnMap.value == null) {
            getDeviceLocation(
                requireActivity(), map, fusedLocationProviderClient,
                locationPermissionGranted, cameraPosition
            )
        }
        homeLatLng = DEFAULT_LATLNG
        setHomeMarker(map, homeLatLng!!)

        // do this after the home marker so that it uses the default
        setInfoWindowLayout()
        setInfoWindowClickListener()

        // set existing plant markers
        setExistingPlantMarkers()

        // only set observers after map has been setup, some are dependent
        setObservers()
        setClickHandlers()
    }


    private fun observeAuthenticationState() {

        // in try because was really causing problems lol
        try {
            // hmm, seems to be kicking off every time we enter, which is okay, but weird
            loginViewModel.sharedPreferenceFile.observe(
                viewLifecycleOwner,
                Observer { sharedPreferenceFile ->
                     val sharedPrefs = activity?.getSharedPreferences(sharedPreferenceFile, MODE_PRIVATE)

                    // update the shared preferences in the viewModel
                    viewModel.setSharedPreferences(sharedPrefs!!)
                })
        } catch (e: IllegalStateException) {
            Timber.e(e,"Failed at loading the SharedPreference file")
        }

        // do other stuff based on changed authentication state
        loginViewModel.authenticationState.observe(viewLifecycleOwner, Observer {
            // update the shared preferences in the viewModel
        })

    }

    // set the window layout, snippet
    private fun setInfoWindowLayout() {

        map.setInfoWindowAdapter(object : GoogleMap.InfoWindowAdapter {

            // Return null here, so that getInfoContents() is called next.
            override fun getInfoWindow(arg0: Marker): View? {
                return null
            }

            override fun getInfoContents(marker: Marker): View {
                if (marker.tag is PlantInformation2) {
                    val binding = MapPlantViewBinding.inflate(layoutInflater)
                    val plant: PlantInformation2 = marker.tag as PlantInformation2

                    binding.plantNameAndId.text = getString(
                        R.string.plant_name_and_id,
                        plant.name,
                        plant.id
                    )
                    binding.plantedTime.text = getString(
                        R.string.time_planted_colon,
                        convertLongToDateString(plant.plantedTime)
                    )
                    binding.wateringsRemaining.text = getString(
                        R.string.waterings_remaining_colon,
                        plant.getWateringsRemaining()
                    )
                    binding.location.text = getString(
                        R.string.location_colon,
                        plant.latitude, plant.longitude
                    )
                    val status = binding.status
                    val harvestImage = binding.harvestImage
                    val growingImage = binding.growingImage
                    val waterImage = binding.waterImage
                    if (plant.getWateringsRemaining() == 0 && plant.getTimeRemaining() <= 0) {
                        status.text = getString(R.string.ready_to_harvest)
                        status.setTextColor(getColor(requireActivity(),R.color.colorAccent))
                        harvestImage.visibility = View.VISIBLE
                        growingImage.visibility = View.INVISIBLE
                        waterImage.visibility = View.INVISIBLE
                    } else if (plant.getTimeRemaining() > 0 && plant.wateringsDone > 0) {
                        status.text = getString(R.string.growing)
                        status.setTextColor(getColor(requireActivity(),R.color.colorPrimaryDark))
                        harvestImage.visibility = View.INVISIBLE
                        growingImage.visibility = View.VISIBLE
                        waterImage.visibility = View.INVISIBLE
                    } else {
                        status.text = getString(R.string.ready_to_water)
                        status.setTextColor(getColor(requireActivity(),R.color.colorWater))
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
                    bindImageMaps(binding.plantImage, plant.imageUrl, marker, imageWidth, imageHeight)
                    return binding.root
                } else { // the home marker
                    val binding = CustomInfoContentsBinding.inflate(layoutInflater)
                    binding.title.text = marker.title
                    binding.snippet.text = marker.snippet
                    return binding.root
                }
            }
        })
    }

    // here we need to go over the plants and add their markers
    private fun setInfoWindowClickListener() {

        // let's put our marker clickListener here because it should always be active
        map.setOnInfoWindowClickListener()
        { marker ->
            if (marker.tag is PlantInformation2) {
                // navigate to the appropriate planted page
                val selectedPlant: PlantInformation2 = marker.tag as PlantInformation2
                Timber.d("selectedPlant = ".plus(selectedPlant))
                Timber.d("selectedPlant.id = ".plus(selectedPlant.id))
                this.findNavController()
                    .navigate(MapFragmentDirections.actionMapFragmentToPlantedFragment(selectedPlant))
            }
        }
    }


    // here we need to go over the plants and add their markers
    private fun setExistingPlantMarkers() {

        viewModel.plantedPlants.observe(viewLifecycleOwner, Observer {
            viewModel.setExistingPlantMarkers(map)
        })
    }


    // set the default
    private fun setObservers() {


        // hmm, so this kind of observer will observe on createView??
        viewModel.gardenVertices.observe(viewLifecycleOwner, Observer {
            if (it.isNotEmpty()) {
                viewModel.reloadGardenLayout(map, it)
            }
        })



        viewModel.gardenCleared.observe(viewLifecycleOwner, Observer {
            if (it) {
                Toast.makeText(
                    activity, "Garden has been cleared.",
                    Toast.LENGTH_SHORT
                ).show()
                viewModel.gardenClearingComplete()
            }
        })


        sharedViewModel.navigateToPlantOnMap.observe(viewLifecycleOwner, Observer {
            if(it != null) {
                // so we just need to get the plant's coordinates and move the camera
                map.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(
                            it.latitude,
                            it.longitude
                        ), DEFAULT_ZOOM.toFloat()
                    )
                )
                sharedViewModel.navigateToPlantOnMap.value = null
            }
        })



        sharedViewModel.plantOnMap.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let { plantToAdd ->

                val plantName = plantToAdd.name

                // we need to make a clickListener that allows the placement of a single temporary marker that is draggable
                map.setOnMapLongClickListener { latLng ->
                    viewModel.addDraggableActiveMarker(map,latLng)
                    // deactivate the long click
                    map.setOnMapLongClickListener(null)
                }

                // first we need to find out where the user wants to place the plant
                binding.instructionsLayout.visibility = View.VISIBLE
                // once that is done we need activate the confirm button
                binding.fabConfirm.visibility = View.VISIBLE
                binding.fabCancel.visibility = View.VISIBLE

                // define what they do with the click listeners for these buttons

                // observer for floating action button to plant
                // send a toast, remove the active marker
                binding.fabCancel.setOnClickListener {

                    // hide the instructions and fab
                    binding.fabCancel.visibility = View.INVISIBLE
                    binding.fabConfirm.visibility = View.INVISIBLE
                    binding.instructionsLayout.visibility = View.INVISIBLE

                    // send toast
                    Toast.makeText(
                        requireNotNull(activity).application,
                        plantToAdd.name.plus(" was NOT added to your garden."),
                        Toast.LENGTH_SHORT
                    ).show()

                    viewModel.removeActiveMarker()

                    // reset onlongclick to do nothing if it wasn't activated
                    resetOnLongClick(map)
                }

                // observer for floating action button to plant
                binding.fabConfirm.setOnClickListener {

                    // if a marker was not set, tell user to select a location
                    // before confirming

                    if (viewModel.isActiveMarkerSet()) {
                        Toast.makeText(
                            requireNotNull(activity).application,
                            "Please place a marker with a long click to plant ".plus(plantName),
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {

                        // hide the instructions and fab
                        binding.fabCancel.visibility = View.INVISIBLE
                        binding.fabConfirm.visibility = View.INVISIBLE
                        binding.instructionsLayout.visibility = View.INVISIBLE

                        // add the plant to the database sending its desired location
                        viewModel.addPlantToGarden(plantToAdd)


                    }


                }

            }
        })


        // once the plant has been added, save its marker, although tbh this might be done automatically
        // with livedata observing plantedPlants??
        viewModel.plantAdded.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let { plantAdded ->
                // save a plant marker at the active marker with a plant tag
                viewModel.savePlantMarker(map, plantAdded)
                // remove the active marker
                viewModel.removeActiveMarker()

                // send toast
                Toast.makeText(
                    requireNotNull(activity).application,
                    plantAdded.name.plus(" #").plus(plantAdded.id)
                        .plus(" added to your garden on the map."),
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    // set the clickhandlers for things like instructions
    private fun setClickHandlers() {


        // make instructions text visible or invisible on click
        // using animate layout changes in instructionsLayout
        binding.instructionsTitle.setOnClickListener {
            if (binding.instructionsText.visibility == View.GONE) {
                binding.instructionsText.visibility = View.VISIBLE
            } else if (binding.instructionsText.visibility == View.VISIBLE) {
                binding.instructionsText.visibility = View.GONE
            }
        }

        // make instructions text visible or invisible on click
        binding.instructionsText.setOnClickListener {
            if (binding.instructionsText.visibility == View.GONE) {
                binding.instructionsText.visibility = View.VISIBLE
            } else if (binding.instructionsText.visibility == View.VISIBLE) {
                binding.instructionsText.visibility = View.GONE
            }
        }

    }

    private fun setupGarden() {

        // buttons to cancel and confirm
        binding.fabCancel.visibility = View.VISIBLE
        binding.fabConfirm.visibility = View.VISIBLE

        // cancel is the same for both
        cancelGardenSetupListener()

        val vertexSetup = true
        if(vertexSetup) {
            // select the vertices of the polygon
            setupGardenPolygonMarkers()

            // click listener for when vertices have been setup
            // collect all the markers that have been added to the list
            binding.fabConfirm.setOnClickListener {

                map.setOnMapLongClickListener(null)

                // if none have been entered and there is no existing garden to color, then cancel
                if(viewModel.polygonMarkerList.size == 0 && viewModel.gardenPolygon == null)
                    cancelGardenSetup()

                viewModel.setupGardenPolygonFromMarkerList(map)
                setupGardenColor()
                confirmGardenSetupListener()
            }

        } else {
            // setup the polygon and show it on screen
            viewModel.setupGardenPolygon(map)
            setupGardenColor()
            confirmGardenSetupListener()

        }



    }

    private fun cancelGardenSetupListener() {


        // setup listeners to finish the process
        binding.fabCancel.setOnClickListener {
            cancelGardenSetup()
        }
    }

    private fun cancelGardenSetup() {

        // if cancel we just need to remove the polygon
        viewModel.cancelGardenSetup()
        map.setOnMapLongClickListener(null)

        binding.instructionsLayout.visibility = View.INVISIBLE
        binding.polygonColorBar.visibility = View.INVISIBLE
        // set the buttons to invisible
        binding.fabCancel.visibility = View.INVISIBLE
        binding.fabConfirm.visibility = View.INVISIBLE

        // tell the user that garden setup has been cancelled
        Toast.makeText(
            requireNotNull(activity).application,
            "Garden setup cancelled.",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun confirmGardenSetupListener() {

        // setup listeners to finish the process
        binding.fabConfirm.setOnClickListener {
            // if confirmed we need to replace the garden polygon in the repository and
            // take it from temp

            viewModel.confirmGardenSetup()

            map.setOnMapLongClickListener(null)


            binding.instructionsLayout.visibility = View.INVISIBLE
            binding.polygonColorBar.visibility = View.INVISIBLE
            // set the buttons to invisible
            binding.fabCancel.visibility = View.INVISIBLE
            binding.fabConfirm.visibility = View.INVISIBLE

            // tell the user that garden setup has been cancelled
            Toast.makeText(
                requireNotNull(activity).application,
                "Garden setup completed.",
                Toast.LENGTH_SHORT
            ).show()

        }


    }

    private fun setupGardenPolygonMarkers() {

        // instructions
        binding.instructionsLayout.visibility = View.VISIBLE
        binding.instructionsTitle.text = getString(R.string.instructions_set_polygon_markers_title)
        binding.instructionsText.text = getString(R.string.instructions_set_polygon_markers_text)
        // we need to make user able to set  up multiple vertices and move them
        // on long click set the marker
        // set the
        viewModel.polygonMarkerList.clear()
        map.setOnMapLongClickListener { latLng ->

            viewModel.addVertexToPolygon(map,latLng)

            if(viewModel.polygonMarkerList.size == 10) {
                // tell the user that garden setup has been cancelled
                Toast.makeText(
                    requireNotNull(activity).application,
                    "A maximum of 10 corners are permitted for a garden.",
                    Toast.LENGTH_SHORT
                ).show()

                map.setOnMapLongClickListener(null)
            }
        }
    }



    // function that makes hue and alpha bar available to alter the color of the polygon
    private fun setupGardenColor() {

        binding.polygonColorBar.visibility = View.VISIBLE
        binding.instructionsTitle.text = getString(R.string.instructions_set_garden_color_title)
        binding.instructionsText.text = getString(R.string.instructions_set_garden_color_text)

        binding.fillAlphaSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {

            override fun onProgressChanged(seekBar: SeekBar, progress: Int, b: Boolean) {
                // Display the current progress of SeekBar
                binding.alphaText.text = getString(R.string.alpha_label,progress)
                viewModel.setTempGardenPolygonAlpha(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) { }
            override fun onStopTrackingTouch(seekBar: SeekBar) { }
        })

        binding.fillHueSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {

            override fun onProgressChanged(seekBar: SeekBar, progress: Int, b: Boolean) {
                // Display the current progress of SeekBar
                binding.hueText.text = getString(R.string.hue_label,progress)
                viewModel.setTempGardenPolygonHue(progress)

            }

            override fun onStartTrackingTouch(seekBar: SeekBar) { }
            override fun onStopTrackingTouch(seekBar: SeekBar) { }
        })

    }


    // allow plants to be draggable
    private fun movePlants() {

        // when we click the button, we want to select a location and then confirm or cancel it
        // first hide the button and make the other fabs visible
        binding.instructionsLayout.visibility = View.VISIBLE
        binding.fabCancel.visibility = View.VISIBLE
        binding.fabConfirm.visibility = View.VISIBLE

        // set the instructions text
        binding.instructionsTitle.text = getString(R.string.move_plants)
        binding.instructionsText.text = getString(R.string.move_plants_text)

        // now we want to activate long click to set the marker and then turn off
        viewModel.setMovableMarkers()
        setDragListener(map)
        //showCurrentPlace()
        //viewModel.addPlantToGarden()

        // set the click listeners for the cancel and confirm button here

        // observer for floating action button to plant
        binding.fabCancel.setOnClickListener {

            // when we click the button, we want to select a location and then confirm or cancel it
            // first hide the button and make the other fabs visible
            binding.instructionsLayout.visibility = View.INVISIBLE
            binding.fabCancel.visibility = View.INVISIBLE
            binding.fabConfirm.visibility = View.INVISIBLE

            // move plants back
            viewModel.cancelMovePlants()


        }

        // observer for floating action button to plant
        binding.fabConfirm.setOnClickListener {

            // when we click the button, we want to select a location and then confirm or cancel it
            // first hide the button and make the other fabs visible
            binding.instructionsLayout.visibility = View.INVISIBLE
            binding.fabCancel.visibility = View.INVISIBLE
            binding.fabConfirm.visibility = View.INVISIBLE

            // we want to move all the plants to their new positions
            // then update that data in the repository
            viewModel.confirmMovePlants()
        }

    }




    private fun setDragListener(map: GoogleMap) {

        // set what to happen when dragging
        // nothing, but for some reason, we need to do something to have it set??
        map.setOnMarkerDragListener(object : GoogleMap.OnMarkerDragListener {

            // when the drag has finished, don'tchange the location of the plant
            override fun onMarkerDragEnd(marker: Marker) {
                Timber.d("Marker moved to (".plus(marker.position.latitude.toString()).plus(",")
                    .plus(marker.position.longitude.toString()).plus(")"))
            }

            // implement extra methods
            override fun onMarkerDragStart(arg0: Marker) {}
            override fun onMarkerDrag(marker: Marker?) {}
        })
    }


    private fun resetOnLongClick(map: GoogleMap) {

        map.setOnMapLongClickListener {
            // deactivate the long click
            map.setOnMapLongClickListener(null)
        }
    }


    // [START maps_current_place_get_device_location]
    private fun getDeviceLocation(activity: Activity, map: GoogleMap,
                          fusedLocationProviderClient: FusedLocationProviderClient,
                          locationPermissionGranted: Boolean,
                          cameraPosition: CameraPosition?) {


        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (locationPermissionGranted) {
                val locationResult = fusedLocationProviderClient.lastLocation
                locationResult.addOnCompleteListener(activity) { task ->
                    if (task.isSuccessful) {
                        // Set the map's camera position to the current location of the device.
                        lastKnownLocation = task.result
                        if (lastKnownLocation != null) {
                            // if we knew a camera position before just use that
                            if(cameraPosition == null) {
                                map.moveCamera(
                                    CameraUpdateFactory.newLatLngZoom(
                                        LatLng(
                                            lastKnownLocation!!.latitude,
                                            lastKnownLocation!!.longitude
                                        ), DEFAULT_ZOOM.toFloat()
                                    )
                                )
                            } else {
                                map.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
                            }
                        }
                    } else {
                        Timber.d("Current location is null. Using defaults.")
                        Timber.e(task.exception, "Exception: ")
                        map.moveCamera(
                            CameraUpdateFactory
                                .newLatLngZoom(DEFAULT_LATLNG, DEFAULT_ZOOM.toFloat())
                        )
                        map.uiSettings?.isMyLocationButtonEnabled = false
                    }
                }


            } else {

                Timber.d("Location permissions not yet granted. Using defaults.")
                map.moveCamera(
                    CameraUpdateFactory
                        .newLatLngZoom(DEFAULT_LATLNG, DEFAULT_ZOOM.toFloat())
                )
                map.uiSettings?.isMyLocationButtonEnabled = false
            }
        } catch (e: SecurityException) {
            Timber.e(e)
        }

    }



    // [START maps_current_place_on_request_permissions_result]
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        locationPermissionGranted = false
        when (requestCode) {
            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION -> {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
                    locationPermissionGranted = true
                }
            }
        }
        lastKnownLocation = updateLocationUI(requireActivity(), map, locationPermissionGranted)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        if (savedInstanceState != null) {
            //Restore the fragment's state here
            lastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION)
            cameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable(KEY_CAMERA_POSITION, map.cameraPosition)
        outState.putParcelable(KEY_LOCATION, lastKnownLocation)
        super.onSaveInstanceState(outState)
    }

    // whenever we are close to leaving and having to redo the map let's save the camera position
    override fun onPause() {
        super.onPause()
        // save the camera position
        cameraPosition = map.cameraPosition
    }
}