package com.example.smy.choosepic;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends Activity {

    public static final int TAKE_PHOTO = 1;
    public static final int CROP_PHOTO = 2;

    @Bind(R.id.picture)
    ImageView picture;

    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        picture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final PictureChoosingDialogFragment dialogFragment = new PictureChoosingDialogFragment();
                dialogFragment.setOnOptionClickListener(new PictureChoosingDialogFragment.onOptionClickListener() {
                    @Override
                    public void onTakePictureClick(DialogFragment paramDialogFragment) {
                        takePhotoClick();
                    }

                    @Override
                    public void onPickAlbumClick(DialogFragment paramDialogFragment) {
                        pickAlbumClick();
                    }

                    @Override
                    public void onCancelClick(DialogFragment paramDialogFragment) {
                    }

                    @Override
                    public void onEmptyAreaClick(DialogFragment paramDialogFragment) {
                    }
                });
                dialogFragment.show(getFragmentManager(),"PictureChoosingDialog");
            }
        });

    }

    private void takePhotoClick()
    {
        File outputImage = new File(Environment.getExternalStorageDirectory(), "tempImage.jpg");
        try {
            if (outputImage.exists()) {
                outputImage.delete();
            }
            outputImage.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        imageUri = Uri.fromFile(outputImage);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, TAKE_PHOTO);
    }

    private void pickAlbumClick()
    {
        File outputImage = new File(Environment.getExternalStorageDirectory(), "outputImage.jpg");
        try {
            if (outputImage.exists()) {
                outputImage.delete();
            }
            outputImage.createNewFile();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        imageUri = Uri.fromFile(outputImage);
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null);
        intent.setType("image/*");
        intent.putExtra("crop", true);
        intent.putExtra("scale", true);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, TAKE_PHOTO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null) {
            imageUri = data.getData();
        }
        switch (requestCode) {
        case TAKE_PHOTO:
            if (resultCode == RESULT_OK) {
                Intent intent = new Intent("com.android.camera.action.CROP");
                intent.setDataAndType(imageUri, "image/*");
                intent.putExtra("scale", true);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(intent, CROP_PHOTO);
            }
            break;
        case CROP_PHOTO:
            if (resultCode == RESULT_OK) {
                try {
                    Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                    picture.setImageBitmap(bitmap);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
            break;
        default:
            break;
    }
    }

}
