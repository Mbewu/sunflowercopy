package com.example.sunflower_copy.ui.main

import android.content.Context
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.ItemKeyProvider
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.sunflower_copy.R
import com.example.sunflower_copy.databinding.FragmentHeaderMyGardenBinding
import com.example.sunflower_copy.databinding.PlantListItemBinding
import com.example.sunflower_copy.databinding.PlantListItemConciseBinding
import com.example.sunflower_copy.domain.Plant
import com.example.sunflower_copy.util.numPlantsGrowing
import com.example.sunflower_copy.util.numPlantsToHarvest
import com.example.sunflower_copy.util.numPlantsToWater
import timber.log.Timber
import java.util.*
import kotlin.reflect.jvm.internal.impl.builtins.jvm.JavaToKotlinClassMap


/**
 * This class implements a [RecyclerView] [ListAdapter] which uses Data Binding to present [List]
 * data, including computing diffs between lists.
 */
class PlantGridSearchAdapter(private val context: Context) :
    ListAdapter<Plant, RecyclerView.ViewHolder>(DiffCallback), Filterable {

    var selectionChecker: SelectionChecker? = null
    private val TYPE_HEADER = 0
    private val TYPE_ITEM = 1

    /**
     * The PlantInformationViewHolder constructor takes the binding variable from the associated
     * GridViewItem, which nicely gives it access to the full [Plant] information.
     */
    class ViewHolder private  constructor(private var binding: PlantListItemBinding):
        RecyclerView.ViewHolder(binding.root) {

        fun bind(plant: Plant, position: Int, isActivated: Boolean = false) {
            //binding.plant = Plant

            // stuff for selection
            Timber.i("hi i'm here! so there")
            binding.position = position
            binding.plant = plant
            binding.frameLayout.isActivated = isActivated



            binding.viewModel = PlantAndGardenPlantingsViewModel(plant)
            // This is important, because it forces the data binding to execute immediately,
            // which allows the RecyclerView to make the correct view size measurements
            binding.executePendingBindings()
        }

        fun getItemDetails(): PlantDetails = PlantDetails(
            position = binding.position,
            plant = binding.plant
        )

        companion object {
            fun from(parent: ViewGroup): RecyclerView.ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = PlantListItemBinding.inflate(layoutInflater)
                return ViewHolder(binding)
            }
        }


    }


    /**
     * The PlantInformationViewHolder constructor takes the binding variable from the associated
     * GridViewItem, which nicely gives it access to the full [Plant] information.
     */
    class ViewHolderConcise private  constructor(private var binding: PlantListItemConciseBinding):
        RecyclerView.ViewHolder(binding.root) {

        fun bind(plant: Plant, position: Int, isActivated: Boolean = false) {
            //binding.plant = Plant

            // stuff for selection
            Timber.i("hi i'm here! so there")
            binding.position = position
            binding.plant = plant
            binding.frameLayout.isActivated = isActivated



            binding.viewModel = PlantAndGardenPlantingsViewModel(plant)
            // This is important, because it forces the data binding to execute immediately,
            // which allows the RecyclerView to make the correct view size measurements
            binding.executePendingBindings()
        }

        fun getItemDetails(): PlantDetails = PlantDetails(
            position = binding.position,
            plant = binding.plant
        )

        companion object {
            fun from(parent: ViewGroup): RecyclerView.ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = PlantListItemConciseBinding.inflate(layoutInflater)
                return ViewHolderConcise(binding)
            }
        }


    }

    class HeaderViewHolder(private var binding: FragmentHeaderMyGardenBinding): RecyclerView.ViewHolder(binding.root) {

        fun bind(numPlantsTotal: Int = 0, numPlantsGrowing: Int = 0,
                 numPlantsToWater: Int = 0, numPlantsToHarvest: Int = 0) {
            //binding.plant = Plant

            binding.totalPlantsNumber.text = numPlantsTotal.toString()
            binding.growingPlantsNumber.text = numPlantsGrowing.toString()
            binding.waterPlantsNumber.text = numPlantsToWater.toString()
            binding.harvestPlantsNumber.text = numPlantsToHarvest.toString()
            // stuff for selection
            Timber.i("hi i'm here! so there")
            // This is important, because it forces the data binding to execute immediately,
            // which allows the RecyclerView to make the correct view size measurements
            //binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): HeaderViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
//                val view = layoutInflater.inflate(R.layout.fragment_header_my_garden, parent, false)
//                val headerText = view.findViewById<TextView>(R.id.section_label)
//                headerText.text = numPlants.toString().plus(" plants are in your list.")
//                return HeaderViewHolder(view)
                val binding = FragmentHeaderMyGardenBinding.inflate(layoutInflater)
                return HeaderViewHolder(binding)
            }
        }
    }


    override fun getItemViewType(position: Int): Int {
        return if (position == 0) {
            TYPE_HEADER
        } else {
            TYPE_ITEM
        }
    }



    /**
     * Allows the RecyclerView to determine which items have changed when the [List] of [Plant]
     * has been updated.
     */
    companion object DiffCallback : DiffUtil.ItemCallback<Plant>() {
        override fun areItemsTheSame(oldItem: Plant, newItem: Plant): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: Plant, newItem: Plant): Boolean {
            return oldItem.id == newItem.id
        }
    }
    /**
     * Create new [RecyclerView] item views (invoked by the layout manager)
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        Timber.i("hi i'm doing this crap again")
        // get the sharedPreferences
        return when (viewType) {
            TYPE_HEADER -> {
                HeaderViewHolder.from(parent)
            }
            TYPE_ITEM -> ViewHolder.from(parent)
            else -> throw ClassCastException("Unknown viewType ${viewType}")
        }
    }

    /**
     * Replaces the contents of a view (invoked by the layout manager)
     * changed from having a return value
     */
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            // only this one to bind
            is ViewHolder -> {
                Timber.i("plant view hello pos = ".plus(position))
                val selectedPlant = getItem(position)
                // not sure about this clicklistener
                //holder.itemView.setOnClickListener { onClickListener.onClick(selectedPlant) }
                holder.bind(
                    selectedPlant,
                    position,
                    selectionChecker?.isSelected(selectedPlant) ?: false
                )
            }
            // only this one to bind
            is ViewHolderConcise -> {
                Timber.i("plant view hello pos = ".plus(position))
                val selectedPlant = getItem(position)
                // not sure about this clicklistener
                //holder.itemView.setOnClickListener { onClickListener.onClick(selectedPlant) }
                holder.bind(
                    selectedPlant,
                    position,
                    selectionChecker?.isSelected(selectedPlant) ?: false
                )
            }
            is HeaderViewHolder -> {
                Timber.i("header view hello pos = ".plus(position))
                //selectionChecker?.isSelected(selectedPlant)
                // remove the clicklistener
                //holder.itemView.setOnClickListener(null)
                plantList?.let {
                    // want to use the list without the header
                    val lastIndex = it.lastIndex
                    val subList = it.subList(1,lastIndex+1)
                    val numPlantsTotal = subList.size
                    val numPlantsGrowing = numPlantsGrowing(subList)
                    val numPlantsToWater = numPlantsToWater(subList)
                    val numPlantsToHarvest = numPlantsToHarvest(subList)
                    holder.bind(
                        numPlantsTotal,
                        numPlantsGrowing,
                        numPlantsToWater,
                        numPlantsToHarvest
                    )
                } ?:  holder.bind()
            }
        }
    }



    // get the list we're working with in the filter
    var plantList: List<Plant>? = null
    var plantFilterList: List<Plant>? = null


    // this is used when selecting and sorting i feel
    override fun submitList(list: List<Plant>?) {

        // add an extra blank plantinformation entry for the header
        // a bit hacky, but we just let it have an id -1
        val tempList: MutableList<Plant>? = list?.toMutableList()
        val headerPlant: Plant = Plant()
        if (list != null) {
            if (list.isNotEmpty()) {
                if(list[0].id != -1) {
                    headerPlant.id = -1
                    tempList?.add(0, headerPlant)
                }
            } else {
                headerPlant.id = -1
                tempList?.add(0, headerPlant)
            }
        }

        if (plantList == null) {
            plantList = tempList
        }

        if (tempList != null) {
            Timber.i("submitting list with size = ".plus(tempList.size))
        }
        super.submitList(tempList)
        //notifyDataSetChanged()
    }

    fun submitNewList(list: List<Plant>?) {

        // add an extra blank Plant entry for the header
        // a bit hacky, but we just let it have an id -1
        val tempList: MutableList<Plant>? = list?.toMutableList()
        val headerPlant: Plant = Plant()
        if (list != null) {
            if (list.isNotEmpty()) {
                if(list[0].id != -1) {
                    headerPlant.id = -1
                    tempList?.add(0, headerPlant)
                }
            } else {
                headerPlant.id = -1
                tempList?.add(0, headerPlant)
            }
        }

        // if it's a new list update this
        plantList = tempList

        if (tempList != null) {
            Timber.i("submitting list with size = ".plus(tempList.size))
        }
        super.submitList(tempList)
        notifyDataSetChanged()
    }



    // might need to handle the header item here
    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearch = constraint.toString()
                if (charSearch.isEmpty()) {
                    plantFilterList = plantList
                } else {
                    plantList?.let {
                        val resultList = arrayListOf<Plant>()
                        for (plant in plantList!!) {
                            if (plant.name.toLowerCase(Locale.ROOT)
                                    .contains(charSearch.toLowerCase(Locale.ROOT))
                            ) {
                                resultList.add(plant)
                            }
                        }
                        plantFilterList = resultList
                    }
                }

                Timber.i("listsize in filter = ".plus(currentList.size))
                val filterResults = FilterResults()
                filterResults.values = plantFilterList
                return filterResults
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                if(plantFilterList != null) {
                    submitList(results?.values as ArrayList<Plant>)
                }
                else {
                    submitList(plantList as ArrayList<Plant>)
                }
                notifyDataSetChanged()
            }
        }
    }

    fun getPlantItem(position: Int): Plant = currentList[position]

    fun getPosition(plant: Plant): Int = currentList.indexOf(plant)
}

