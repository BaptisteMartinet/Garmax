package griffith.baptiste.martinet.garmax

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import java.text.DecimalFormat

class MainActivity : AppCompatActivity() {
  private lateinit var _gpsHelper: GPSHelper
  private lateinit var _gpxHelper: GPXHelper
  private val _recordedLocations: MutableList<Location> = mutableListOf()

  private lateinit var trackerBtn: Button
  private lateinit var liveDistanceText: TextView
  private lateinit var liveTimeText: TextView
  private lateinit var liveSpeedText: TextView
  private lateinit var trackPointsText: TextView

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    _gpsHelper = GPSHelper(this)
    _gpxHelper = GPXHelper(this)

    trackerBtn = findViewById(R.id.trackerBtn)
    trackerBtn.setOnClickListener {
      switchTrackingMode()
    }
    //live data
    trackPointsText = findViewById(R.id.trackPointsText)
    trackPointsText.text = getString(R.string.track_point_placeholder).format(0)
    //circles
    liveDistanceText = findViewById(R.id.liveDistanceText)
    liveTimeText = findViewById(R.id.liveTimeText)
    liveSpeedText = findViewById(R.id.liveSpeedText)
  }

  private fun switchTrackingMode() {
    if (_gpsHelper.isTracking()) {
      val filepath = _gpxHelper.getCurrentFilePath()
      try {
        _gpsHelper.stopTracking()
        _gpxHelper.close()
      } catch (e: Exception) {
        Toast.makeText(this, "An error occurred while trying to stop the tracking", Toast.LENGTH_SHORT).show()
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
        _gpxHelper.writeLocation(location)
        _recordedLocations.add(location)
        updateLiveData()
      }
    } catch (e: Exception) {
      Toast.makeText(this, "An error occurred while trying to start the tracking", Toast.LENGTH_SHORT).show()
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

  private fun updateLiveData() {
    val nbRecordedLocations = _recordedLocations.size
    //live data
    trackPointsText.text = getString(R.string.track_point_placeholder).format(nbRecordedLocations)
    if (nbRecordedLocations < 2)
      return
    val trackDuration = LocationHelper.timeBetweenLocations(_recordedLocations.first(), _recordedLocations.last())
    val decimalFormat = DecimalFormat("00")
    //circles
    liveDistanceText.text = "%.2f".format(LocationHelper.distanceBetweenLocations(_recordedLocations) / 1000)
    liveTimeText.text = "%s:%s:%s".format(decimalFormat.format(trackDuration.getHours()), decimalFormat.format(trackDuration.getMinutes() % 60), decimalFormat.format(trackDuration.getSeconds() % 60))
    liveSpeedText.text = "%.2f".format(LocationHelper.speedBetweenLocations(_recordedLocations[nbRecordedLocations - 2], _recordedLocations[nbRecordedLocations - 1]))
  }
}
