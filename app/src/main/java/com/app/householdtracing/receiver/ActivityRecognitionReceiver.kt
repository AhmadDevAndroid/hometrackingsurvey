package com.app.householdtracing.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.app.householdtracing.App.Companion.APP_TAG
import com.app.householdtracing.data.model.ActivityInfo
import com.app.householdtracing.data.repositoryImpl.UserActivityTrackingRepository
import com.app.householdtracing.tracking.UserActivityTransitionManager
import com.app.householdtracing.util.AppUtil.showLogError
import com.app.householdtracing.util.DateUtil
import com.google.android.gms.location.ActivityRecognitionResult
import com.google.android.gms.location.DetectedActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent.getKoin

class ActivityRecognitionReceiver : BroadcastReceiver() {

    private val userActivityRepository: UserActivityTrackingRepository by lazy { getKoin().get() }
    private val scope = CoroutineScope(Dispatchers.IO)
    private val userActivityTransitionManager: UserActivityTransitionManager by lazy { getKoin().get() }

    override fun onReceive(context: Context?, intent: Intent?) {
        val result = intent?.let { ActivityRecognitionResult.extractResult(it) }
        val activity = result?.mostProbableActivity

        if (activity == null || activity.type == DetectedActivity.UNKNOWN || activity.confidence == -1) {
            showLogError("$APP_TAG ActivityReceiver", "No valid activity detected.")
            return
        }

        val activityInfo = ActivityInfo(
            confidenct = activity.confidence,
            type = activity.type,
            time = DateUtil.getHourAndMinute()
        )

        scope.launch {
            userActivityRepository.postRecognition(activityInfo)
        }

        logActivityType(activity.type)
    }

    private fun logActivityType(type: Int) {
        val activityType = userActivityTransitionManager.getActivityType(type)
        showLogError("$APP_TAG ActivityReceiver", activityType)
    }
}