package com.editimagexmp;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.net.URI;

public class MainActivity extends Activity {

    static {
        System.loadLibrary("yiExiv2");
    }

    public native void setImageGPano(String path, int width, int height);

    private EditText edtPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        edtPath = (EditText)findViewById(R.id.edt_path);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == FILE_SELECT_CODE)
        {
            if (resultCode == RESULT_OK)
            {
                Uri uri = data.getData();
                if (uri != null)
                {
                    String path = uri.toString();
                    if (path.toLowerCase().startsWith("file://"))
                    {
                        // Selected file/directory path is below
                        path = (new File(URI.create(path))).getAbsolutePath();
                    } else {
                        path = getImagePath(uri, null);
                    }
                    edtPath.setText(path);
                }
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    public void onBtnChooseFileClick(View v)
    {
        showFileChooser();
    }

    private String getImagePath(Uri uri, String selection) {
        String path = null;
        Cursor cursor = getContentResolver().query(uri, null, selection, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }

            cursor.close();
        }
        return path;
    }

    private static final int FILE_SELECT_CODE = 1;
    //file choose
    private void showFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        try {
            startActivityForResult(Intent.createChooser(intent, "Please Choose a Image File"), FILE_SELECT_CODE);
        } catch (android.content.ActivityNotFoundException ex) {
            ex.printStackTrace();
        }
    }


    public void onBtnEditClick(View v){
        String path = edtPath.getText().toString();
        if (TextUtils.isEmpty(path)){
            Toast.makeText(getApplicationContext(), "invalid path!", Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(getApplicationContext(), path, Toast.LENGTH_SHORT).show();
        editPcitureXmpGPanoInfo(path);
    }

    public void editPcitureXmpGPanoInfo(String path){
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        Bitmap bmp = BitmapFactory.decodeFile(path, options);
        setImageGPano(path, options.outWidth, options.outHeight);
    }
}
