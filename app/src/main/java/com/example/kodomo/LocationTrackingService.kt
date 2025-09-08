package com.example.kodomo

import android.app.*
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*

class LocationTrackingService : Service() {

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var tracker: TrackerLoop? = null

    companion object {
        private const val NOTIF_ID = 1
        private const val CHANNEL_ID = "kodomo_tracking_channel"
    }

    override fun onBind(intent: Intent?): IBinder? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIF_ID, createNotification())

        tracker = TrackerLoop(applicationContext) { log ->
            // Optionally, broadcast log messages to the UI or further processing.
        }

        scope.launch {
            tracker?.startLoop()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        tracker?.stopLoop()
        scope.cancel()
    }

    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("kodomo Tracking Active")
            .setContentText("Location tracking is running in the background.")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "kodomo Background Tracking",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
        }
    }
}
