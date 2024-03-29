package com.example.sunflower_copy.detail


import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.text.util.Linkify
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
import androidx.core.text.HtmlCompat.FROM_HTML_MODE_LEGACY
import androidx.fragment.app.*
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.example.sunflower_copy.R
import com.example.sunflower_copy.SunflowerApplication
import com.example.sunflower_copy.databinding.FragmentDetailBinding
import com.example.sunflower_copy.domain.Plant
import com.example.sunflower_copy.ui.main.PageViewModel
import com.example.sunflower_copy.ui.main.PageViewModelFactory
import com.example.sunflower_copy.util.bindImage
import com.google.android.material.snackbar.Snackbar
import timber.log.Timber

class DetailFragment : Fragment() {

    private lateinit var binding: FragmentDetailBinding
//    private lateinit var viewModel: PageViewModel
//    private lateinit var viewModelFactory: PageViewModelFactory

    private val viewModel by activityViewModels<PageViewModel> {
        PageViewModelFactory(requireActivity().application,
            (requireContext().applicationContext as SunflowerApplication).plantRepository,
            (requireContext().applicationContext as SunflowerApplication).gardenRepository)
    }
    private lateinit var selectedPlant: Plant


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val application = requireNotNull(activity).application
        binding = FragmentDetailBinding.inflate(inflater)
        binding.lifecycleOwner = this

        //viewModelFactory = PageViewModelFactory(application)
        //viewModel = ViewModelProvider(requireActivity(), viewModelFactory).get(PageViewModel::class.java)

        // detail view doesn't actually use selected plant, so we could get away with it..
        selectedPlant = DetailFragmentArgs.fromBundle(requireArguments()).selectedPlant
        viewModel.setSelectedPlant(selectedPlant)

        // need to refresh the plants to make sure selectedPlant in the viewModel is set

        viewModel.getPlantInformation()


        binding.viewModel = viewModel

        // set the plantAdded observer
        setObservers()

        // set the textViews
        setTextViews(selectedPlant)

        setHasOptionsMenu(true)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)

        binding.toolbar
            .setupWithNavController(navController, appBarConfiguration)

        (activity as AppCompatActivity).supportActionBar?.hide()
        (activity as AppCompatActivity).setSupportActionBar(binding.toolbar)

        (activity as AppCompatActivity).supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_back_circle);

    }


    private fun setObservers() {



        // observer for floating action button to plant
        binding.addPlantFab.setOnClickListener {

            val addPlantDialogFragment: DialogFragment = AddPlantDialogFragment()
            val fm: FragmentManager = requireActivity().supportFragmentManager
            addPlantDialogFragment.show(fm, "add_plant_dialog")
        }

        // Observe the plantAdded LiveData and Navigate when it isn't null
        // After navigating, call displayPropertyDetailsComplete() so that the ViewModel is ready
        // for another navigation event.
        viewModel.plantAdded.observe(viewLifecycleOwner, Observer { plantAdded ->

            if(plantAdded != null) {
                view?.let { view ->
                    Snackbar.make(
                        view,
                        "${plantAdded.name} #${plantAdded.id} added to your garden.",
                        Snackbar.LENGTH_LONG
                    )
                        .setAction("Action", null).show()
                }
                // reset to null
                viewModel.plantAddedComplete()
            }


        })


        // navigate to the map but only once
        viewModel.navigateToMap.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                findNavController().navigate(R.id.action_detailFragment_to_mapFragment)
            }
        })
    }


    private fun setTextViews(selectedPlant: Plant) {


        // image
        val imageView: ImageView = binding.mainPhotoImage
        bindImage(imageView,selectedPlant.imageUrl)

        // plant name
        val textViewName: TextView = binding.title
        textViewName.text = selectedPlant.name

        // plant latin name
        val textViewLatinName: TextView = binding.plantName
        textViewLatinName.text = getString(R.string.latin_plant_name_insert,selectedPlant.latinName)


        // plant description
        val textViewDescription: TextView = binding.plantDescription
        textViewDescription.text = HtmlCompat.fromHtml(selectedPlant.description, FROM_HTML_MODE_LEGACY)
        Linkify.addLinks(textViewDescription, Linkify.WEB_URLS)
        textViewDescription.movementMethod = LinkMovementMethod.getInstance()


    }
}