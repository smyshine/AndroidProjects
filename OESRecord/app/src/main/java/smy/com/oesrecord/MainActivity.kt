package smy.com.oesrecord

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import java.io.File
import java.net.URI

class MainActivity : AppCompatActivity() {

    companion object {
        var MEDIA_PATH = "media_path"
        var IS_PANO = "is_pano"
        var USE_IJK = "use_ijk"
    }

    private var edtPath: EditText? = null
    private var cbPlayer: CheckBox? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        cbPlayer = findViewById<CheckBox>(R.id.cbPlayer) as CheckBox
        edtPath = findViewById<EditText>(R.id.edt_path) as EditText
        edtPath?.setText("android.resource://" + packageName + "/" + R.raw.pano_video)
    }

    fun onPanoVideoClick(v: View) {
        val path = edtPath?.text.toString()
        Toast.makeText(applicationContext, path, Toast.LENGTH_SHORT).show()
        val intent = Intent(this@MainActivity, VideoPlayerActivity::class.java)
        intent.putExtra(MEDIA_PATH, path)
        intent.putExtra(IS_PANO, true)
        intent.putExtra(USE_IJK, cbPlayer?.isChecked)
        startActivity(intent)
    }

    fun onDualVideoClick(v: View) {
        val path = edtPath?.getText().toString()
        Toast.makeText(applicationContext, path, Toast.LENGTH_SHORT).show()
        val intent = Intent(this@MainActivity, VideoPlayerActivity::class.java)
        intent.putExtra(MEDIA_PATH, path)
        intent.putExtra(IS_PANO, false)
        intent.putExtra(USE_IJK, cbPlayer?.isChecked)
        startActivity(intent)
    }

    fun onPictureClick(v: View) {
        val path = edtPath?.text.toString()
        Toast.makeText(applicationContext, path, Toast.LENGTH_SHORT).show()
        val intent = Intent(this@MainActivity, PictureActivity::class.java)
        intent.putExtra(MEDIA_PATH, path)
        startActivity(intent)
    }

    fun onExampleClick(v: View) {
        when (v.id) {
            R.id.panoVideo -> edtPath?.setText("android.resource://" + packageName + "/" + R.raw.pano_video)
            R.id.dualVideo -> edtPath?.setText("android.resource://" + packageName + "/" + R.raw.dual_video)
            R.id.panoPicture -> edtPath?.setText("android.resource://" + packageName + "/" + R.raw.pano_picture)
            R.id.dualPicture -> edtPath?.setText("android.resource://" + packageName + "/" + R.raw.dual_picture)
        }
    }

    fun onBtnChooseFileClick(v: View) {
        showFileChooser()
    }

    private val FILE_SELECT_CODE = 1
    //file choose
    private fun showFileChooser() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "*/*"
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        try {
            startActivityForResult(Intent.createChooser(intent, "Please Choose a Video File"), FILE_SELECT_CODE)
        } catch (ex: android.content.ActivityNotFoundException) {
            ex.printStackTrace()
        }

    }


    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if (requestCode == FILE_SELECT_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                val uri = data.data
                if (uri != null) {
                    var path: String? = uri.toString()
                    if (path!!.toLowerCase().startsWith("file://")) {
                        // Selected file/directory path is below
                        path = File(URI.create(path)).absolutePath
                    } else {
                        path = getImagePath(uri, null)
                    }
                    edtPath?.setText(path)
                }
            }
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun getImagePath(uri: Uri, selection: String?): String? {
        var path: String? = null
        val cursor = contentResolver.query(uri, null, selection, null, null)
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA))
            }

            cursor.close()
        }
        return path
    }
}
