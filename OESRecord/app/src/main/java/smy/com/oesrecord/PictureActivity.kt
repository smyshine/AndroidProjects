package smy.com.oesrecord

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.provider.MediaStore
import android.widget.Toast
import smy.com.vrplayer.render.VRVideoRender
import smy.com.vrplayer.view.CustomCardboardView
import java.io.IOException

class PictureActivity : AbstractPlayerActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mVRPlayerView.initRender(CustomCardboardView.RenderType.VRPicture,
                VRVideoRender.AndroidMediaPlayer, this)
        loadBitmap(intent.getStringExtra(MainActivity.MEDIA_PATH))
    }

    /*var LOAD_SUCCESS = 0
    var LOAD_FAIL = 1
    var handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            when(msg.what){
                LOAD_SUCCESS->mVRPlayerView.setDataSource(mPicture)
                LOAD_FAIL->Toast.makeText(this@PictureActivity, "load fail", Toast.LENGTH_SHORT).show()
            }
        }
    }

    var mPicture : Bitmap ?= null
    private fun loadBitmap(url : String){
        object : Thread(){
            override fun run() {
                mPicture = getBitmapFromFile(url)
                if (mPicture == null){
                    handler.sendEmptyMessage(LOAD_FAIL)
                } else {
                    handler.sendEmptyMessage(LOAD_SUCCESS)
                }
            }
        }
    }

    private fun getBitmapFromFile(path : String) : Bitmap {
        if (path.startsWith("android.resource://")){
            try {
                return MediaStore.Images.Media.getBitmap(contentResolver, Uri.parse(path))
            } catch (e : IOException){
                e.printStackTrace()
            }
        }
        val options = BitmapFactory.Options()
        options.inPreferredConfig = Bitmap.Config.ARGB_8888
        return BitmapFactory.decodeFile(path, options)
    }*/

    private val DOWNLOAD_PICTURE_SUCCESS = 0
    private val DOWNLOAD_PICTURE_FAILED = 1
    private var mPicture: Bitmap? = null
    private fun loadBitmap(url: String) {
        object : Thread() {
            override fun run() {
                mPicture = getBitmapFromFile(url)
                if (mPicture == null) {
                    handler.sendEmptyMessage(DOWNLOAD_PICTURE_FAILED)
                    return
                }
                handler.sendEmptyMessage(DOWNLOAD_PICTURE_SUCCESS)
            }
        }.start()
    }

    fun getBitmapFromFile(path: String): Bitmap {
        if (path.startsWith("android.resource://")) {
            try {
                return MediaStore.Images.Media.getBitmap(this.contentResolver, Uri.parse(path))
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }

        val options = BitmapFactory.Options()
        options.inPreferredConfig = Bitmap.Config.ARGB_8888
        return BitmapFactory.decodeFile(path, options)
    }

    private val handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                DOWNLOAD_PICTURE_SUCCESS -> mVRPlayerView.setDataSource(mPicture)
                DOWNLOAD_PICTURE_FAILED -> Toast.makeText(this@PictureActivity, "load fail!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
