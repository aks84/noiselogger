package com.tubemint.noiselogger

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private var audioRecord: AudioRecord? = null
    private var isRecording = false
    private val RECORD_AUDIO_PERMISSION_REQUEST = 200
    private lateinit var logTextView: TextView
    private lateinit var locationTextView: TextView
    private lateinit var dateTextView: TextView
    private lateinit var timeTextView: TextView
    private lateinit var temperatureTextView: TextView
    private var locationManager: LocationManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        logTextView = findViewById(R.id.logTextView)
        locationTextView = findViewById(R.id.locationTextView)
        dateTextView = findViewById(R.id.dateTextView)
        timeTextView = findViewById(R.id.timeTextView)
        temperatureTextView = findViewById(R.id.temperatureTextView)

        val recordButton: Button = findViewById(R.id.recordButton)
        recordButton.setOnClickListener {
            if (isRecording) {
                stopRecording()
            } else {
                requestRecordingPermission()
            }
        }

        // Initialize location manager
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager?

        // Request location updates (you may need to handle runtime permissions)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                requestLocationUpdates()
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    1
                )
            }
        } else {
            requestLocationUpdates()
        }

        // Update date and time every second
        val handler = Handler(Looper.getMainLooper())
        handler.post(object : Runnable {
            override fun run() {
                updateDateTime()
                handler.postDelayed(this, 1000)
            }
        })
    }

    private fun updateDateTime() {
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val currentTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())

        dateTextView.text = "Date: $currentDate"
        timeTextView.text = "Time: $currentTime"
    }

    private fun requestLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        locationManager?.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            0L,
            0f,
            locationListener
        )
    }




    private val locationListener: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            Log.d("Location", "Latitude: ${location.latitude}, Longitude: ${location.longitude}")
            // Update locationTextView with current location information
            val locationLat: Double = location.latitude
            val locationLong: Double = location.longitude
            locationTextView.text = "Location: Lat ${locationLat}, Long ${locationLong}"
        }

        override fun onProviderDisabled(provider: String) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
    }

    private fun requestRecordingPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.RECORD_AUDIO
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                startRecording()
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.RECORD_AUDIO),
                    RECORD_AUDIO_PERMISSION_REQUEST
                )
            }
        } else {
            startRecording()
        }
    }







    private fun startRecording() {
        // ... (rest of the recording logic)
        logTextView.text = "Recording started"
        Log.d("MainActivity", "Recording started")

    val temperature = getTemperature()
        temperatureTextView.text = "Temperature: $temperature°C"
    }


        // Example method to get temperature (replace this with your actual logic)
    private fun getTemperature(): Int {
        // Replace this with your logic to fetch real-time temperature data
        // For now, let's return a dummy value (e.g., 27°C)
        return 27
    }


    private fun stopRecording() {
        isRecording = false
        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null
        logTextView.text = "Recording stopped"
        Log.d("MainActivity", "Recording stopped")
        temperatureTextView.text = ""  // Clear the temperature text when recording stops
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            RECORD_AUDIO_PERMISSION_REQUEST -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startRecording()
                } else {
                    logTextView.text = "Recording permission denied"
                    Log.d("MainActivity", "Recording permission denied")
                    // Handle permission denied
                }
            }
            1 -> {
                // Request for location updates permission
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    requestLocationUpdates()
                    locationTextView.text = "Location"
                } else {
                    locationTextView.text = "Location permission denied"
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        locationManager?.removeUpdates(locationListener)
    }
}
