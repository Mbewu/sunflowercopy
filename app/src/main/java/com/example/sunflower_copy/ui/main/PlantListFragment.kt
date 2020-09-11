package com.example.sunflower_copy.ui.main

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.*
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import com.example.sunflower_copy.SunflowerApplication
import com.example.sunflower_copy.databinding.FragmentPlantListBinding
import com.example.sunflower_copy.domain.PlantInformation2
import com.example.sunflower_copy.util.hideKeyboard
import com.example.sunflower_copy.util.setRecyclerViewSpan
import com.example.sunflower_copy.util.setupUI
import timber.log.Timber


/**
 * A my garden fragment containing a garden view
 */
class PlantListFragment : Fragment() {

    private lateinit var binding: FragmentPlantListBinding
//    private lateinit var pageViewModel: PageViewModel
//    private lateinit var pageViewModelFactory: PageViewModelFactory

    private val pageViewModel by activityViewModels<PageViewModel> {
        PageViewModelFactory(requireActivity().application,
            (requireContext().applicationContext as SunflowerApplication).plantRepository,
            (requireContext().applicationContext as SunflowerApplication).gardenRepository)
    }
    private lateinit var adapter: PlantGridSearchAdapter
    private lateinit var tracker: SelectionTracker<PlantInformation2>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Timber.i("plantlist before pageviewmodel0")
        val application = requireNotNull(activity).application
        Timber.i("plantlist before pageviewmodel")
        //pageViewModelFactory = PageViewModelFactory(application)
        Timber.i("plantlist before pageviewmodel")

//        pageViewModel = ViewModelProviders.of(this).get(PageViewModel::class.java).apply {
//            setIndex(arguments?.getInt(ARG_SECTION_NUMBER) ?: 1)
//        }
        // set pageviewmodel attached to the activity, same object forever
//        pageViewModel = ViewModelProvider(requireActivity()).get(PageViewModel::class.java).apply {
//            setIndex(arguments?.getInt(ARG_SECTION_NUMBER) ?: 1)
//        }

        pageViewModel.setIndex(arguments?.getInt(PlantListFragment.ARG_SECTION_NUMBER) ?: 1)

        Timber.i("plantlist after pageviewmodel1")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // inflate using binding to correct layout
        binding = FragmentPlantListBinding.inflate(inflater).apply {
            // set viewModel
            viewModel = pageViewModel
        }

        Timber.i("what are you doing?")
        //container?.let { activity?.let { it1 -> setupUI(it, it1) } }

        // set lifecyle owner for coroutines
        binding.lifecycleOwner = this

        Log.i("PlantListFragment", "hello1")
        setAdapter()

        setSelectionTracker()

        val recyclerView = binding.photosGridSearch
        context?.let { setRecyclerViewSpan(it,recyclerView) }

        setObservers()

        //setHasOptionsMenu(true)
        return binding.root
    }
