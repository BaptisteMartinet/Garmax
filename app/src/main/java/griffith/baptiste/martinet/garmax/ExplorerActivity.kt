package griffith.baptiste.martinet.garmax

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.io.File

class ExplorerActivity : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_explorer)

    val externalStoragePath: File = this.getExternalFilesDir(null) ?: throw Exception("Could not get externalStoragePath")
    val explorerFiles = File(externalStoragePath, "/GPStracks").walk().filter { it.isFile && it.extension == "gpx" }.map { RecyclerExplorerAdapter.ExplorerFile(it.name, it.absolutePath, it.lastModified()) }
    val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewExplorer)
    recyclerView.adapter = RecyclerExplorerAdapter(explorerFiles.toList())

    val storagePathText = findViewById<TextView>(R.id.storagePathText)
    storagePathText.text = getString(R.string.storage_path_placeholder).format(externalStoragePath.path + "/GPStracks")
    // TODO refactor and handle back button
  }
}
