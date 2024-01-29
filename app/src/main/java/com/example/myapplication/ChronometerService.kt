package com.example.myapplication

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
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
    private var elapsedTime: Long = 0
    private val binder = LocalBinder()
    private val handler = Handler()
    inner class LocalBinder : Binder() {
        fun getService(): ChronometerService = this@ChronometerService
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val notification = createNotification()
        startForeground(NotificationHelper.NOTIFICATION_ID, notification)
        return START_NOT_STICKY
    }

    private fun createNotification(): Notification {
        val pendingIntentStop = PendingIntent.getBroadcast(this,1,
            Intent(this, ChronometerReceiver::class.java).putExtra("action","stop"),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val pendingIntentStart = PendingIntent.getBroadcast(this,2,
            Intent(this, ChronometerReceiver::class.java).putExtra("action","start"),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val pendingIntentReset = PendingIntent.getBroadcast(this,3,
            Intent(this, ChronometerReceiver::class.java).putExtra("action","reset"),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        return NotificationCompat.Builder(this, NotificationHelper.CHANNEL_ID)
            .setContentTitle("Cronómetro en ejecución")
            .setContentText("Tiempo transcurrido: ${getElapsedTime()}")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .addAction(0,"Iniciar",pendingIntentStart)
            .addAction(0,"Reiniciar",pendingIntentReset)
            .addAction(0,"Detener",pendingIntentStop)
            .build()
    }


    fun startChronometer() {
        if (!isRunning) {
            startTime = System.currentTimeMillis() - elapsedTime
            isRunning = true
            updateNotification()
        }
    }

    fun stopChronometer() {
        if (isRunning) {
            elapsedTime = System.currentTimeMillis() - startTime
            isRunning = false
        }
    }

    fun resetChronometer() {
        startTime = System.currentTimeMillis()
        isRunning = false
        elapsedTime = 0
    }

    private fun updateNotification() {
        if (isRunning) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            handler.postDelayed({
                val notification = createNotification()
                notificationManager.notify(NotificationHelper.NOTIFICATION_ID, notification)
                updateNotification()
            }, 1000)
        }
    }
    fun getElapsedTime(): String {
        val currentTime = if (isRunning) {
            System.currentTimeMillis() - startTime
        } else {
            elapsedTime
        }

        val seconds = (currentTime / 1000).toInt()
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60

        return String.format("%02d:%02d", minutes, remainingSeconds)
    }
}