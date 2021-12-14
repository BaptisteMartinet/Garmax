package griffith.baptiste.martinet.garmax

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class RecyclerExplorerAdapter(private val dataSet: List<ExplorerFile>) : RecyclerView.Adapter<RecyclerExplorerAdapter.ViewHolder>() {
  class ExplorerFile(val filename: String, val absolutePath: String, val lastModified: Long)

  class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val recyclerExplorerFileNameText: TextView = view.findViewById(R.id.recyclerExplorerFileNameText)
    val recyclerExplorerLastModifiedText: TextView = view.findViewById(R.id.recyclerExplorerLastModifiedText)
  }

  private val _dateFormatter = SimpleDateFormat("dd/MM/yyyy h:mm a", Locale.getDefault())

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
    val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_explorer_item, parent, false)
    return ViewHolder(view)
  }

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    val file = dataSet[position]
    holder.recyclerExplorerFileNameText.text = file.filename
    holder.recyclerExplorerLastModifiedText.text = _dateFormatter.format(Date(file.lastModified))
    // TODO handle click
  }

  override fun getItemCount(): Int = dataSet.count()
}