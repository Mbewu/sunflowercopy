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
import androidx.recyclerview.selection.*
import com.example.sunflower_copy.SunflowerApplication
import com.example.sunflower_copy.databinding.FragmentMyGardenBinding
import com.example.sunflower_copy.domain.Plant
import com.example.sunflower_copy.util.hideKeyboard
import com.example.sunflower_copy.util.setRecyclerViewSpan
import com.example.sunflower_copy.util.setupUI
import timber.log.Timber


/**
 * A my garden fragment containing a garden view
 */
class MyGardenFragment : Fragment() {

    private lateinit var binding: FragmentMyGardenBinding
//    private lateinit var pageViewModel: PageViewModel
//    private lateinit var pageViewModelFactory: PageViewModelFactory


    private val pageViewModel by activityViewModels<PageViewModel>() {
        PageViewModelFactory(requireActivity().application,
            (requireContext().applicationContext as SunflowerApplication).plantRepository,
            (requireContext().applicationContext as SunflowerApplication).gardenRepository)
    }

    private lateinit var adapter: PlantGridSearchAdapter
    private lateinit var tracker: SelectionTracker<Plant>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Timber.i("plantlist before pageviewmodel0")
        Timber.i("plantlist before pageviewmodel")
        Timber.i("plantlist before pageviewmodel")


        Timber.i("plantlist after pageviewmodel1")

        Timber.i("mygarden before pageviewmodel")
        val application = requireNotNull(activity).application
        //pageViewModelFactory = PageViewModelFactory(application)
//        pageViewModel = ViewModelProviders.of(this).get(PageViewModel::class.java).apply {
//            setIndex(arguments?.getInt(ARG_SECTION_NUMBER) ?: 1)
//        }

        // the viewmodel attached to the activity
//        pageViewModel = ViewModelProvider(requireActivity()).get(PageViewModel::class.java).apply {
//            setIndex(arguments?.getInt(ARG_SECTION_NUMBER) ?: 1)
//        }
        // still need to set the index
        pageViewModel.setIndex(arguments?.getInt(ARG_SECTION_NUMBER) ?: 1)
        Timber.i("mygarden after pageviewmodel")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // inflate using binding to correct layout
        binding = FragmentMyGardenBinding.inflate(inflater).apply {
            // set viewModel
            viewModel = pageViewModel
        }


        //container?.let { activity?.let { it1 -> setupUI(it, it1) } }

        // set lifecyle owner for coroutines
        binding.lifecycleOwner = this


        setAdapter()

        setSelectionTracker()

        val recyclerView = binding.photosGrid
        context?.let { setRecyclerViewSpan(it,recyclerView) }

        setObservers()


