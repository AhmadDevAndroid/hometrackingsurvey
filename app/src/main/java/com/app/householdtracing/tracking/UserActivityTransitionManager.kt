package com.app.householdtracing.tracking

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.app.householdtracing.App.Companion.APP_TAG
import com.app.householdtracing.data.model.ActivityInfo
import com.app.householdtracing.receiver.ActivityRecognitionReceiver
import com.app.householdtracing.util.AppUtil.showLogError
import com.app.householdtracing.util.PermissionUtil
import com.google.android.gms.location.ActivityRecognition
import com.google.android.gms.location.DetectedActivity

class UserActivityTransitionManager(private val context: Context) {

    private val activityClient = ActivityRecognition.getClient(context)
    private var currentUserActivityType: Int = DetectedActivity.STILL
    private var actionCount: Int = 0
    private fun isConfidenceSatisfied(confidence: Int) = confidence > 70
    private fun isConsecutiveCountMet() = actionCount > 3
    private fun isCurrentActivity(type: Int) = type == currentUserActivityType
    private var isUserInStillStateInitially: Boolean = false

    companion object {

        const val KEY_DATA = "DATA"
        const val INTENT_FILTER_SHOWN = "RECOGNITION"
        const val DETECTION_INTERVAL_IN_MILLISECONDS = (45 * 1000).toLong()
    }

    fun observeUserActivity(activityInfo: ActivityInfo) {
        if (isConfidenceSatisfied(activityInfo.confidenct)) {
            if (activityInfo.type == DetectedActivity.STILL) {
                if (!isUserInStillStateInitially) {
                    isUserInStillStateInitially = true
                }
            } else {
                if (isUserInStillStateInitially) {
                    isUserInStillStateInitially = false
                }
                updateActionCount(activityInfo.type)
                evaluateActivityState()
            }
        }
    }

    private fun updateActionCount(detectedType: Int) {
        actionCount = if (detectedType == currentUserActivityType) actionCount + 1 else 0
    }

    private fun evaluateActivityState() {
        if (isConsecutiveCountMet()) {
            when (currentUserActivityType) {
                DetectedActivity.IN_VEHICLE -> switchToStill()
                DetectedActivity.STILL -> switchToVehicle()
                else -> Unit
            }
        }
    }

    fun switchToStill() {
        resetActionCount()
        currentUserActivityType = DetectedActivity.STILL
        isUserInStillStateInitially = false
    }

    fun switchToVehicle() {
        resetActionCount()
        currentUserActivityType = DetectedActivity.IN_VEHICLE
    }

    private fun resetActionCount() {
        actionCount = 0
    }

    fun isInVehicle(): Boolean =
        isConsecutiveCountMet() && isCurrentActivity(DetectedActivity.IN_VEHICLE)

    fun isStill(): Boolean = isConsecutiveCountMet() && isCurrentActivity(DetectedActivity.STILL)

    fun getActivityMessage(): String = when (currentUserActivityType) {

        DetectedActivity.IN_VEHICLE -> if (isInVehicle()) {
            "User is in Vehicle Now"
        } else {
            "Detecting User is in Vehicle"
        }

        DetectedActivity.STILL -> if (isStill()) {
            "User is Still Now"
        } else {
            "Detecting User is Still"
        }

        else -> "Current user None activity"
    }

    fun getActivityType(int: Int): String {
        return when (int) {
            DetectedActivity.IN_VEHICLE -> "User is IN_VEHICLE"
            DetectedActivity.ON_BICYCLE -> "User is ON_BICYCLE"
            DetectedActivity.ON_FOOT -> "User is ON_FOOT"
            DetectedActivity.STILL -> "User is STILL"
            DetectedActivity.UNKNOWN -> "User is UNKNOWN"
            DetectedActivity.TILTING -> "User is TILTING"
            DetectedActivity.WALKING -> "User is WALKING"
            DetectedActivity.RUNNING -> "User is RUNNING"
            else -> "UNKNOWN"
        }
    }

    fun getConfidenceText(confidence: Int) = "confidence level: $confidence"

    private val pendingIntentFlag by lazy {
        return@lazy when {
            Build.VERSION.SDK_INT < Build.VERSION_CODES.S -> PendingIntent.FLAG_UPDATE_CURRENT
            else -> PendingIntent.FLAG_MUTABLE
        }
    }

    private val receiverIntent by lazy {
        Intent(context, ActivityRecognitionReceiver::class.java)
    }

    private val pendingIntentInstance by lazy {
        PendingIntent.getBroadcast(context, 0, receiverIntent, pendingIntentFlag)
    }


    @SuppressLint("InlinedApi", "MissingPermission")
    fun registerActivityTransitions() {
        if (isPermissionGiven()) {

            activityClient.requestActivityUpdates(
                DETECTION_INTERVAL_IN_MILLISECONDS,
                pendingIntentInstance
            ).addOnSuccessListener {
                showLogError(
                    APP_TAG,
                    "Successfully requested activity updates"
                )
            }.addOnFailureListener {
                showLogError(
                    APP_TAG,
                    "Failed to request activity updates -> ${it.message} ?: N/A"
                )
            }
        }
    }

    private fun isPermissionGiven() =
        PermissionUtil.isActivityRecognitionPermissionGranted(context = context)

    @SuppressLint("MissingPermission")
    fun deregisterActivityTransitions() {
        if (isPermissionGiven()) {
            activityClient.removeActivityUpdates(pendingIntentInstance)
        }
    }


}