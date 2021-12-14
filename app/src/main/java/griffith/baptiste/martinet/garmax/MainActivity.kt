package griffith.baptiste.martinet.garmax

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.PointF
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
  private lateinit var _gpsHelper: GPSHelper
  private lateinit var _gpxHelper: GPXHelper
  private val _recordedLocations: MutableList<Location> = mutableListOf()
  private val _decimalFormatter = DecimalFormat("00")
  private val _timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())

  private lateinit var trackerBtn: ImageButton
  private lateinit var liveDistanceText: TextView
  private lateinit var liveTimeText: TextView
  private lateinit var liveSpeedText: TextView
  private lateinit var trackPointsText: TextView
  private lateinit var graphView: Graph

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

    liveDistanceText.text = getString(R.string.distance_placeholder).format(0f)
    liveTimeText.text = getString(R.string.time_placeholder).format(_decimalFormatter.format(0), _decimalFormatter.format(0), _decimalFormatter.format(0))
    liveSpeedText.text = getString(R.string.speed_placeholder).format(0f)

    graphView = findViewById(R.id.liveSpeedGraph)
    graphView.abscissaAxisFormatFunction = { seconds: Float -> _timeFormatter.format(Date(TimeUnit.MILLISECONDS.convert(seconds.toLong(), TimeUnit.SECONDS))) }
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
      trackerBtn.setImageResource(R.drawable.ic_baseline_play_circle_outline_24)
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
    _recordedLocations.clear()
    graphView.resetPoints()
    try {
      _gpxHelper.create()
      _gpsHelper.startTracking(2000, 0f) { location ->
        graphView.loadPoint(PointF((TimeUnit.SECONDS.convert(location.time, TimeUnit.MILLISECONDS) % 86400).toFloat(), LocationHelper.speedBetweenLocations(_recordedLocations.lastOrNull(), location) * 3.6f), true)
        _recordedLocations.add(location)
        updateLiveData()
        _gpxHelper.writeLocation(location)
      }
    } catch (e: Exception) {
      Toast.makeText(this, "An error occurred while trying to start the tracking", Toast.LENGTH_SHORT).show()
      return
    }
    trackerBtn.setImageResource(R.drawable.ic_baseline_pause_circle_outline_24)
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
    //circles
    liveDistanceText.text = getString(R.string.distance_placeholder).format(LocationHelper.distanceBetweenLocations(_recordedLocations) / 1000)
    liveTimeText.text = trackDuration.toFormattedString(_decimalFormatter)
    liveSpeedText.text = getString(R.string.speed_placeholder).format(LocationHelper.speedBetweenLocations(_recordedLocations[nbRecordedLocations - 2], _recordedLocations[nbRecordedLocations - 1]) * 3.6f)
  }
}
