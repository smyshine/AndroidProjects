package smy.com.screencapture

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        screenShot.setOnClickListener(View.OnClickListener {
            startActivity(Intent(this, ScreenShotActivity::class.java))
        })
    }
}
