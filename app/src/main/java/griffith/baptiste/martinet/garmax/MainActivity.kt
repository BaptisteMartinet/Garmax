package griffith.baptiste.martinet.garmax

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class MainActivity : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    val trackerBtn = findViewById<Button>(R.id.trackerBtn)
    trackerBtn.setOnClickListener {
      // TODO start tracking & stats activity
    }
  }
}