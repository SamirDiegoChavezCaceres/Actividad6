package com.example.myapplication

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import androidx.core.app.NotificationCompat

class ChronometerService : Service() {
    private var isRunning = false
    private var startTime: Long = 0L
    private val binder = LocalBinder()
    private val handler = Handler()

    companion object {
        const val ACTION_START = "com.example.myapplication.ChronometerService.START"
        const val ACTION_STOP = "com.example.myapplication.ChronometerService.STOP"
        const val ACTION_RESET = "com.example.myapplication.ChronometerService.RESET"
    }

    inner class LocalBinder : Binder() {
        fun getService(): ChronometerService = this@ChronometerService
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NotificationHelper.NOTIFICATION_ID, createNotification())
        if (intent != null && intent.action != null) {
            when (intent.action) {
                ACTION_START -> startChronometer()
                ACTION_STOP -> stopChronometer()
                ACTION_RESET -> resetChronometer()
            }
        }
        return START_NOT_STICKY
    }

    private fun createNotification(): Notification {
        createNotificationChannel()

        return NotificationCompat.Builder(this, NotificationHelper.CHANNEL_ID)
            .setContentTitle("Cronómetro en ejecución")
            .setContentText("Tiempo transcurrido: ${getElapsedTime()}")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()
    }

    private fun createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NotificationHelper.CHANNEL_ID,
                "Cronometer Channel",
                NotificationManager.IMPORTANCE_LOW
            )

            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun startChronometer() {
        if (!isRunning) {
            startTime = System.currentTimeMillis()
            isRunning = true
            updateNotification()
        }
    }

    fun stopChronometer() {
        if (isRunning) {
            isRunning = false
        }
    }

    fun resetChronometer() {
        stopChronometer()
        startChronometer()
    }

    private fun updateNotification() {
        if (isRunning) {
            handler.postDelayed({
                val notification = createNotification()
//                notificationManager.notify(NotificationHelper.NOTIFICATION_ID, notification)
                updateNotification()
            }, 1000)
        }
    }

    fun getElapsedTime(): String {
        val currentTime = System.currentTimeMillis()
        val elapsedTime = currentTime - startTime
        val seconds = (elapsedTime / 1000).toInt()
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60

        return String.format("%02d:%02d", minutes, remainingSeconds)
    }
}