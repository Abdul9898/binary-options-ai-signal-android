package com.binaryoptions.aisignal.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.binaryoptions.aisignal.MainActivity
import com.binaryoptions.aisignal.R
import com.binaryoptions.aisignal.engine.SignalEngine
import com.binaryoptions.aisignal.model.Signal

class SignalBackgroundService : Service() {

    companion object {
        const val CHANNEL_ID = "binary_signals_channel"
        const val NOTIFICATION_ID = 1001

        fun startService(context: Context) {
            val intent = Intent(context, SignalBackgroundService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildForegroundNotification())

        // Start engine
        SignalEngine.instance.start()
        SignalEngine.instance.onNewSignalListener = { signal ->
            postSignalNotification(signal)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Binary AI Signals",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "High-priority alerts for new CALL/PUT binary options signals"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 250, 100, 250)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun buildForegroundNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Binary Options AI Signal")
            .setContentText("Monitoring live tick streams & generating signals")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun postSignalNotification(signal: Signal) {
        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val pendingIntent = PendingIntent.getActivity(
            this,
            signal.id.hashCode(),
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notif = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("⚡ ${signal.pair} -> ${signal.direction}")
            .setContentText("${signal.direction} (${signal.durationTicks} Ticks) @ ${signal.strikePrice} • Conf: ${signal.confidence}%")
            .setSubText(signal.reason)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setSound(soundUri)
            .setVibrate(longArrayOf(0, 300, 150, 300))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(signal.id.hashCode(), notif)
    }
}
