package smy.com.oesrecord

import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import android.view.OrientationEventListener
import android.view.Surface
import android.view.Window
import android.view.WindowManager
import android.widget.TextView
import smy.com.vrplayer.common.VrConstant
import smy.com.vrplayer.view.CustomCardboardView


/**
 * Created by SMY on 2017/11/30.
 */
abstract class AbstractPlayerActivity : Activity() {
    internal lateinit var mVRPlayerView : CustomCardboardView
    internal lateinit var tvMode : TextView
    internal lateinit var tvSensor : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_vr_media)
        initView()
    }

    protected fun initView(){
        mVRPlayerView = findViewById<CustomCardboardView>(R.id.view) as CustomCardboardView
        tvMode = findViewById<TextView>(R.id.mode) as TextView
        tvMode.setOnClickListener{
            when(tvMode.text){
                getString(R.string.render_mode_sphere)->changeMode(VrConstant.RENDER_MODE_PLANET)
                getString(R.string.render_mode_planet)->changeMode(VrConstant.RENDER_MODE_SPHERE_OUTSIDE)
                getString(R.string.render_mode_plane)->changeMode(VrConstant.RENDER_MODE_OVERALL)
                getString(R.string.render_mode_overall)->changeMode(VrConstant.RENDER_MODE_SPHERE)
            }
        }
        findViewById<TextView>(R.id.vr).setOnClickListener { changeMode(VrConstant.RENDER_MODE_VR) }
        tvSensor = findViewById<TextView>(R.id.sensor) as TextView
        tvSensor.setOnClickListener {
            when(tvSensor.text){
                getString(R.string.enable_sensor)->{
                    tvSensor.text = getString(R.string.disable_sensor)
                    mVRPlayerView.changeSensor(true)
                }
                getString(R.string.disable_sensor)->{
                    tvSensor.text = getString(R.string.enable_sensor)
                    mVRPlayerView.changeSensor(false)
                }
            }
        }
        mOrientationEventListener = OrientationDetector(this)
    }

    private fun changeMode(mode: Int){
        val oldMode = mVRPlayerView.customRenderMode
        if (oldMode != mode){
            mVRPlayerView.customRenderMode = mode
            mVRPlayerView.resetCameraOrientation()
            when(mode){
                VrConstant.RENDER_MODE_SPHERE->tvMode.text = getString(R.string.render_mode_sphere)
                VrConstant.RENDER_MODE_PLANET->tvMode.text = getString(R.string.render_mode_planet)
                VrConstant.RENDER_MODE_SPHERE_OUTSIDE->tvMode.text = getString(R.string.render_mode_plane)
                VrConstant.RENDER_MODE_OVERALL->tvMode.text = getString(R.string.render_mode_overall)
                VrConstant.RENDER_MODE_VR->requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            }
        }
    }

    override fun onBackPressed() {
        if (mVRPlayerView.customRenderMode == VrConstant.RENDER_MODE_VR){
            changeMode(VrConstant.RENDER_MODE_SPHERE)
        } else {
            finish()
        }
    }

    internal var mOrientationEventListener: OrientationEventListener? = null
    internal var mCurrentLandScreen = false

    internal inner class OrientationDetector(context: Context?) : OrientationEventListener(context) {
        private var mOrientation : Int ?= Surface.ROTATION_0

        override fun onOrientationChanged(orientation: Int) {
            if (mVRPlayerView.getCustomRenderMode() === VrConstant.RENDER_MODE_VR) {
                return
            }
            if (orientation == OrientationEventListener.ORIENTATION_UNKNOWN) {
                return
            }
            if (orientation > 350 || orientation < 10) { //0度
                mOrientation = Surface.ROTATION_0
            } else if (orientation > 80 && orientation < 100) { //90度
                mOrientation = Surface.ROTATION_90
            } else if (orientation > 170 && orientation < 190) { //180度
                mOrientation = Surface.ROTATION_180
            } else if (orientation > 260 && orientation < 280) { //270度
                mOrientation = Surface.ROTATION_270
            }

            //恢复自动旋转
            if ((mOrientation == Surface.ROTATION_0 || mOrientation == Surface.ROTATION_180) && mCurrentLandScreen) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
            } else if ((mOrientation == Surface.ROTATION_90 || mOrientation == Surface.ROTATION_270) && !mCurrentLandScreen) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        mCurrentLandScreen = newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE
        mVRPlayerView.onOrientationChanged()
    }

    override fun onResume() {
        super.onResume()
        mOrientationEventListener!!.enable()
    }

    override fun onPause() {
        super.onPause()
        mOrientationEventListener!!.disable()
    }

}