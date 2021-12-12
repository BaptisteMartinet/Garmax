package griffith.baptiste.martinet.garmax

import android.content.Context
import android.location.Location
import android.location.LocationManager
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayDeque

/*
* Tip: Check emulator files using Android Studio: View -> Tool Windows -> Device File Explorer
* Path: "sdcard/Android/data/[app name]/files/[directory name]"
 */

class GPXHelper(private val context: Context) {
  class GPXData {
    var creator: String = ""
    var version: String = ""
    var metadata: GPXMetaData = GPXMetaData()
    var tracks: MutableList<GPXTrack> = mutableListOf()

    override fun toString(): String {
      return "{ creator: $creator, version: $version, metadata: $metadata, tracks: $tracks }"
    }
  }

  class GPXMetaData {
    var time: String = ""

    override fun toString(): String {
      return "{ time: $time }"
    }
  }

  class GPXTrack {
    var name: String = ""
    var segments: MutableList<MutableList<Location>> = mutableListOf()

    override fun toString(): String {
      return "{ name: $name, segments: $segments }"
    }
  }

  enum class TagEnum {
    GPX,
    CLOSE_GPX,
    METADATA,
    CLOSE_METADATA,
    TRACK,
    CLOSE_TRACK,
    TRACK_SEGMENT,
    CLOSE_TRACK_SEGMENT,
    TRACK_POINT,
    CLOSE_TRACK_POINT,
    TIME,
    NAME,
    ELEVATION,
  }

  private val _tags = mapOf(
    TagEnum.GPX to "gpx",
    TagEnum.CLOSE_GPX to "/gpx",
    TagEnum.METADATA to "metadata",
    TagEnum.CLOSE_METADATA to "/metadata",
    TagEnum.TRACK to "trk",
    TagEnum.CLOSE_TRACK to "/trk",
    TagEnum.TRACK_SEGMENT to "trkseg",
    TagEnum.CLOSE_TRACK_SEGMENT to "/trkseg",
    TagEnum.TRACK_POINT to "trkpt",
    TagEnum.CLOSE_TRACK_POINT to "/trkpt",
    TagEnum.TIME to "time",
    TagEnum.NAME to "name",
    TagEnum.ELEVATION to "ele",
  )

