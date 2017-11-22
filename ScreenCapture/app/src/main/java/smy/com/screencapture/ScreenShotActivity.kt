package smy.com.screencapture

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.View.VISIBLE
import android.view.Window
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_screen_shot.*
import java.io.File

class ScreenShotActivity : Activity() , View.OnClickListener{

    companion object {
        var REQUEST_MEDIA_PROJECTION = 0x1092
    }

    private var mShotter : ScreenShotter? = null
    private var imagePath : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT))
        window.setDimAmount(0f)

        setContentView(R.layout.activity_screen_shot)

        okay.setOnClickListener(this)

        setFullScreen()

        requestScreenShot()
    }

    private fun setFullScreen(){
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN
    }

    private fun requestScreenShot(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            startActivityForResult(
                    (getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager).createScreenCaptureIntent(),
                    REQUEST_MEDIA_PROJECTION)
        } else {
            Toast.makeText(this, "Too low to shot", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode){
            REQUEST_MEDIA_PROJECTION -> {
                if (resultCode == Activity.RESULT_OK && data != null){
                    setFullScreen()
                    mShotter = ScreenShotter(this, data,
                            ScreenUtil.getScreenRealWidth(this), ScreenUtil.getScreenRealHeight(this))
                    mShotter!!.startScreenShot { path -> handleScreenShotResult(path) }
                }
            }
        }
    }

    private fun handleScreenShotResult(path: String){
        window.setDimAmount(1.0f)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
        llShotResult!!.visibility = VISIBLE
        imagePath = path
        ivImage!!.setImageURI(Uri.fromFile(File(path)))
    }

    override fun onClick(p0: View) {
        when(p0.id){
            R.id.okay -> {
                finish()
            }
        }
    }
}
