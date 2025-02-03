package com.app.householdtracing.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.app.householdtracing.data.repositoryImpl.GeofencingRepository
import com.app.householdtracing.util.DateUtil

class SharedViewModel(
    private val geoFenceRepository: GeofencingRepository
) : ViewModel() {

    var currentDate by mutableStateOf(DateUtil.getCurrentDate())
    var search by mutableStateOf("")
}