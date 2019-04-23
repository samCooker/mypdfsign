package cn.com.chaochuang.pdf_operation;

import android.graphics.Bitmap;
import android.util.Log;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Image;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;

import java.io.*;

/**
 * 2019-3-24
 *
 * @author Shicx
 */

public class SavePdfUtil {

    public static void insertImage(String pdfPath, String outputPath, Bitmap bitmap, int pageNo, float zoom, float x, float y) {
        try {

            byte[] imageByte = bitmap2Bytes(bitmap);

            PdfReader reader = new PdfReader(pdfPath);///打开要写入的PDF
            FileOutputStream outputStream = new FileOutputStream(outputPath);//设置涂鸦后的PDF
            PdfStamper stamp = new PdfStamper(reader, outputStream);
            PdfContentByte over = stamp.getOverContent(pageNo);
            Image img = Image.getInstance(imageByte);
            Rectangle rectangle = reader.getPageSize(pageNo);
            float radio = rectangle.getHeight()/bitmap.getHeight();
            //相对于左下角 缩小
            img.scaleAbsolute(bitmap.getWidth()*radio/zoom ,rectangle.getHeight()/zoom);
            img.setAbsolutePosition(x*radio/zoom,((bitmap.getHeight()*zoom-y-bitmap.getHeight())/zoom)*radio);


            Log.d("signaturePad","zoom:"+ (bitmap.getWidth() - rectangle.getWidth()/radio));
            Log.d("signaturePad","x:"+x);
            Log.d("signaturePad","y:"+y);
            Log.d("signaturePad","rectangle w:"+rectangle.getWidth());
            Log.d("signaturePad","rectangle h:"+rectangle.getHeight());
            Log.d("signaturePad","bitmap w:"+bitmap.getWidth());
            Log.d("signaturePad","bitmap h:"+bitmap.getHeight());
            Log.d("signaturePad",":"+rectangle.getWidth()/bitmap.getWidth());
            Log.d("signaturePad",":"+rectangle.getHeight()/bitmap.getHeight());

            over.addImage(img);
            stamp.close();
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }

    /**
     * 将BitMap转换为Bytes
     *
     * @param bm
     * @return
     */
    public static byte[] bitmap2Bytes(Bitmap bm) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }

    public static void saveBitmapFile(Bitmap bitmap,String savePath){
        //将要保存图片的路径
        File file=new File(savePath);
        try {
            if(!file.exists()){
                file.createNewFile();
            }
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            bos.flush();
            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
