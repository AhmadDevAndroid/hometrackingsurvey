package com.app.householdtracing.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.app.householdtracing.worker.HomeLocationTrackingWorker

class AlarmReceiver : BroadcastReceiver() {


    companion object {
        const val sunrise_time = "sunrise"
    }

    override fun onReceive(context: Context, intent: Intent?) {
        val sunriseTime = intent?.getLongExtra(sunrise_time, 0L) ?: 0L
        HomeLocationTrackingWorker.configureWorker(context, sunriseTime)
    }
}



