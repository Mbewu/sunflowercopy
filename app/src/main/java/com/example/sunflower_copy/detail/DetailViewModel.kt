package com.example.sunflower_copy.detail

import android.app.Application
import androidx.lifecycle.*
import com.example.sunflower_copy.domain.PlantInformation
import com.example.sunflower_copy.domain.PlantInformation2

/**
 *  The [ViewModel] associated with the [DetailFragment], containing information about the selected
 *  [PlantInformation].
 */
class DetailViewModel(plantInformation: PlantInformation2,
                      app: Application
) : AndroidViewModel(app) {

    private var _selectedPlant = MutableLiveData<PlantInformation2>()

    val selectedPlant: LiveData<PlantInformation2>
        get() = _selectedPlant
    init {
        _selectedPlant.value = plantInformation
    }



}

