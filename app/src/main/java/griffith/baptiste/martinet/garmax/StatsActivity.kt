package griffith.baptiste.martinet.garmax

import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*

class StatsActivity: AppCompatActivity() {
  private val _gpxHelper = GPXHelper(this)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_stats)

    val filepath: String = intent.extras?.getString("filepath") ?: ""
    val data: GPXHelper.GPXData
    val mainTrack: GPXHelper.GPXTrack
    val mainSegment: List<Location>
    try {
      data = _gpxHelper.readFile(filepath)
      mainTrack = data.tracks.first()
      mainSegment = mainTrack.segments.first()
    } catch (e: Exception) {
      if (e is GPXParsingError) {
        Log.i("debug", e.message!!)
      }
      return
    }
    val dateFormatterTZ = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.UK)
    val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.UK)
    val timeFormatterPM = SimpleDateFormat("HH:mm 'PM'", Locale.UK)
    val currentTime = Calendar.getInstance().time

    //basic track info
    val trackNameText = findViewById<TextView>(R.id.trackNameText)
    val trackDateText = findViewById<TextView>(R.id.trackDateText)
    val lastUpdateTimeText = findViewById<TextView>(R.id.lastUpdateTimeText)
    //circles
    val distanceText = findViewById<TextView>(R.id.distanceText)
    val timeText = findViewById<TextView>(R.id.timeText)
    val speedText = findViewById<TextView>(R.id.speedText)

    val trackDurationMillis = mainSegment.last().time - mainSegment.first().time
    val trackDurationSeconds = trackDurationMillis / 1000
    val trackDurationMinutes = trackDurationSeconds / 60
    val trackDurationHours = trackDurationMinutes / 60

    //basic track info
    trackNameText.text = mainTrack.name
    trackDateText.text = dateFormatter.format(dateFormatterTZ.parse(data.metadata.time)!!)
    lastUpdateTimeText.text = timeFormatterPM.format(currentTime)
    //circles
    distanceText.text = "%.2f".format(LocationHelper.distanceBetweenLocations(mainSegment) / 1000)
    timeText.text = "%d:%d:%d".format(trackDurationHours, trackDurationMinutes, trackDurationSeconds)
    speedText.text = "%.2f".format(LocationHelper.speedAverageBetweenLocations(mainSegment))
  }
}
