package com.app.householdtracing.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.app.householdtracing.App.Companion.APP_TAG
import com.app.householdtracing.data.model.ActivityInfo
import com.app.householdtracing.util.AppUtil.showLogError
import com.app.householdtracing.util.DateUtil
import com.google.android.gms.location.ActivityRecognitionResult
import com.google.android.gms.location.DetectedActivity

class ActivityRecognitionReceiver : BroadcastReceiver() {


    override fun onReceive(context: Context?, intent: Intent?) {
        val result = ActivityRecognitionResult.extractResult(intent!!)
        val activity = result?.mostProbableActivity
        activity?.let {

            ActivityInfo(
                confidenct = it.confidence,
                type = it.type,
                time = DateUtil.getHourAndMinute()
            )


            when (it.type) {
                DetectedActivity.WALKING -> {
                    showLogError("$APP_TAG ActivityRecognition", "User is walking")
                }

                DetectedActivity.RUNNING -> {
                    showLogError("$APP_TAG ActivityRecognition", "User is running")
                }

                DetectedActivity.IN_VEHICLE -> {
                    showLogError("$APP_TAG ActivityRecognition", "User is in vehicle")
                }

                DetectedActivity.STILL -> {
                    showLogError("$APP_TAG ActivityRecognition", "User is still")
                }

                DetectedActivity.ON_FOOT -> {
                    showLogError("$APP_TAG ActivityRecognition", "User is on Foot")
                }

                DetectedActivity.ON_BICYCLE -> {
                    showLogError("$APP_TAG ActivityRecognition", "User is By Cycle")
                }

                else -> {
                    showLogError("$APP_TAG ActivityRecognition", "User is ${it.type}")
                }
            }
        }
    }
}