// for selection
// this basically does the click functionality
class PlantDetailsLookup(private val recyclerView: RecyclerView) :
    ItemDetailsLookup<Plant>() {

    object EMPTY_ITEM: ItemDetails<Plant>() {
        override fun getPosition(): Int {
            return -1
        }

        override fun getSelectionKey(): Plant? {
            return Plant()
        }
    }

    override fun getItemDetails(event: MotionEvent): ItemDetails<Plant>? {
        Timber.e("okay we are here")
        val view = recyclerView.findChildViewUnder(event.x, event.y)
        if (view != null) {
            // check that it's not the header
            if(recyclerView.getChildViewHolder(view) is PlantGridSearchAdapter.ViewHolder) {
                return (recyclerView.getChildViewHolder(view) as PlantGridSearchAdapter.ViewHolder)
                    .getItemDetails()
            }
            // check that it's not the header
            if(recyclerView.getChildViewHolder(view) is PlantGridSearchAdapter.ViewHolderConcise) {
                return (recyclerView.getChildViewHolder(view) as PlantGridSearchAdapter.ViewHolderConcise)
                    .getItemDetails()
            }
        }

        // return an empty item so that clicks outside of the selection won't cancel
        // either the header or empty grid slots
        //return null
        Timber.i("returning empty item")
        return EMPTY_ITEM
    }

}

// for selection
data class PlantDetails(
    private val position: Int,
    private val plant: Plant?
) : ItemDetailsLookup.ItemDetails<Plant>() {

    override fun getPosition(): Int = position

    override fun getSelectionKey(): Plant? = plant
}

// for selection
class PlantKeyProvider(
    private val adapter: PlantGridSearchAdapter
) : ItemKeyProvider<Plant>(ItemKeyProvider.SCOPE_CACHED) {
    override fun getKey(position: Int): Plant? = adapter.getPlantItem(position)

    override fun getPosition(key: Plant): Int = adapter.getPosition(key)
}

// for selection
interface SelectionChecker {

    fun isSelected(plant: Plant): Boolean

}