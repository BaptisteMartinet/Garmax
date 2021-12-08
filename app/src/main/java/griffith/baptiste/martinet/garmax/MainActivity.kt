package griffith.baptiste.martinet.garmax

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.core.app.ActivityCompat

class MainActivity : AppCompatActivity() {
  private lateinit var _gpsHelper: GPSHelper
  private lateinit var _gpxHelper: GPXHelper
  private lateinit var trackerBtn: Button

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    _gpsHelper = GPSHelper(this)
    _gpxHelper = GPXHelper(this)

    trackerBtn = findViewById(R.id.trackerBtn)
    trackerBtn.setOnClickListener {
      switchTrackingMode()
    }
  }

  private fun switchTrackingMode() {
    if (_gpsHelper.isTracking()) {
      val filepath = _gpxHelper.getCurrentFilePath()
      try {
        _gpsHelper.stopTracking()
        _gpxHelper.close()
      } catch (e: Exception) {
        // TODO display a Toast or something
        Log.i("debug", "An exception has occurred: ${e.message}")
        return
      }
      trackerBtn.text = "start"
      val intent = Intent(this, StatsActivity::class.java)
      intent.putExtra("filepath", filepath)
      startActivity(intent)
      return
    }
    if (ActivityCompat.checkSelfPermission(this,  Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
      && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
      requestPermissions(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION), 1)
      return
    }
    try {
      _gpxHelper.create()
      _gpsHelper.startTracking(5000, 0f) { location ->
        Log.i("debug", location.toString())
        _gpxHelper.writeLocation(location)
      }
    } catch (e: Exception) {
      // TODO display a Toast or something
      Log.i("debug", "An exception has occurred: ${e.message}")
      return
    }
    trackerBtn.text = "stop"
  }

  override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    if(requestCode == 1 && grantResults.size >= 2) {
      if(grantResults[0] == PackageManager.PERMISSION_DENIED || grantResults[1] == PackageManager.PERMISSION_DENIED) {
        Log.i("debug", "Location permissions denied.")
      } else {
        switchTrackingMode()
      }
    }
  }
}
