package com.cookie.mypdfsign;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import cn.com.chaochuang.pdf_operation.SignPdfView;
import cn.com.chaochuang.pdf_operation.utils.Constants;

public class MainActivity extends Activity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = new Intent(this,SignPdfView.class);
        intent.putExtra(Constants.PARAM_USER_ID, "1");
        intent.putExtra(Constants.PARAM_USER_NAME, "测试人员");
        intent.putExtra(Constants.PARAM_FILE_ID, "40289fbc6f6a130d016f6a22cbc20000");
        intent.putExtra(Constants.KEY_SERVER_URL, "http://192.168.31.60:8089/pdf_handwriting/");
        intent.putExtra(Constants.KEY_WEB_SOCKET_URL, "ws://192.168.31.60:8089/pdf_handwriting/meeting/ws/");
//        intent.putExtra(Constants.KEY_MEETING_REOCRD_ID, "1");
        intent.putExtra(Constants.KEY_IS_HOST, true);
        intent.putExtra(Constants.KEY_PEN_ONLY, false);
        intent.putExtra(Constants.KEY_CURRENT_PAGE, 1);
        intent.putExtra(Constants.KEY_SERVER_TOKEN, "eyJhbGciOiJIUzI1NiJ9.eyJ1aWQiOiIxIiwidW5hbWUiOiJhZG1pbiIsImV4cCI6MTU4MjIxMDkwMn0.NGn4VMwOinRyTwZB-1t4OrqdthZbAJuTbPf5LRKZrwo");
        startActivityForResult(intent,1001);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("MainActivity", requestCode + "--" + resultCode);
    }
}
