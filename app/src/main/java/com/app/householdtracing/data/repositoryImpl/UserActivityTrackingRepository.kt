package com.app.householdtracing.data.repositoryImpl

import com.app.householdtracing.data.model.ActivityInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class UserActivityTrackingRepository {
    private val _recognitionSharedFlow = MutableStateFlow(ActivityInfo(-1, -1, ""))
    val recognitionSharedFlow = _recognitionSharedFlow.asStateFlow()

    suspend fun postRecognition(recognitionInfo: ActivityInfo) {
        _recognitionSharedFlow.emit(recognitionInfo)
    }
}