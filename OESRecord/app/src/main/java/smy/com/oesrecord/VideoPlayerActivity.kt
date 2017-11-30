package smy.com.oesrecord

import android.os.Bundle
import smy.com.vrplayer.render.VRVideoRender
import smy.com.vrplayer.view.CustomCardboardView

class VideoPlayerActivity : AbstractPlayerActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var isPano = intent.getBooleanExtra(MainActivity.IS_PANO, false) as Boolean
        var useIjk = intent.getBooleanExtra(MainActivity.USE_IJK, false) as Boolean

        mVRPlayerView.initRender(if (isPano) CustomCardboardView.RenderType.VRVideo else CustomCardboardView.RenderType.DualVRVideo,
                if (useIjk) VRVideoRender.IjkMediaPlayer else VRVideoRender.AndroidMediaPlayer, this)
        mVRPlayerView.setDataSource(intent.getStringExtra(MainActivity.MEDIA_PATH))
    }
}
