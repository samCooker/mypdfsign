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
        intent.putExtra(Constants.PARAM_FILE_ID, "2c9f80816c73de17016c73e7d1880000");
        intent.putExtra(Constants.KEY_SERVER_URL, "http://192.168.31.60:8089/pdf_handwriting/");
        intent.putExtra(Constants.KEY_WEB_SOCKET_URL, "ws://192.168.31.60:8089/pdf_handwriting/meeting/ws/");
        intent.putExtra(Constants.KEY_MEETING_REOCRD_ID, "1");
        intent.putExtra(Constants.KEY_IS_HOST, true);
        intent.putExtra(Constants.KEY_PEN_ONLY, false);
        intent.putExtra(Constants.KEY_CURRENT_PAGE, 2);
        intent.putExtra(Constants.KEY_SERVER_TOKEN, "eyJhbGciOiJIUzI1NiJ9.eyJ1aWQiOiIxIiwidW5hbWUiOiLns7vnu5_nrqHnkIblkZgiLCJleHAiOjE1NjU4NTUwMzB9.aZ0kzC7zbhZjuHFeo7XjxxV26O9ojSxfknI90YLY4As");
        startActivity(intent);

    }


}
