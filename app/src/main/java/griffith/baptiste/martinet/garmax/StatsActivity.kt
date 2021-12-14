package griffith.baptiste.martinet.garmax

import android.content.Intent
import android.graphics.PointF
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class StatsActivity: AppCompatActivity() {
  private val _gpxHelper = GPXHelper(this)
  private val _decimalFormatter = DecimalFormat("00")
  private val _timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_stats)

    val backBtn = findViewById<ImageButton>(R.id.backBtn)
    backBtn.setOnClickListener {
      val intent = Intent(this, MainActivity::class.java)
      intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
      startActivity(intent)
    }
    val explorerBtn = findViewById<ImageButton>(R.id.explorerBtn_statsActivity)
    explorerBtn.setOnClickListener {
      val intent = Intent(this, ExplorerActivity::class.java)
      startActivity(intent)
    }

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
    val dateFormatterTZ = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.getDefault())
    val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    val timeFormatterPM = SimpleDateFormat("HH:mm a", Locale.getDefault())
    val currentTime = Calendar.getInstance().time

    //basic track info
    val trackNameText = findViewById<TextView>(R.id.trackNameText)
    val trackDateText = findViewById<TextView>(R.id.trackDateText)
    val lastUpdateTimeText = findViewById<TextView>(R.id.lastUpdateTimeText)

    trackNameText.text = mainTrack.name
    trackDateText.text = dateFormatter.format(dateFormatterTZ.parse(data.metadata.time)!!)
    lastUpdateTimeText.text = timeFormatterPM.format(currentTime)

    //cards
    val distanceText = findViewById<TextView>(R.id.distanceText)
    val speedText = findViewById<TextView>(R.id.speedText)
    val timeText = findViewById<TextView>(R.id.timeText)
    val altitudeAverageText = findViewById<TextView>(R.id.altitudeAverageText)
    val altitudeMinText = findViewById<TextView>(R.id.altitudeMinText)
    val altitudeMaxText = findViewById<TextView>(R.id.altitudeMaxText)

    val segmentDuration = LocationHelper.timeBetweenLocations(mainSegment.first(), mainSegment.last())
    val altitudeInfo = LocationHelper.altitudeWithinLocations(mainSegment)

    distanceText.text = getString(R.string.distance_placeholder).format(LocationHelper.distanceBetweenLocations(mainSegment) / 1000)
    speedText.text = getString(R.string.speed_placeholder).format(LocationHelper.speedAverageBetweenLocations(mainSegment) * 3.6f)
    timeText.text = segmentDuration.toFormattedString(_decimalFormatter)
    altitudeAverageText.text = getString(R.string.altitude_average_placeholder).format(altitudeInfo.average)
    altitudeMinText.text = getString(R.string.altitude_min_placeholder).format(altitudeInfo.min)
    altitudeMaxText.text = getString(R.string.altitude_max_placeholder).format(altitudeInfo.max)

    //graph
    val speedGraph = findViewById<Graph>(R.id.speedGraph)
    speedGraph.abscissaAxisFormatFunction = { seconds: Float -> _timeFormatter.format(Date(TimeUnit.MILLISECONDS.convert(seconds.toLong(), TimeUnit.SECONDS))) }
    speedGraph.loadPoints(mainSegment.mapIndexed { index: Int, location: Location -> PointF((TimeUnit.SECONDS.convert(location.time, TimeUnit.MILLISECONDS) % 86400).toFloat(), LocationHelper.speedBetweenLocations(mainSegment.getOrNull(index - 1), location) * 3.6f) }, true)
  }
}
