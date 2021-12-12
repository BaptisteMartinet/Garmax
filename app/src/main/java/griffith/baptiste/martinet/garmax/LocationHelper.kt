package griffith.baptiste.martinet.garmax

import android.location.Location
import java.text.DecimalFormat
import java.util.concurrent.TimeUnit

class LocationHelper {
  class LocationHelperTime(private val _milliseconds: Long) {
    //fun getMilliseconds(): Long { return _milliseconds }
    fun getSeconds(): Long { return TimeUnit.SECONDS.convert(_milliseconds, TimeUnit.MILLISECONDS) }
    fun getMinutes(): Long { return TimeUnit.MINUTES.convert(_milliseconds, TimeUnit.MILLISECONDS)  }
    fun getHours(): Long { return TimeUnit.HOURS.convert(_milliseconds, TimeUnit.MILLISECONDS) }
    fun toFormattedString(decimalFormat: DecimalFormat): String {
      return "${decimalFormat.format(getHours() % 24)}:${decimalFormat.format(getMinutes() % 60)}:${decimalFormat.format(getSeconds() % 60)}"
    }
  }
  class LocationHelperAltitude {
    var average: Double = 0.0
    var min: Double = 0.0
    var max: Double = 0.0
  }

  companion object {
    private fun distanceBetweenLocations(loc1: Location, loc2: Location): Float {
      return loc1.distanceTo(loc2)
    }

    fun distanceBetweenLocations(locations: List<Location>): Float {
      var distance = 0f
      for (i in 0 until (locations.size - 1)) {
        distance += distanceBetweenLocations(locations[i], locations[i + 1])
      }
      return distance
    }

    fun speedBetweenLocations(loc1: Location?, loc2: Location?): Float {
      if (loc1 == null || loc2 == null)
        return 0f
      val timeInBetween = timeBetweenLocations(loc1, loc2)
      if (timeInBetween.getSeconds() <= 0)
        return 0f
      return distanceBetweenLocations(loc1, loc2) / timeInBetween.getSeconds()
    }

    fun speedAverageBetweenLocations(locations: List<Location>): Float {
      if (locations.size <= 1)
        return 0f
      var totalSpeed = 0f
      for (i in 0 until (locations.size - 1)) {
        totalSpeed += speedBetweenLocations(locations[i], locations[i + 1])
      }
      return totalSpeed / (locations.size - 1)
    }

    fun timeBetweenLocations(loc1: Location, loc2: Location): LocationHelperTime {
      return LocationHelperTime(loc2.time - loc1.time)
    }

    fun altitudeWithinLocations(locations: List<Location>): LocationHelperAltitude {
      val alt = LocationHelperAltitude()
      if (locations.isEmpty())
        return alt
      for (loc in locations) {
        alt.average += loc.altitude
        if (loc.altitude < alt.min)
          alt.min = loc.altitude
        else if (loc.altitude > alt.max)
          alt.max = loc.altitude
      }
      alt.average /= locations.size
      return alt
    }
  }
}
