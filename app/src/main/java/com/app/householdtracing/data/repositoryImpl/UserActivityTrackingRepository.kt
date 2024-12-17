package com.app.householdtracing.data.repositoryImpl

import com.app.householdtracing.data.model.ActivityInfo
import com.app.householdtracing.data.model.GeofenceEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asStateFlow

class UserActivityTrackingRepository {

    //User Recognition
    private val _recognitionSharedFlow = MutableStateFlow(ActivityInfo(-1, -1, ""))
    val recognitionSharedFlow = _recognitionSharedFlow.asStateFlow()

    suspend fun postRecognition(recognitionInfo: ActivityInfo) {
        _recognitionSharedFlow.emit(recognitionInfo)
    }

    // User Geofence Transition
    private val _geofenceEvents = MutableSharedFlow<GeofenceEvent>()
    val geofenceEvents: SharedFlow<GeofenceEvent> = _geofenceEvents

    suspend fun postGeofenceEvent(event: GeofenceEvent) {
        _geofenceEvents.emit(event)
    }
}