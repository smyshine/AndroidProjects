package com.panostitch;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.Stitcher;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Example of a call to a native method
        TextView tv = (TextView) findViewById(R.id.sample_text);

        final CombineParams params = new CombineParams(5760, 2880, 16,
                "{\"rval\":0,\"msg_id\":268505089,\"fisheyeImgW\":2880,\"fisheyeImgH\":2880,\"panoImgW\":5760,\"panoImgH\":2880,\"sphereRadius\":2800,\"maxFovAngle\":\"98.0000000000000000\"}",
                "{\"rval\":0,\"msg_id\":268505090,\"uc\":\"1447.3041754448104257\",\"vc\":\"1439.8961357116224917\",\"length_pol\":5,\"pol\":\"-776.9650262430000112,-0.0922230273954000,0.0007974384239300,-0.0000006743560240,0.0000000003614159\",\"length_invpol\":10,\"invpol\":\"1300.3511077299999670,767.1975029760000098,-175.9097791579999921,-7.1328483572700003,100.9909220299999930,-75.0764391447000037,-94.9144339457999990,33.8976894000999991,53.2546931298999979,13.2249842111000007\",\"height\":2880,\"width\":2880,\"c\":\"1.0000000000000000\",\"d\":\"0.0000000000000000\",\"e\":\"0.0000000000000000\",\"vcf_factors\":\"0.0000000000000000,0.0000000000000000,0.0000000000000000,0.0000000000000000,0.0000000000000000,0.0000000000000000,0.0000000000000000,0.0000000000000000,0.0000000000000000\"}",
                "{\"rval\":0,\"msg_id\":268505091,\"uc\":\"1430.1821466314729605\",\"vc\":\"1443.0638894081096169\",\"length_pol\":5,\"pol\":\"-774.7932577049999736,-0.1172918121930000,0.0008767516423950,-0.0000007674317175,0.0000000003974767\",\"length_invpol\":10,\"invpol\":\"1299.6351369799999702,761.6288693420000300,-195.8057029390000139,-38.3330942863000033,92.8527647667999929,-54.3929510401000016,-87.9178461282999990,18.2959752866999992,40.8469867989999997,10.5774977230000005\",\"height\":2880,\"width\":2880,\"c\":\"1.0000000000000000\",\"d\":\"0.0000000000000000\",\"e\":\"0.0000000000000000\",\"vcf_factors\":\"0.0000000000000000,0.0000000000000000,0.0000000000000000,0.0000000000000000,0.0000000000000000,0.0000000000000000,0.0000000000000000,0.0000000000000000,0.0000000000000000\"}",
                "{\"rval\":0,\"msg_id\":268505092,\"rotationMtx\":\"-0.0000000000000001,-1.0000000000000000,0.0000000000000000,1.0000000000000000,-0.0000000000000001,-0.0000000000000001,0.0000000000000001,-0.0000000000000000,1.0000000000000000\",\"translateVec\":\"-0.0000000000000000,0.0000000000000000,21.0000000000000000\"}",
                "{\"rval\":0,\"msg_id\":268505093,\"rotationMtx\":\"0.0019478824362200,-0.9999780779470000,-0.0063247318713400,-0.9999964893240000,-0.0019591074190500,0.0017690763476200,-0.0017814283952100,0.0063212636763000,-0.9999784445530000\",\"translateVec\":\"0.1328193692980000,-0.0371506033000000,20.9995473355999991\"}");
        prepareDatFile(datPath, R.raw.normal_blend_mask);
        prepareDatFile(inPath, R.raw.stitch_in);

        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Stitcher.getInstance().imageStitch(
                        inPath,
                        "/storage/emulated/0/vrsmy/stitch-out.jpg",
                        params, datPath
                );
            }
        });
    }

    String inPath = "/storage/emulated/0/vrsmy/stich-in.jpg";
    String datPath = "/storage/emulated/0/vrsmy/normal_blend_mask.dat";
    private void prepareDatFile(String path, int rid){
        File bigPath = new File("/storage/emulated/0/vrsmy/");
        if (!bigPath.exists()){
            bigPath.mkdir();
        }

        File datFile = new File(path);
        if (datFile.exists()) {
            return;
        }

        InputStream inputStream = getResources().openRawResource(rid);
        OutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(datFile);
            byte buffer[] = new byte[1024];
            int len = 0;
            while ((len = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, len);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
