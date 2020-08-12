package cn.com.chaochuang.pdf_operation.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;

/**
 * Created by Shicx on 2020/8/11.
 */
public class FileUtil {


    /**
     * 获取文件的MD5码
     *
     * @param file 要获取MD5码的文件
     * @return
     * @throws IOException
     */
    public static String getFileMd5Code(File file){

        if(file==null||!file.exists()){
            return null;
        }

        MessageDigest md;
        FileInputStream in = null;
        try {

            md = MessageDigest.getInstance("MD5");
            in = new FileInputStream(file);

            byte[] buffer = new byte[8192];
            int length;
            while ((length = in.read(buffer)) != -1) {
                md.update(buffer, 0, length);
            }

            return bufferToHex(md.digest());
        } catch (java.security.NoSuchAlgorithmException ex) {
            throw new AssertionError(ex);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(in!=null){
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    /**
     * 算出字节数组的MD5码
     *
     * @param bytes
     * @return
     */
    private static String bufferToHex(byte bytes[]) {
        int m = 0, n = bytes.length;
        StringBuffer stringbuffer = new StringBuffer(2 * n);
        int k = m + n;
        for (int l = m; l < k; l++) {
            appendHexPair(bytes[l], stringbuffer);
        }
        return stringbuffer.toString();
    }

    /**
     * @param bt
     * @param stringbuffer
     */
    private static void appendHexPair(byte bt, StringBuffer stringbuffer) {
        char hexDigits[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

        char c0 = hexDigits[(bt & 0xf0) >> 4];
        char c1 = hexDigits[bt & 0xf];
        stringbuffer.append(c0);
        stringbuffer.append(c1);
    }

}
