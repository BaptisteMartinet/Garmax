package griffith.baptiste.martinet.garmax

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import java.io.File

class ExplorerActivity : AppCompatActivity() {
  private val _directoryName: String = "GPStracks"
  private val _extensionName: String = "gpx"

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_explorer)

    val backBtnExplorer = findViewById<ImageButton>(R.id.backBtnExplorer)
    backBtnExplorer.setOnClickListener {
      this.finish()
    }

    try {
        fillRecyclerView()
    } catch (e: Exception) {
      Toast.makeText(this, "Unable to load past activity from external storage", Toast.LENGTH_SHORT).show()
    }
  }

  private fun fillRecyclerView() {
    val externalStoragePath: File = this.getExternalFilesDir(null) ?: throw Exception("Could not get externalStoragePath")
    val explorerFiles = File(externalStoragePath, _directoryName).walk().filter { it.isFile && it.extension == _extensionName }.map { RecyclerExplorerAdapter.ExplorerFile(it.name, it.absolutePath, it.lastModified()) }
    val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewExplorer)
    recyclerView.adapter = RecyclerExplorerAdapter(this, explorerFiles.toList())

    val storagePathText = findViewById<TextView>(R.id.storagePathText)
    storagePathText.text = getString(R.string.storage_path_placeholder).format("${externalStoragePath.path}/$_directoryName")
  }
}
