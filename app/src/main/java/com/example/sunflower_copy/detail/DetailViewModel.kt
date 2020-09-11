package com.example.sunflower_copy.detail

import android.app.Application
import androidx.lifecycle.*
import com.example.sunflower_copy.domain.Plant

/**
 *  The [ViewModel] associated with the [DetailFragment], containing information about the selected
 *  [Plant].
 */
class DetailViewModel(plantInformation: Plant,
                      app: Application
) : AndroidViewModel(app) {

    private var _selectedPlant = MutableLiveData<Plant>()

    val selectedPlant: LiveData<Plant>
        get() = _selectedPlant
    init {
        _selectedPlant.value = plantInformation
    }



}

