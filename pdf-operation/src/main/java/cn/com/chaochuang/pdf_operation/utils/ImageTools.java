package cn.com.chaochuang.pdf_operation.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 2019-7-3
 *
 * @author Shicx
 */
public class ImageTools {

    /**
     * @param bitmap
     * @param filePath
     */
    public static void bitmap2File(Bitmap bitmap, String filePath) {
        File file = new File(filePath);
        BufferedOutputStream bos = null;
        try {
            bos = new BufferedOutputStream(new FileOutputStream(file));
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            bos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static Bitmap file2Bitmap(String filePath){
        return BitmapFactory.decodeFile(filePath);
    }
}