  private var _file: File? = null
  private val _directoryName: String = "GPStracks"
  private val _extensionName: String = ".gpx"
  private val _dateFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.getDefault())

  fun getCurrentFilePath(): String? = _file?.absolutePath

  fun create() {
    if (_file != null)
      throw Exception("A file is already open")
    val externalStoragePath: File = context.getExternalFilesDir(null) ?: throw Exception("Could not get externalStoragePath")
    val currentDate: Date = Calendar.getInstance().time
    val fileName: String = _dateFormatter.format(currentDate)

    val dir = File(externalStoragePath, _directoryName)
    dir.mkdir() //create dir if not exist
    _file = File(dir, fileName + _extensionName) //create file
    this.writeHeader(currentDate)
  }

  fun close() {
    if (_file == null)
      throw Exception("No file open")
    this.writeFooter()
    _file = null
  }

  private fun writeHeader(date: Date) {
    val header: String =
      "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\" ?>\n" +
          "<gpx creator=\"Baptiste Martinet\" version=\"1.1\">\n" +
          "<metadata>\n" +
          "<time>${_dateFormatter.format(date)}</time>\n" +
          "</metadata>\n" +
          "<trk>\n" +
          "<name>Garmax app GPX document</name>\n" +
          "<trkseg>\n"
    _file?.appendText(header) ?: throw Exception("Could not appendText")
  }

  fun writeLocation(location: Location) {
    val point =
      "<trkpt lat=\"${location.latitude}\" lon=\"${location.longitude}\">\n" +
          "<time>${_dateFormatter.format(Date(location.time))}</time>\n" +
          "<ele>${location.altitude}</ele>\n" +
          "</trkpt>\n"
    _file?.appendText(point) ?: throw Exception("Could not appendText")
  }

  private fun writeFooter() {
    val footer: String =
      "</trkseg>\n" +
          "</trk>\n" +
          "</gpx>"
    _file?.appendText(footer) ?: throw Exception("Could not appendText")
  }

  /*
  * Explanation:
  * Before regex: '<gpx creator="Baptiste Martinet" version="1.1">'
  * After regex: '<(gpx) (creator)="(Baptiste Martinet)" (version)="(1.1)">'
  * After mapping: [ ("tag", "gpx"), ("creator": "Baptiste Martinet"), ("version", "1.1") ]
  * Once mapped, groups are really easy to use:
  * Example: map["creator"] == "Baptiste Martinet"
   */
  private fun createMapFromMatch(matchedResult: MatchResult): Map<String, String> {
    val groups = matchedResult.groups.filterIndexed { index, matchGroup -> (index != 0 && matchGroup != null) }
    val map = mutableMapOf<String, String>()
    if (groups.isEmpty())
      return map
    val groupSize = groups.size
    map["tag"] = groups[0]!!.value
    var i = 1
    while (i < groupSize) {
      if (i + 1 < groupSize) {
        map[groups[i]!!.value] = groups[i + 1]!!.value
        i += 2
      } else {
        map["content"] = groups[i]!!.value
        i++
      }
    }
    return map
  }

  fun readFile(filepath: String): GPXData {
    val data = GPXData()

    val file = File(filepath)
    val lines = file.readLines()

    val reg = Regex("^<(/?[a-z]+)(?:\\s+([a-z]+)=\"([^\"]+)\")?(?:\\s+([a-z]+)=\"([^\"]+)\")?(?:\\s+([a-z]+)=\"([^\"]+)\")?\\s*>(?:(?<content>[^<]*)(?:</[a-z]+>))?\$")
    val openedTags = ArrayDeque<String>()

    for (line in lines) {
      val matchedResult: MatchResult = reg.matchEntire(line) ?: continue
      val groups = createMapFromMatch(matchedResult)

      if (groups.isEmpty())
        continue
      when (groups["tag"]) {
        _tags[TagEnum.GPX] -> {
          if (openedTags.isNotEmpty())
            throw GPXParsingError(TagEnum.GPX, "Tag stack is not empty.")
          openedTags.addFirst(_tags[TagEnum.GPX]!!)
          data.creator = groups["creator"] ?: ""
          data.version = groups["version"] ?: ""
        }
        _tags[TagEnum.CLOSE_GPX] -> {
          if (openedTags.firstOrNull() != _tags[TagEnum.GPX])
            throw GPXParsingError(TagEnum.CLOSE_GPX, "Closing non-opened tag.")
          openedTags.removeFirst()
        }
        _tags[TagEnum.METADATA] -> {
          if (openedTags.firstOrNull() != _tags[TagEnum.GPX])
            throw GPXParsingError(TagEnum.METADATA, "Invalid scope.")
          openedTags.addFirst(_tags[TagEnum.METADATA]!!)
        }
        _tags[TagEnum.CLOSE_METADATA] -> {
          if (openedTags.firstOrNull() != _tags[TagEnum.METADATA])
            throw GPXParsingError(TagEnum.CLOSE_METADATA, "Invalid scope.")
          openedTags.removeFirst()
        }
        _tags[TagEnum.TIME] -> {
          val timeContent: String = groups["content"] ?: throw GPXParsingError(TagEnum.TIME, "Missing content.")
          val timeValue: Long = _dateFormatter.parse(timeContent)?.time ?: throw GPXParsingError(TagEnum.TIME, "Invalid time format.")
          when (openedTags.firstOrNull()) {
            _tags[TagEnum.METADATA] -> data.metadata.time = timeContent
            _tags[TagEnum.TRACK_POINT] -> data.tracks.last().segments.last().last().time = timeValue
            else -> throw GPXParsingError(TagEnum.TIME, "Invalid scope.")
          }
        }
        _tags[TagEnum.TRACK] -> {
          if (openedTags.firstOrNull() != _tags[TagEnum.GPX])
            throw GPXParsingError(TagEnum.TRACK, "Invalid scope.")
          openedTags.addFirst(_tags[TagEnum.TRACK]!!)
          data.tracks.add(GPXTrack())
        }
        _tags[TagEnum.CLOSE_TRACK] -> {
          if (openedTags.firstOrNull() != _tags[TagEnum.TRACK])
            throw GPXParsingError(TagEnum.CLOSE_TRACK, "Invalid scope.")
          openedTags.removeFirst()
        }
        _tags[TagEnum.NAME] -> {
          if (openedTags.firstOrNull() != _tags[TagEnum.TRACK])
            throw GPXParsingError(TagEnum.NAME, "Invalid scope.")
          val nameContent: String = groups["content"] ?: throw GPXParsingError(TagEnum.NAME, "Missing content.")
          data.tracks.last().name = nameContent
        }
        _tags[TagEnum.TRACK_SEGMENT] -> {
          if (openedTags.firstOrNull() != _tags[TagEnum.TRACK])
            throw GPXParsingError(TagEnum.TRACK_SEGMENT, "Invalid scope.")
          data.tracks.last().segments.add(mutableListOf())
          openedTags.addFirst(_tags[TagEnum.TRACK_SEGMENT]!!)
        }
        _tags[TagEnum.CLOSE_TRACK_SEGMENT] -> {
          if (openedTags.firstOrNull() != _tags[TagEnum.TRACK_SEGMENT])
            throw GPXParsingError(TagEnum.CLOSE_TRACK_SEGMENT, "Invalid scope.")
          openedTags.removeFirst()
        }
        _tags[TagEnum.TRACK_POINT] -> {
          if (openedTags.firstOrNull() != _tags[TagEnum.TRACK_SEGMENT])
            throw GPXParsingError(TagEnum.TRACK_POINT, "Invalid scope.")
          openedTags.addFirst(_tags[TagEnum.TRACK_POINT]!!)
          val loc = Location(LocationManager.GPS_PROVIDER)
          loc.latitude = groups["lat"]?.toDouble() ?: 0.0
          loc.longitude = groups["lon"]?.toDouble() ?: 0.0
          data.tracks.last().segments.last().add(loc)
        }
        _tags[TagEnum.ELEVATION] -> {
          if (openedTags.firstOrNull() != _tags[TagEnum.TRACK_POINT])
            throw GPXParsingError(TagEnum.ELEVATION, "Invalid scope.")
          data.tracks.last().segments.last().last().altitude = groups["content"]?.toDouble() ?: 0.0
        }
        _tags[TagEnum.CLOSE_TRACK_POINT] -> {
          if (openedTags.firstOrNull() != _tags[TagEnum.TRACK_POINT])
            throw GPXParsingError(TagEnum.CLOSE_TRACK_POINT, "Invalid scope.")
          openedTags.removeFirst()
        }
        else -> throw GPXParsingError(TagEnum.GPX, "Unknown tag.")
      }
    }
    return data
  }
}