        //setHasOptionsMenu(true)
        return binding.root
    }




    private fun setAdapter() {

        // handled in selection
//        // set adapter for recyclerView photosGrid
//        adapter = PlantGridSearchAdapter(PlantGridSearchAdapter.OnClickListener {
//            pageViewModel.displayGardenPlantDetails(it)
//        })
        adapter = PlantGridSearchAdapter(requireActivity())

        Timber.i("hey babe")

        val searchView: SearchView = binding.plantListSearch
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                Timber.i("ymmmmm")
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

        Timber.i("hey babe2")

        binding.photosGrid.adapter = adapter
    }




    private fun setSelectionTracker() {

        val recyclerView = binding.photosGrid
        tracker = SelectionTracker.Builder<Plant>(
            "mySelection",
            recyclerView,
            PlantKeyProvider(adapter),
            PlantDetailsLookup(recyclerView),
            StorageStrategy.createParcelableStorage(Plant::class.java))
//            .withOnItemActivatedListener { item, _ ->
//                Log.e("MainActivity", item.toString())
//                return@withOnItemActivatedListener true}
            .withOnItemActivatedListener { item, _ ->
                Timber.e("Hmmm")
                Timber.i("position = ${item.position}")
                // some issue with the header actually being at position -1 instead on 0
                if(item.position > 0) {
                    Timber.i("position = ${item.position}")
                    // make sure fab is hidden and then go to plant details
                    binding.fabDeleteSelection.visibility = View.GONE
                    pageViewModel.displayGardenPlantDetails(item.selectionKey!!)
                    return@withOnItemActivatedListener true
                }  else {
                    // don't do anything and allow for more clicks
                    //return@withOnItemActivatedListener false
                    false
                }}
                // makes sure you can select outside the items in the recycler view
            // e.g. header and empty grid slots
            .withSelectionPredicate(object : SelectionTracker.SelectionPredicate<Plant>() {
                override fun canSelectMultiple(): Boolean = true
                override fun canSetStateForKey(key: Plant, nextState: Boolean): Boolean =
                    key != PlantDetailsLookup.EMPTY_ITEM.selectionKey

                override fun canSetStateAtPosition(position: Int, nextState: Boolean): Boolean =
                    position != PlantDetailsLookup.EMPTY_ITEM.position
            })
//            .withSelectionPredicate(
//            SelectionPredicates.createSelectAnything())
            .build()

        //adapter.tracker = tracker
        adapter.selectionChecker = object : SelectionChecker {
            override fun isSelected(plant: Plant): Boolean =
                tracker.isSelected(plant)
        }


    }



    private fun setObservers() {

        // observer that resubmits the list for the recycleview
        pageViewModel.plantedPlants.observe(viewLifecycleOwner, Observer {
            Timber.i("planted plants observer")
            Timber.i("hmm plantedPlants.size = ${it.size}")
            adapter.submitNewList(pageViewModel.plantedPlants.value)
        })


        pageViewModel.plantNeedsWatering.observe(viewLifecycleOwner, Observer {
            //adapter.submitList(pageViewModel.plantedPlants.value)
            adapter.notifyDataSetChanged()
        })

        //val textView: TextView = binding.sectionLabel
        pageViewModel.plantedPlants.observe(viewLifecycleOwner, Observer {
            //textView.text = "${it.size} plants are in your garden";
        })


        // Observe the navigateToSelectedProperty LiveData and Navigate when it isn't null
        // After navigating, call displayPropertyDetailsComplete() so that the ViewModel is ready
        // for another navigation event.
        pageViewModel.navigateToSelectedGardenPlant.observe(viewLifecycleOwner, Observer {

            Timber.i(
                "navigate before is null = "
                    .plus(pageViewModel.navigateToSelectedGardenPlant.value == null)
            )
            if (null != it) {
                Timber.i("hello1")
                Timber.i("")
                // hide keyboard
                activity?.let { it1 -> hideKeyboard(it1) }

                pageViewModel.initPlantedView()
                // Must find the NavController from the Fragment
                this.findNavController().navigate(
                    ViewPagerFragmentDirections.actionViewPagerFragmentToPlantedFragment(
                        it
                    )
                )
                // Tell the ViewModel we've made the navigate call to prevent multiple navigation

                Timber.i("2")
                pageViewModel.displayGardenPlantDetailsComplete()
                Timber.i("hello3")
            }

            Timber.i(
                "navigate after is null = "
                    .plus(pageViewModel.navigateToSelectedGardenPlant.value == null)
            )
        })


        tracker.addObserver(
            object : SelectionTracker.SelectionObserver<Plant>() {
                override fun onSelectionChanged() {
                    super.onSelectionChanged()
                    val items = tracker.selection.size()
                    if (items > 0) {
                        Timber.i("You selected at least one item")
                        // show the fab
                        binding.fabDeleteSelection.visibility = View.VISIBLE
                    } else {
                        // hide the fab
                        binding.fabDeleteSelection.visibility = View.GONE
                    }
                }
            })

        // click handler to remove the plants or rather bring up a dialog

        // observer for floating action button to plant
        binding.fabDeleteSelection.setOnClickListener {

            val plantsToDelete = tracker.selection.toList()
            val removeSelectedPlantsDialogFragment: DialogFragment = RemoveSelectedPlantsDialogFragment(plantsToDelete)
            val fm: FragmentManager = requireActivity().supportFragmentManager

            removeSelectedPlantsDialogFragment.show(fm, "remove_plant_dialog")

            //clear the selection
            //viewModel.removePlantFromGarden()
        }

        // observer for when plants deleted
        // observer for when user has removed a plant
        // displays a toast and navigates up
        pageViewModel.plantsRemoved.observe(viewLifecycleOwner, Observer { plantsRemoved ->
            Timber.i("hello1")
            if(plantsRemoved != null) {

                //val application = requireNotNull(activity).application

                Toast.makeText(activity,
                    "$plantsRemoved plants removed from your garden.",
                    Toast.LENGTH_SHORT).show()

                Timber.i("plants removed = $plantsRemoved")

                pageViewModel.plantRemovalComplete()
                Timber.i("hello3")

                // hide the fab
                binding.fabDeleteSelection.visibility = View.GONE
                // clear the selection
                tracker.clearSelection()
            }

            Timber.i("hello plantedRemoved after = $plantsRemoved")

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
        fun newInstance(sectionNumber: Int): MyGardenFragment {
            return MyGardenFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_SECTION_NUMBER, sectionNumber)
                }
            }
        }
    }
}