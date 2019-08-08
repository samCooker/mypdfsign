package com.cookie.mypdfsign;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import cn.com.chaochuang.pdf_operation.SignPdfView;
import cn.com.chaochuang.pdf_operation.utils.Constants;

public class MainActivity extends Activity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = new Intent(this,SignPdfView.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(Constants.PARAM_USER_ID, "1");
        intent.putExtra(Constants.PARAM_USER_NAME, "测试人员");
        intent.putExtra(Constants.PARAM_FILE_ID, "2c9f80816c64c058016c64c0ecc20000");
        intent.putExtra(Constants.KEY_SERVER_URL, "http://192.168.31.60:8089/pdf_handwriting/");
        intent.putExtra(Constants.KEY_WEB_SOCKET_URL, "ws://192.168.31.60:8089/pdf_handwriting/meeting/ws/");
        intent.putExtra(Constants.KEY_MEETING_REOCRD_ID, "1");
        intent.putExtra(Constants.KEY_IS_HOST, true);
        intent.putExtra(Constants.KEY_PEN_ONLY, false);
        intent.putExtra(Constants.KEY_SERVER_TOKEN, "eyJhbGciOiJIUzI1NiJ9.eyJ1aWQiOiIxIiwidW5hbWUiOiLns7vnu5_nrqHnkIblkZgiLCJleHAiOjE1NjUzMzgyNzd9.m3o4l64oJtm0X_NVpggQAJQR9Xi5AIsZiiJiKDHnoA0");
        startActivity(intent);

    }


}
