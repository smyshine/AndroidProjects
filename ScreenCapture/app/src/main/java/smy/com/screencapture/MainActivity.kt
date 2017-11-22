package smy.com.screencapture

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        screenShot.setOnClickListener(this)
        startRecord.setOnClickListener(this)
        stopRecord.setOnClickListener(this)
    }

    override fun onClick(p0: View?) {
        when(p0!!.id){
            R.id.screenShot -> startShot()
            R.id.startRecord -> {
                isRecord = true
                startShot()
            }
            R.id.stopRecord -> stopShot()
        }
    }

    private var isRecord : Boolean = false
    private var mShotter : ScreenShotter? = null

    private fun startShot(){
        requestScreenShot()
    }

    private fun stopShot(){
        if (isRecord){
            mShotter!!.stopRecord()
            isRecord = false
        }
    }

    private fun requestScreenShot(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            startActivityForResult(
                    (getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager).createScreenCaptureIntent(),
                    ScreenShotActivity.REQUEST_MEDIA_PROJECTION)
        } else {
            Toast.makeText(this, "Too low to shot", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode){
            ScreenShotActivity.REQUEST_MEDIA_PROJECTION -> {
                if (resultCode == Activity.RESULT_OK && data != null){
                    mShotter = ScreenShotter(this, data,
                            ScreenUtil.getScreenRealWidth(this), ScreenUtil.getScreenRealHeight(this))
                    if (isRecord){
                        mShotter!!.startScreenRecord { path -> handleScreenShotResult(path) }
                        moveTaskToBack(true)
                    } else {
                        mShotter!!.startScreenShot { path -> handleScreenShotResult(path) }
                    }
                }
            }
        }
    }

    private fun handleScreenShotResult(path: String){
        runOnUiThread(Runnable { tvResult.text = path })
    }
}