//
//    fun onCreateOptionsMenu(menu: Menu): Boolean {
//        getMenuInflater().inflate(R.menu.menu_main, menu)
//
//        // Associate searchable configuration with the SearchView
//        val searchManager = getSystemService<Any>(Context.SEARCH_SERVICE) as SearchManager?
//        searchView = menu.findItem(R.id.action_search)
//            .getActionView() as SearchView
//        searchView.setSearchableInfo(
//            searchManager
//                .getSearchableInfo(getComponentName())
//        )
//        searchView.setMaxWidth(Int.MAX_VALUE)
//    }

    private fun setAdapter() {

//        // set adapter for recyclerView photosGrid
//        val adapter = PlantGridAdapter(PlantGridAdapter.OnClickListener {
//            pageViewModel.displayPlantDetails(it)
//        })

        Log.i("PlantListFragment", "hello2")
        // handled in selection
//        // set adapter for recyclerView photosGrid
//        adapter = PlantGridSearchAdapter(PlantGridSearchAdapter.OnClickListener {
//            pageViewModel.displayPlantDetails(it)
//        })
        adapter = PlantGridSearchAdapter(requireActivity())


        Log.i("PlantListFragment", "hello3")
        val searchView: SearchView = binding.plantListSearch
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                adapter.filter.filter(query)
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                Timber.i("hmmmmm")

                adapter.filter.filter(newText)
                //adapter.filter.filter(newText)
                return true
            }

        })

        // hide the keyboard when clicking elsewhere in this fragment
        val parentLayout = binding.constraintLayout
        activity?.let { setupUI(parentLayout, it) }


        Log.i("PlantListFragment", "hello4")


        binding.photosGridSearch.adapter = adapter

        Log.i("PlantListFragment", "hello5")
    }


    private fun setSelectionTracker() {

        val recyclerView = binding.photosGridSearch
        tracker = SelectionTracker.Builder<PlantInformation2>(
            "mySelection2",
            recyclerView,
            PlantKeyProvider(adapter),
            PlantDetailsLookup(recyclerView),
            StorageStrategy.createParcelableStorage(PlantInformation2::class.java))
            .withOnItemActivatedListener { item, e ->
                Log.e("MainActivity", item.toString())
                // to account for the header
                if(item.position != 0) {
                    binding.fabAddSelection.visibility = View.GONE
                    pageViewModel.displayPlantDetails(item.selectionKey!!)
                }
                return@withOnItemActivatedListener true}
            .withSelectionPredicate(
                SelectionPredicates.createSelectAnything())
            .build()

        //adapter.tracker = tracker
        adapter.selectionChecker = object : SelectionChecker {
            override fun isSelected(plant: PlantInformation2): Boolean =
                tracker.isSelected(plant)
        }


    }

    private fun setObservers() {

        // observer that resubmits the list for the recycleview
        pageViewModel.plants2.observe(viewLifecycleOwner, Observer {
            adapter.submitNewList(pageViewModel.plants2.value)
            Timber.i("hplantlist observer ")
            Timber.i("hmm plants2.size = ".plus(it.size.toString()))
        })
//
//        val textView: TextView = binding.sectionLabel
//        pageViewModel.text.observe(viewLifecycleOwner, Observer<String> {
//            textView.text = it.plus(" plantListFragment")
//        })


        // Observe the navigateToSelectedProperty LiveData and Navigate when it isn't null
        // After navigating, call displayPropertyDetailsComplete() so that the ViewModel is ready
        // for another navigation event.
        pageViewModel.navigateToSelectedPlant.observe(viewLifecycleOwner, Observer {
            if (null != it) {
                Log.i("PlantListFragment", "hello1")
                //hide keyboard
                activity?.let { it1 -> hideKeyboard(it1) }

                // Must find the NavController from the Fragment
                this.findNavController().navigate(
                    ViewPagerFragmentDirections.actionViewPagerFragmentToDetailFragment(
                        it
                    )
                )
                // Tell the ViewModel we've made the navigate call to prevent multiple navigation

                Log.i("PlantListFragment", "2")
                pageViewModel.displayPlantDetailsComplete()
                Log.i("PlantListFragment", "hello3")
            }
        })


        tracker.addObserver(
            object : SelectionTracker.SelectionObserver<PlantInformation2>() {
                override fun onSelectionChanged() {
                    super.onSelectionChanged()
                    val items = tracker.selection.size()
                    if (items > 0) {
                        Timber.i("You selected at least one item")
                        // show the fab
                        binding.fabAddSelection.visibility = View.VISIBLE
                    } else {
                        // hide the fab
                        binding.fabAddSelection.visibility = View.GONE
                    }
                }
            })

        // click handler to remove the plants or rather bring up a dialog

        // observer for floating action button to plant
        binding.fabAddSelection.setOnClickListener {

            val plantsToAdd = tracker.selection.toList()
            val addSelectedPlantsDialogFragment: DialogFragment = AddSelectedPlantsDialogFragment(plantsToAdd)
            val fm: FragmentManager = requireActivity().supportFragmentManager

            addSelectedPlantsDialogFragment.show(fm, "remove_plant_dialog")

            //clear the selection
            //viewModel.removePlantFromGarden()
        }

        // observer for when plants deleted
        // observer for when user has removed a plant
        // displays a toast and navigates up
        pageViewModel.plantsAdded.observe(viewLifecycleOwner, Observer { plantsAdded ->
            Timber.i("hello1")
            if(plantsAdded != null) {

                //val application = requireNotNull(activity).application

                Toast.makeText(activity,
                    plantsAdded.toString().plus(" plants added to your garden."),
                    Toast.LENGTH_SHORT).show()

                Timber.i("plants added = ".plus(plantsAdded))

                pageViewModel.plantAddedComplete()
                Timber.i("hello3")

                // hide the fab
                binding.fabAddSelection.visibility = View.GONE
                // clear the selection
                tracker.clearSelection()
            }

            Timber.i("hello plantedRemoved after = ".plus(plantsAdded))

        })

    }

    companion object {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private const val ARG_SECTION_NUMBER = "section_number"

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        @JvmStatic
        fun newInstance(sectionNumber: Int): PlantListFragment {
            return PlantListFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_SECTION_NUMBER, sectionNumber)
                }
            }
        }
    }
}