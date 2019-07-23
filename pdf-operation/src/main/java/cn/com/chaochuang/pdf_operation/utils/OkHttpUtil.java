package cn.com.chaochuang.pdf_operation.utils;

import android.util.Log;
import com.alibaba.fastjson.JSON;
import com.chaochuang.oa.dataaec.util.AesTool;
import com.github.barteksc.pdfviewer.model.HandwritingData;
import okhttp3.*;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
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
    public final static int CONNECT_TIMEOUT =20;
    //设置读取超时时间
    public final static int READ_TIMEOUT=30;
    //设置写的超时时间
    public final static int WRITE_TIMEOUT=20;

    public static final MediaType CONTENT_TYPE = MediaType.parse("application/x-www-form-urlencoded; charset=utf-8");
    private OkHttpClient client;
    private Boolean isEncoding;
    private String serverToken;

    private List<HandwritingData> commentDataList;

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

    public void setCommentDataList(String jsonData){
        this.commentDataList = JSON.parseArray(jsonData, HandwritingData.class);
    }

    public List<HandwritingData> getCommentDataList() {
        return commentDataList;
    }
}
