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
        intent.putExtra(Constants.PARAM_FILE_ID, "2c9f80816c45cf7b016c45d4eb940000");
        intent.putExtra(Constants.KEY_SERVER_URL, "http://192.168.31.60:8089/filecomment/");
        intent.putExtra(Constants.KEY_PEN_ONLY, false);
        intent.putExtra(Constants.KEY_SERVER_TOKEN, "eyJhbGciOiJIUzI1NiJ9.eyJ1aWQiOiIxIiwidW5hbWUiOiLns7vnu5_nrqHnkIblkZgiLCJleHAiOjE1NjQ2NDA5MzZ9.srL5N8bzreczU-hg0UdSU9yoCx574JDhfMIR73W0dEM");
        startActivity(intent);

    }


}
