package com.example.myapplication

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.example.myapplication.ChronometerService.Companion.ACTION_RESET
import com.example.myapplication.ChronometerService.Companion.ACTION_START
import com.example.myapplication.ChronometerService.Companion.ACTION_STOP

class MainActivity : AppCompatActivity() {
    private var chronometerService: ChronometerService? = null
    private var isServiceBound = false
    private lateinit var startButton: Button
    private lateinit var stopButton: Button
    private lateinit var resetButton: Button
    private lateinit var timeElapsedTextView: TextView
    private val handler = Handler(Looper.getMainLooper())
    private var isUpdating = false



    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as ChronometerService.LocalBinder
            chronometerService = binder.getService()
            isServiceBound = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isServiceBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        startButton = findViewById(R.id.startButton)
        stopButton = findViewById(R.id.stopButton)
        resetButton = findViewById(R.id.resetButton)
        timeElapsedTextView = findViewById(R.id.timeElapsedTextView)

        // Botones en la actividad
        startButton.setOnClickListener { startChronometer() }
        stopButton.setOnClickListener { stopChronometer() }
        resetButton.setOnClickListener { resetChronometer() }
        // Conectar al servicio
        val serviceIntent = Intent(this, ChronometerService::class.java)
        bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE)
    }

    private fun startChronometer() {
        chronometerService?.startChronometer()
        updateNotification(ACTION_START)
        isUpdating = true
        updateElapsedTime()
    }

    private fun stopChronometer() {
        chronometerService?.stopChronometer()
        isUpdating = false
        updateNotification(ACTION_STOP)
    }

    private fun resetChronometer() {
        chronometerService?.resetChronometer()
        updateNotification(ACTION_RESET)
    }

    private fun updateNotification(action: String) {
        val notification = chronometerService?.let {
            NotificationCompat.Builder(this, NotificationHelper.CHANNEL_ID)
                .setContentTitle("Cronómetro en ejecución")
                .setContentText("Tiempo transcurrido: ${it.getElapsedTime()}")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .addAction(createNotificationAction("Stop", ACTION_STOP))
                .addAction(createNotificationAction("Reset", ACTION_RESET))
                .build()
        }

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NotificationHelper.NOTIFICATION_ID, notification)

        // Enviar la acción al servicio
        val intent = Intent(this, ChronometerService::class.java)
        intent.action = action
        startService(intent)
    }

    private fun createNotificationAction(label: String, action: String): NotificationCompat.Action {
        val intent = Intent(this, ChronometerService::class.java)
        intent.action = action
        val pendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        return NotificationCompat.Action(0, label, pendingIntent)
    }

    private fun updateElapsedTime() {
        if (isUpdating) {
            // Obtener el tiempo transcurrido del servicio
            val elapsedTime = chronometerService?.getElapsedTime() ?: "00:00"

            // Actualizar el TextView
            timeElapsedTextView.text = "Tiempo transcurrido: $elapsedTime"

            // Programar la próxima actualización después de 1 segundo
            handler.postDelayed({ updateElapsedTime() }, 1000)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isServiceBound) {
            unbindService(connection)
            isServiceBound = false
        }
    }
}