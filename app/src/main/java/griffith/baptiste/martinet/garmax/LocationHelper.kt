package griffith.baptiste.martinet.garmax

import android.location.Location

class LocationHelper {
  class LocationHelperTime(private val _milliseconds: Long) {
    fun getMilliseconds(): Long { return _milliseconds }
    fun getSeconds(): Long { return _milliseconds / 1000 }
    fun getMinutes(): Long { return  getSeconds() / 60  }
    fun getHours(): Long { return getMinutes() / 60 }
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

    fun speedBetweenLocations(loc1: Location, loc2: Location): Float {
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
  }
}