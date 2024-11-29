package com.app.householdtracing.util

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.app.householdtracing.App
import com.app.householdtracing.receiver.AlarmReceiver
import com.app.householdtracing.util.AppUtil.showLogError


class AlarmManager(private val context: Context = App.getInstance()) {

    private val alarmManager by lazy {
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    }

    @SuppressLint("MissingPermission")
    fun scheduleNextAlarmTime(triggerTime: Long, sunriseTimeInMilliSeconds: Long) {

        showLogError("Tracking schedule Time", "$triggerTime && $sunriseTimeInMilliSeconds")

        val context = App.getInstance()
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(AlarmReceiver.sunrise_time, sunriseTimeInMilliSeconds)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val alarmClock = AlarmManager.AlarmClockInfo(triggerTime, null)
        alarmManager.setAlarmClock(alarmClock, pendingIntent)


    }

}

