package com.app.householdtracing.tracking

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.app.householdtracing.App.Companion.APP_TAG
import com.app.householdtracing.receiver.ActivityRecognitionReceiver
import com.app.householdtracing.util.AppUtil.showLogError
import com.app.householdtracing.util.PermissionUtil
import com.google.android.gms.location.ActivityRecognition
import com.google.android.gms.location.DetectedActivity

class UserActivityTransitionManager(private val context: Context) {

    companion object {

        const val KEY_DATA = "DATA"
        const val INTENT_FILTER_SHOWN = "RECOGNITION"
        const val DETECTION_INTERVAL_IN_MILLISECONDS = (45 * 1000).toLong()

    }


    private val activityClient = ActivityRecognition.getClient(context)

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