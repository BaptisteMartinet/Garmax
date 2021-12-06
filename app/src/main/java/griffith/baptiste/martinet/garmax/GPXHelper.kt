package griffith.baptiste.martinet.garmax

import android.content.Context
import android.location.Location
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/*
* Tip: Check emulator files using Android Studio: View -> Tool Windows -> Device File Explorer
* Path: "sdcard/Android/data/[app name]/files/[directory name]"
 */

class GPXHelper(private val context: Context) {
  class GPXData {
    var creator: String = ""
    var version: String = ""
    var metadata: GPXMetaData = GPXMetaData()
    var tracks: MutableList<GPXDataTrack> = mutableListOf()

    override fun toString(): String {
      return "{ creator: $creator, version: $version, metadata: { time: ${metadata.time} }, tracks: $tracks }"
    }
  }
  class GPXMetaData {
    var time: String = ""
  }
  class GPXDataTrack {
    var name: String = ""
    var segments: MutableList<MutableList<Location>> = mutableListOf()

    override fun toString(): String {
      return "{ name: $name, segments: $segments }"
    }
  }

  private var _file: File? = null
  private val _directoryName: String = "GPStracks"
  private val _extensionName: String = ".gpx"
  private val _dateFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")

  fun getCurrentFilePath(): String? = _file?.absolutePath

  fun create(): Boolean {
    if (_file != null)
      return false
    val externalStoragePath: File = context.getExternalFilesDir(null) ?: return false
    val currentDate: Date = Calendar.getInstance().time
    val fileName: String = _dateFormatter.format(currentDate)

    val dir = File(externalStoragePath, _directoryName)
    dir.mkdir() //create dir if not exist
    _file = File(dir, fileName + _extensionName) //create file
    writeHeader(currentDate)
    return true
  }

  fun close(): Boolean {
    if (_file == null)
      return false
    if (!writeFooter())
      return false
    _file = null
    return true
  }

  private fun writeHeader(date: Date): Boolean {
    val header: String =
      "<gpx creator=\"Baptiste Martinet\" version=\"1.1\">\n" +
          "<metadata>\n" +
          "<time>${_dateFormatter.format(date)}</time>\n" +
          "</metadata>\n" +
          "<trk>\n" +
          "<name>Garmax app GPX document</name>\n" +
          "<trkseg>\n"
    _file?.appendText(header) ?: return false
    return true
  }

  fun writeLocation(location: Location): Boolean {
    val point =
      "<trkpt lat=\"${location.latitude}\" lon=\"${location.longitude}\" alt=\"${location.altitude}\">\n" +
          "<time>${_dateFormatter.format(Date(location.time))}</time>\n" +
          "</trkpt>\n"
    _file?.appendText(point) ?: return false
    return true
  }

  private fun writeFooter(): Boolean {
    val footer: String =
      "</trkseg>\n" +
          "</trk>\n" +
          "</gpx>"
    _file?.appendText(footer) ?: return false
    return true
  }

  fun readFile(filepath: String): GPXData {
    val data = GPXData()

    val file = File(filepath)
    val lines = file.readLines()

    val reg = Regex("^<([a-z]+)(?:\\s+([a-z]+)=\"([^\"]+)\")?(?:\\s+([a-z]+)=\"([^\"]+)\")?(?:\\s+([a-z]+)=\"([^\"]+)\")?\\s*>(?:(?<content>[^<]*)(?:</[a-z]+>))?\$")
    var lastOpenedTag = ""

    for (line in lines) {
      val matchedResults: MatchResult = reg.matchEntire(line) ?: continue
      val groups = matchedResults.groups.filterIndexed { index, matchGroup -> (index != 0 && matchGroup != null) }

      if (groups.isEmpty())
        continue
      when (groups[0]?.value) {
        "gpx" -> {
          lastOpenedTag = "gpx"
          if (groups.size != 5)
            continue
          data.creator = groups[2]!!.value
          data.version = groups[4]!!.value
        }
        "metadata" -> {
          lastOpenedTag = "metadata"
        }
        "time" -> {
          if (groups.size != 2)
            continue
          when (lastOpenedTag) {
            "metadata" -> data.metadata.time = groups[1]!!.value
            "trkpt" -> data.tracks.last().segments.last().last().time = _dateFormatter.parse(groups[1]!!.value).time
          }
        }
        "trk" -> {
          lastOpenedTag = "trk"
          data.tracks.add(GPXDataTrack())
        }
        "name" -> {
          if (groups.size != 2)
            continue
          if (lastOpenedTag == "trk")
            data.tracks.last().name = groups[1]!!.value
        }
        "trkseg" -> {
          if (lastOpenedTag == "trk") {
            data.tracks.last().segments.add(mutableListOf())
            lastOpenedTag = "trkseg"
          }
        }
        "trkpt" -> {
          if (groups.size != 7)
            continue
          lastOpenedTag = "trkpt"
          val loc = Location("")
          loc.latitude = groups[2]!!.value.toDouble()
          loc.longitude = groups[4]!!.value.toDouble()
          loc.altitude = groups[6]!!.value.toDouble()
          data.tracks.last().segments.last().add(loc)
        }
      }
    }
    return data
  }
}