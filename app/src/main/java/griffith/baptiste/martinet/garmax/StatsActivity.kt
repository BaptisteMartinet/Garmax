package griffith.baptiste.martinet.garmax

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

class StatsActivity: AppCompatActivity() {
  private val _gpxHelper = GPXHelper(this)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val filepath: String = intent.extras?.getString("filepath") ?: ""
    try {
      val data = _gpxHelper.readFile(filepath)
      Log.i("debug", data.toString())
    } catch (e: Exception) {
      if (e is GPXParsingError) {
        Log.i("debug", e.message!!)
      }
    }
  }
}
