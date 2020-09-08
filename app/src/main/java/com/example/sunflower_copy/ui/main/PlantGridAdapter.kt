package com.example.sunflower_copy.ui.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.sunflower_copy.databinding.PlantListItemBinding
import com.example.sunflower_copy.domain.PlantInformation
import com.example.sunflower_copy.domain.PlantInformation2


/**
 * This class implements a [RecyclerView] [ListAdapter] which uses Data Binding to present [List]
 * data, including computing diffs between lists.
 */
class PlantGridAdapter( private val onClickListener: OnClickListener ) :
    ListAdapter<PlantInformation2,
            PlantGridAdapter.PlantInformationViewHolder>(DiffCallback) {

    /**
     * The PlantInformationViewHolder constructor takes the binding variable from the associated
     * GridViewItem, which nicely gives it access to the full [PlantInformation] information.
     */
    class PlantInformationViewHolder(private var binding: PlantListItemBinding):
        RecyclerView.ViewHolder(binding.root) {

        fun bind(plantInformation2: PlantInformation2) {
            //binding.plant = plantInformation2
            binding.viewModel = PlantAndGardenPlantingsViewModel(plantInformation2)
            // This is important, because it forces the data binding to execute immediately,
            // which allows the RecyclerView to make the correct view size measurements
            binding.executePendingBindings()
        }

    }

    /**
     * Allows the RecyclerView to determine which items have changed when the [List] of [PlantInformation]
     * has been updated.
     */
    companion object DiffCallback : DiffUtil.ItemCallback<PlantInformation2>() {
        override fun areItemsTheSame(oldItem: PlantInformation2, newItem: PlantInformation2): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: PlantInformation2, newItem: PlantInformation2): Boolean {
            return oldItem.id == newItem.id
        }
    }

    /**
     * Create new [RecyclerView] item views (invoked by the layout manager)
     */
    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): PlantInformationViewHolder {
        return PlantInformationViewHolder(PlantListItemBinding.inflate(LayoutInflater.from(parent.context)))
    }

    /**
     * Replaces the contents of a view (invoked by the layout manager)
     */
    override fun onBindViewHolder(holder: PlantInformationViewHolder, position: Int) {
        val plantInformation2 = getItem(position)
        holder.itemView.setOnClickListener {
            onClickListener.onClick(plantInformation2)
        }
        holder.bind(plantInformation2)
    }

    /**
     * Custom listener that handles clicks on [RecyclerView] items.  Passes the [PlantInformation]
     * associated with the current item to the [onClick] function.
     * @param clickListener lambda that will be called with the current [PlantInformation]
     */
    class OnClickListener(val clickListener: (plantInformation2:PlantInformation2) -> Unit) {
        fun onClick(plantInformation2:PlantInformation2) = clickListener(plantInformation2)
    }

}
