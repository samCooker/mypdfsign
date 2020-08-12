package com.cookie.mypdfsign;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import cn.com.chaochuang.pdf_operation.OaPdfView;
import cn.com.chaochuang.pdf_operation.SignPdfView;
import cn.com.chaochuang.pdf_operation.utils.Constants;

public class MainActivity extends Activity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = new Intent(this, OaPdfView.class);
//        Intent intent = new Intent(this,SignPdfView.class);

        intent.putExtra(Constants.KEY_OA_SERVER_URL, "http://192.168.31.60:48080/gaoxinoa/");
        intent.putExtra(Constants.KEY_OA_SERVER_TOKEN, "eyJhbGciOiJIUzUxMiJ9.eyJ1aWQiOiIxIiwiZXhwIjoxNTk3MTU5MzE5fQ.xEARu9voRmFxOUunVMvIIGZUtvPLWqaDu5jqa_N85MR2K2f4_sVrkhfzo9I49l7ib-mS5BlUpuTNOmYr6vopmQ");


        intent.putExtra(Constants.PARAM_USER_ID, "1");
        intent.putExtra(Constants.PARAM_USER_NAME, "测试人员");
        intent.putExtra(Constants.PARAM_FILE_ID, "40289fbc73d6436d0173d7151c470000");
        intent.putExtra(Constants.KEY_SERVER_URL, "http://192.168.31.60:8089/pdf_handwriting/");
        intent.putExtra(Constants.KEY_WEB_SOCKET_URL, "ws://192.168.31.60:8089/pdf_handwriting/meeting/ws/");
////        intent.putExtra(Constants.KEY_MEETING_REOCRD_ID, "1");
//        intent.putExtra(Constants.KEY_IS_HOST, true);
//        intent.putExtra(Constants.KEY_IS_DOC_MODE, true);
//        intent.putExtra(Constants.KEY_CURRENT_PAGE, 1);
        intent.putExtra(Constants.KEY_SERVER_TOKEN, "eyJhbGciOiJIUzI1NiJ9.eyJ1aWQiOiIxIiwidW5hbWUiOiLns7vnu5_nrqHnkIblkZgiLCJleHAiOjE1OTcyMDI1MTl9.LvfWSXbLMVjRPd3AhhzKBWeg1MiISmGY0aKNAYCL0ms");
        startActivityForResult(intent,1001);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("MainActivity", requestCode + "--" + resultCode);
    }
}
