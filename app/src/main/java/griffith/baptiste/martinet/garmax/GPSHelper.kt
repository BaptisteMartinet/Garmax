package griffith.baptiste.martinet.garmax

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityCompat

class GPSHelper(private val context: Context) {
  private var _isTracking = false
  private lateinit var _callback: (location: Location) -> Unit
  private val _locationManager: LocationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
  private val _locationListener = object : LocationListener {
    override fun onLocationChanged(location: Location) {
      _callback.invoke(location)
    }
    override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
  }

  fun isTracking() = _isTracking

  fun startTracking(minTimeMs: Long, minDistanceM: Float, callback: (location: Location) -> Unit) {
    if (_isTracking)
      throw Exception("Tracking is already activated")
    if (ActivityCompat.checkSelfPermission(context,  Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
      && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
      throw Exception("GPS permission denied")
    }
    _callback = callback
    _locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTimeMs, minDistanceM, _locationListener)
    _isTracking = true
  }

  fun stopTracking() {
    if (!_isTracking)
      throw Exception("GPS is not currently tracking")
    _locationManager.removeUpdates(_locationListener)
    _isTracking = false
  }
}