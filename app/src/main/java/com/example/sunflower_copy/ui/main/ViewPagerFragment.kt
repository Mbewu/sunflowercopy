package com.example.sunflower_copy.ui.main

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.*
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.example.sunflower_copy.R
import com.example.sunflower_copy.SunflowerApplication
import com.example.sunflower_copy.databinding.FragmentViewPagerBinding
import com.example.sunflower_copy.util.hideKeyboard
import com.example.sunflower_copy.util.setupUI
import com.google.android.material.tabs.TabLayoutMediator



class ViewPagerFragment : Fragment() {

    //private lateinit var viewModel: PageViewModel

    // moved inside, not sure why it was outside
    private val viewModel by activityViewModels<PageViewModel> {
        PageViewModelFactory(requireActivity().application,
            (requireContext().applicationContext as SunflowerApplication).plantRepository,
            (requireContext().applicationContext as SunflowerApplication).gardenRepository)
    }
    //private lateinit var viewModelFactory: PageViewModelFactory

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // inflate using binding to correct layout
        val binding = FragmentViewPagerBinding.inflate(inflater)
        val tabLayout = binding.tabs
        val viewPager = binding.viewPager

        viewPager.adapter = SectionsPagerAdapter(this)

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.setIcon(getTabIcon(position))
            tab.text = getTabTitle(position)
        }.attach()

        // get the view pager to remove all the plants from the garden
        val application = requireNotNull(activity).application
        //viewModelFactory = PageViewModelFactory(application)
        //viewModel = ViewModelProvider(requireActivity(), viewModelFactory).get(PageViewModel::class.java)

        setObservers()

        // hide the keyboard when clicking elsewhere in this fragment
        val parentLayout = binding.drawerLayout
        activity?.let { setupUI(parentLayout, it) }

        setHasOptionsMenu(true)
        return binding.root
    }

    private fun getTabIcon(position: Int): Int {
        return when (position) {
            MY_GARDEN_PAGE_INDEX -> R.drawable.my_garden_tab_selector
            PLANT_LIST_PAGE_INDEX -> R.drawable.plant_list_tab_selector
            else -> throw IndexOutOfBoundsException()
        }
    }

    private fun getTabTitle(position: Int): String? {
        return when (position) {
            MY_GARDEN_PAGE_INDEX -> getString(R.string.tab_text_1)
            PLANT_LIST_PAGE_INDEX -> getString(R.string.tab_text_2)
            else -> null
        }
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.options_menu, menu)

    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.aboutFragment -> {
                activity?.let { it1 -> hideKeyboard(it1) };
                NavigationUI.onNavDestinationSelected(
                item,requireView().findNavController()
            )}
            R.id.mapFragment -> {
                activity?.let { it1 -> hideKeyboard(it1) };
                NavigationUI.onNavDestinationSelected(
                    item,requireView().findNavController()
                )
            }
            R.id.clear_garden -> {
                activity?.let { it1 -> hideKeyboard(it1) };
                clearGarden()
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun clearGarden(): Boolean {
        // clear the garden and then output a toast
        //viewModel.clearGarden()

        val clearGardenDialogFragment: DialogFragment = ClearGardenDialogFragment()
        val fm: FragmentManager = requireActivity().supportFragmentManager

        clearGardenDialogFragment.show(fm, "remove_plant_dialog")


        return true
    }

    private fun setObservers() {


        viewModel.gardenCleared.observe(viewLifecycleOwner, Observer { it ->
            if (it) {
                Toast.makeText(
                    activity, "Garden has been cleared.",
                    Toast.LENGTH_SHORT
                ).show()
                viewModel.gardenClearingComplete()
            }
        })
    }

}
