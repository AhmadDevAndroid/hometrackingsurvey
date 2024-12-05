package com.app.householdtracing.util

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.app.householdtracing.R

class AppNotificationManager(private val context: Context) {

    private val notificationManager by lazy {
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    companion object {
        const val TRACKING_NOTIFICATION_CHANNEL_ID = "tracking_id"
        const val WORKER_CHANNEL_ID = "worker"
        const val LOCATION_WORKER_CHANNEL_ID = "location_worker"
        const val GEOFENCE_CHANNEL_ID = "geofence_id"
    }

     fun createNotificationChannel(channelId: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelId.uppercase(),
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun setUpNotification(channelId : String,text: String = context.getString(R.string.tracking_location)): NotificationCompat.Builder {

        createNotificationChannel(channelId)

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notification =
            NotificationCompat.Builder(context, channelId)
                .setSmallIcon(android.R.drawable.ic_menu_mylocation)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(text)
                .setSound(defaultSoundUri).setAutoCancel(false).setOngoing(true).setLargeIcon(
                    BitmapFactory.decodeResource(context.resources, android.R.drawable.ic_menu_mylocation)
                )

        notificationManager.notify(channelId.hashCode(), notification.build())

        return notification
    }

    fun createForegroundNotification(channelId: String): Notification {

        return NotificationCompat.Builder(context, channelId)
            .setContentTitle(context.getString(R.string.app_name))
            .setSmallIcon(android.R.drawable.ic_menu_mylocation).build()
    }



}