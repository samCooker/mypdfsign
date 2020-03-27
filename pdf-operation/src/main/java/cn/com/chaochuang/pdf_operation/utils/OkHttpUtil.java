package cn.com.chaochuang.pdf_operation.utils;

import android.graphics.Bitmap;
import android.util.Log;
import cn.com.chaochuang.writingpen.model.CommentData;
import com.chaochuang.oa.dataaec.util.AesTool;
import okhttp3.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 2018-5-9
 *
 * @author Shicx
 */

public class OkHttpUtil {

    //设置连接超时时间
    public final static int CONNECT_TIMEOUT =40;
    //设置读取超时时间
    public final static int READ_TIMEOUT=40;
    //设置写的超时时间
    public final static int WRITE_TIMEOUT=30;

    public static final MediaType CONTENT_TYPE = MediaType.parse("application/x-www-form-urlencoded; charset=utf-8");
    private OkHttpClient client;
    private Boolean isEncoding;
    private String serverToken;

    private List<CommentData> handwritingList;
    private List<CommentData> textDataList;

    public OkHttpUtil(boolean encodeFlag,String token){
        this.isEncoding = encodeFlag;
        this.serverToken = token;
        client = new OkHttpClient().newBuilder().readTimeout(READ_TIMEOUT, TimeUnit.SECONDS).writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS).connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS).addInterceptor(new DecodeInterceptor()).build();
    }

    /**
     *
     * @param url
     * @param postData 格式 param1=value1&param2&value2...
     * @param callBack
     */
    public void post(String url, String postData, Callback callBack) {

        //加密参数
        String params = encodePostData(postData);
        if(isEncoding){
            params = "data="+params+"&_encodeFlag=true";
        }
        RequestBody body = RequestBody.create(CONTENT_TYPE, params);
        Request request = new Request.Builder()
                .url(url)
                .header(Constants.HEADER_TOKEN_NAME,serverToken)
                .post(body)
                .build();
        client.newCall(request).enqueue(callBack);
    }

    private String encodePostData(String postData) {
        Log.d("OkHttpUtil",isEncoding+"");
        if(!isEncoding){
            return postData;
        }
        //在token中加入的参数
        long ts = new Date().getTime();

        if (postData == null || "".equals(postData)) {
            postData = "ts=" + ts;
        } else {
            postData = postData + "&ts=" + ts;
        }
        postData = AesTool.encode(postData);
        try {
            return URLEncoder.encode(postData,"utf8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void get(String url, Callback callBack){
        int i = url.indexOf("?");
        String postData = "";
        if(i!=-1){
            String[] array = url.split("\\?");
            url = array[0];
            postData = array[1];
        }
        String params = encodePostData(postData);
        if(isEncoding){
            url = url + "?data="+params+"&_encodeFlag=true";
        }else{
            url = url + "?"+params;
        }
        Request request = new Request.Builder()
                .url(url)
                .header(Constants.HEADER_TOKEN_NAME,serverToken)
                .build();
        client.newCall(request).enqueue(callBack);
    }

    public void setHandwritingList(List<CommentData> commentDataList){
        if(commentDataList!=null){
            for (CommentData commentData:commentDataList){
                if(CommentData.TYPE_HANDWRITING.equals(commentData.getSignType())){
                    if(commentData.getImageBitmap()==null&&commentData.getSignContent()!=null&&!"".equals(commentData.getSignContent().trim())){
                        Bitmap bitmap = ImageTools.base64ToBitmap(commentData.getSignContent());
                        commentData.setImageBitmap(bitmap);
                    }
                }
            }
        }
        this.handwritingList = commentDataList;
    }

    public List<CommentData> getHandwritingList() {
        return handwritingList;
    }

    public List<CommentData> getTextDataList() {
        return textDataList;
    }

    public void setTextDataList(List<CommentData> textDataList) {
        this.textDataList = textDataList;
    }


    /**
     * 获取文件的MD5码
     *
     * @param file 要获取MD5码的文件
     * @return
     * @throws IOException
     */
    public static String getFileMd5Code(File file) {
        MessageDigest md = null;
        FileInputStream in = null;
        try {
            md = MessageDigest.getInstance("MD5");
            in = new FileInputStream(file);
            FileChannel ch = in.getChannel();
            MappedByteBuffer byteBuffer = ch.map(FileChannel.MapMode.READ_ONLY, 0, file.length());
            md.update(byteBuffer);
            return bufferToHex(md.digest());
        } catch (IOException | NoSuchAlgorithmException ex) {
            ex.printStackTrace();
        } finally {
            if(in!=null){
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return "";
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
