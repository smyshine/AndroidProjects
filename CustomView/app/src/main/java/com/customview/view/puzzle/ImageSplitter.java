package com.customview.view.puzzle;

import android.graphics.Bitmap;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by SMY on 2017/10/24.
 */

public class ImageSplitter {

    public static List<ImagePiece> split(Bitmap bitmap, int piece){
        List<ImagePiece> pieces = new ArrayList<>(piece * piece);
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int pieceWidth = Math.min(width, height) / piece;

        for (int i = 0; i < piece; ++i){
            for (int j = 0; j < piece; j++) {
                ImagePiece imagePiece = new ImagePiece();
                imagePiece.index = j + i * piece;
                int x = j * pieceWidth;
                int y = i * pieceWidth;
                imagePiece.bitmap = Bitmap.createBitmap(bitmap, x, y, pieceWidth, pieceWidth);
                pieces.add(imagePiece);
            }
        }
        return pieces;
    }
}
