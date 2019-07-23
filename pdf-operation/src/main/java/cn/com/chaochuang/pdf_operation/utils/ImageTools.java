package cn.com.chaochuang.pdf_operation.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.support.annotation.NonNull;
import android.util.Base64;
import com.github.barteksc.pdfviewer.model.HandwritingData;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.pdmodel.PDPage;
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream;
import com.tom_roush.pdfbox.pdmodel.graphics.image.LosslessFactory;
import com.tom_roush.pdfbox.pdmodel.graphics.image.PDImageXObject;

import java.io.*;

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
    public static void bitmap2File(@NonNull Bitmap bitmap, @NonNull String filePath) {
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

    public static Bitmap file2Bitmap(@NonNull String filePath){
        return BitmapFactory.decodeFile(filePath);
    }

    public static Bitmap scaleBitmap(Bitmap bitmap,float scale){
        if (bitmap!=null) {
            Matrix matrix = new Matrix();
            matrix.setScale(scale, scale);
            return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        }
        return null;
    }

    /**
     * 将图片保存到pdf文件
     * @param filePath
     * @param data
     * @return
     */
    public static boolean insertPdfBitmap(String filePath, HandwritingData data){
        PDDocument doc = null;
        try {
            //使用pdf-box库对PDF进行插入图片操作
            File file = new File(filePath);
            doc = PDDocument.load(file);
            PDPage page = doc.getPage(data.getPageNo());
            PDImageXObject pdImage = LosslessFactory.createFromImage(doc, data.getImageBitmap());
            PDPageContentStream contentStream = new PDPageContentStream(doc, page, true, false);
            //pdf坐标系的Y轴坐标
            float pdfY = data.getPdfFileHeight()-data.getPy()-data.getImageHeight();
            //计算实际pdf的坐标值
            contentStream.drawImage(pdImage, data.getPx(),  pdfY, data.getImageWidth(), data.getImageHeight());
            contentStream.close();
            doc.save(file);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(doc!=null){
                try {
                    doc.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }


    /**
     * bitmap转为base64
     * @param bitmap
     * @return
     */
    public static String bitmapToBase64(Bitmap bitmap) {
        String result = null;
        ByteArrayOutputStream baos = null;
        try {
            if (bitmap != null) {
                baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);

                baos.flush();
                baos.close();

                byte[] bitmapBytes = baos.toByteArray();
                result = Base64.encodeToString(bitmapBytes, Base64.URL_SAFE);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (baos != null) {
                    baos.flush();
                    baos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * base64转为bitmap
     * @return
     */
    public static Bitmap base64ToBitmap(String base64Data) {
        byte[] bytes = Base64.decode(base64Data, Base64.URL_SAFE);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
}

