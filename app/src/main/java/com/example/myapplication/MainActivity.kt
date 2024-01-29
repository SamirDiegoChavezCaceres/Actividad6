package com.example.myapplication

import android.app.NotificationChannel
import android.app.NotificationManager
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
        createNotificationChannel()

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
    private fun startChronometer() {
        chronometerService?.startChronometer()
        isUpdating = true
        updateElapsedTime()
    }

    private fun stopChronometer() {
        chronometerService?.stopChronometer()
        isUpdating = false
    }

    private fun resetChronometer() {
        chronometerService?.resetChronometer()
        isUpdating = true
        updateElapsedTime()
    }

    private fun updateElapsedTime() {
        if (isUpdating) {
            // Obtener el tiempo transcurrido del servicio
            val elapsedTime = chronometerService?.getElapsedTime()

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