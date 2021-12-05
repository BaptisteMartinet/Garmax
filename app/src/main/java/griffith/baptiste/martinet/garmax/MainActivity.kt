package griffith.baptiste.martinet.garmax

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.core.app.ActivityCompat

class MainActivity : AppCompatActivity() {
  private lateinit var gpsHelper: GPSHelper
  private lateinit var trackerBtn: Button

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    gpsHelper = GPSHelper(this)

    trackerBtn = findViewById(R.id.trackerBtn)
    trackerBtn.setOnClickListener {
      switchTrackingMode()
    }
  }

  private fun switchTrackingMode() {
    if (gpsHelper.isTracking()) {
      gpsHelper.stopTracking()
      trackerBtn.text = "start"
      // TODO launch stats activity
      return
    }
    if (ActivityCompat.checkSelfPermission(this,  Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
      && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
      requestPermissions(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION), 1)
      return
    }
    if (!gpsHelper.startTracking(5000, 0f))
      return
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
