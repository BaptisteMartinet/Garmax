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
  private var _file: File? = null
  private val _directoryName: String = "GPStracks"
  private val _extensionName: String = ".gpx"
  private val _dateFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")

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
}