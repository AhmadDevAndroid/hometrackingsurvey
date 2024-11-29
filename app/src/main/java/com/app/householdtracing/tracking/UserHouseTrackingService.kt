package com.app.householdtracing.tracking

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
import android.os.Binder
import android.os.Build
import android.os.IBinder
import com.app.householdtracing.data.datastore.PreferencesManager
import com.app.householdtracing.util.AppNotificationManager
import com.app.householdtracing.worker.SunriseTrackingWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


class UserHouseTrackingService : Service() {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val binder = LocalBinder()
    private val notificationManager by lazy { AppNotificationManager(this) }

    inner class LocalBinder : Binder() {
        val service: UserHouseTrackingService get() = this@UserHouseTrackingService
    }

    override fun onBind(intent: Intent): IBinder = binder

    override fun onCreate() {
        super.onCreate()

        scope.launch {
            PreferencesManager.getValue(PreferencesManager.SUNRISE_TIME, 0).collectLatest {
                SunriseTrackingWorker.configureWorker(this@UserHouseTrackingService)
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForegroundService()
        return START_STICKY
    }

    @SuppressLint("NewApi")
    private fun startForegroundService() {
        val notification = notificationManager.setUpNotification(
            channelId = AppNotificationManager.TRACKING_NOTIFICATION_CHANNEL_ID
        )
        val foregroundServiceType =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                FOREGROUND_SERVICE_TYPE_LOCATION
            } else 0

        startForeground(1, notification.build(), foregroundServiceType)
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}