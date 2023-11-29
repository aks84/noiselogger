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
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private var audioRecord: AudioRecord? = null
    private var isRecording = false
    private val RECORD_AUDIO_PERMISSION_REQUEST = 200
    private lateinit var ambientNoiseTextView: TextView
    private lateinit var logTextView: TextView
    private lateinit var locationTextView: TextView
    private var locationManager: LocationManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ambientNoiseTextView = findViewById(R.id.logTextView)
        logTextView = findViewById(R.id.logTextView)
        locationTextView = findViewById(R.id.locationTextView)

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
    }

    private fun requestLocationUpdates() {
        locationManager?.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            0L,
            0f,
            locationListener
        )
    }

    private val locationListener: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            // Update locationTextView with current location information
            locationTextView.text = "Location: Lat ${location.latitude}, Long ${location.longitude}"
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
    }

    private fun stopRecording() {
        isRecording = false
        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null
        logTextView.text = "Recording stopped"
        Log.d("MainActivity", "Recording stopped")
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
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
                    locationTextView.text = "Location "
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
