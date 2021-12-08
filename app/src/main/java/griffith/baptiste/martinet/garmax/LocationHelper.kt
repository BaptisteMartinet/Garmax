package griffith.baptiste.martinet.garmax

import android.location.Location

class LocationHelper {
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

    private fun speedBetweenLocations(loc1: Location, loc2: Location): Float {
      if (loc2.time - loc1.time <= 0)
        return 0f
      return distanceBetweenLocations(loc1, loc2) / ((loc2.time - loc1.time) / 1000)
    }

    fun speedAverageBetweenLocations(locations: List<Location>): Float {
      if (locations.size <= 1)
        return  0f
      var totalSpeed = 0f
      for (i in 0 until (locations.size - 1)) {
        totalSpeed += speedBetweenLocations(locations[i], locations[i + 1])
      }
      return totalSpeed / (locations.size - 1)
    }
  }
}