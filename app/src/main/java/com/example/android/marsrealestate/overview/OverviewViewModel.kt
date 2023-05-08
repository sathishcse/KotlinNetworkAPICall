/*
 * Copyright 2018, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.example.android.marsrealestate.overview

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.android.marsrealestate.network.MarsApi
import com.example.android.marsrealestate.network.MarsApiFilter
import com.example.android.marsrealestate.network.MarsProperty
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

enum class MarsApiStatus {
    LOADING, ERROR, DONE
}

/**
 * The [ViewModel] that is attached to the [OverviewFragment].
 */
class OverviewViewModel : ViewModel() {

    // The internal MutableLiveData String that stores the status of the most recent request
    private val _status = MutableLiveData<MarsApiStatus>()

    // The external immutable LiveData for the request status String
    val status: LiveData<MarsApiStatus>
        get() = _status

    // retrieve single value
    private val _property = MutableLiveData<List<MarsProperty>>()

    val property: LiveData<List<MarsProperty>>
        get() = _property

    // navigate to details screen
    private val _navigateToSelectedProperty = MutableLiveData<MarsProperty>()

    val navigateToSelectedProperty: LiveData<MarsProperty>
        get() = _navigateToSelectedProperty

    //Job
    private var viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    /**
     * Call getMarsRealEstateProperties() on init so we can display status immediately.
     */
    init {
        getMarsRealEstateProperties(MarsApiFilter.SHOW_ALL)
    }


    /**
     * Sets the value of the status LiveData to the Mars API status.
     */
    private fun getMarsRealEstateProperties(filter: MarsApiFilter) {

        coroutineScope.launch {
            var getPropertyDeffer = MarsApi.retrofitService.getProperties(filter.type)
            try {
                _status.value = MarsApiStatus.LOADING
                val listResult = getPropertyDeffer.await()
                _status.value = MarsApiStatus.DONE
                if (listResult.isNotEmpty()) {
                    _property.value = listResult
                }
            } catch (e: java.lang.Exception) {
                _status.value = MarsApiStatus.ERROR
                _property.value = ArrayList()
            }

        }
        // TODO (05) Call the MarsApi to enqueue the Retrofit request, implementing the callbacks
        /* MarsApi.retrofitService.getProperties().enqueue(object : Callback<List<MarsProperty>> {
             override fun onResponse(call: Call<List<MarsProperty>>, response: Response<List<MarsProperty>>) {
                 _response.value = "Success ${response.body()?.size} Mars Properties Received"
             }

             override fun onFailure(call: Call<List<MarsProperty>>, t: Throwable) {
                 _response.value = t.message
             }

         })*/

    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    fun displayPropertyDetails(marsProperty: MarsProperty) {
        _navigateToSelectedProperty.value = marsProperty
    }

    fun displayPropertyDetailsDone() {
        _navigateToSelectedProperty.value = null
    }

    class OnClickListener(val clickListener: (marsProperty: MarsProperty) -> Unit) {
        fun onClick(marsProperty: MarsProperty) = clickListener(marsProperty)
    }

    fun updateFilter(filter: MarsApiFilter) {
        getMarsRealEstateProperties(filter)
    }
}
