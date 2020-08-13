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
        intent.putExtra(Constants.KEY_OA_SERVER_TOKEN, "eyJhbGciOiJIUzUxMiJ9.eyJ1aWQiOiIxMTUxIiwiZXhwIjoxNTk3MzI1MzEzfQ.-mB-unKFKqEcZRjZOcmG984CHFQ6p1abBmHpMr7mz-gqcTdVlEKlM0Rb7oShJz1CTen1AV1P4DNETuAHnsGkfw");
        intent.putExtra(Constants.KEY_BUSINESS_ID, "40289fbc73e567b00173e568f7c10001");


        intent.putExtra(Constants.KEY_MULTI_TO_ONE, true);
        intent.putExtra(Constants.PARAM_USER_ID, "1");
        intent.putExtra(Constants.PARAM_USER_NAME, "测试人员");
        intent.putExtra(Constants.PARAM_FILE_ID, "40289fbc73e559c40173e56ed5750000");
        intent.putExtra(Constants.KEY_SERVER_URL, "http://192.168.31.60:8089/pdf_handwriting/");
        intent.putExtra(Constants.KEY_WEB_SOCKET_URL, "ws://192.168.31.60:8089/pdf_handwriting/meeting/ws/");
////        intent.putExtra(Constants.KEY_MEETING_REOCRD_ID, "1");
//        intent.putExtra(Constants.KEY_IS_HOST, true);
//        intent.putExtra(Constants.KEY_IS_DOC_MODE, true);
//        intent.putExtra(Constants.KEY_CURRENT_PAGE, 1);
        intent.putExtra(Constants.KEY_SERVER_TOKEN, "eyJhbGciOiJIUzI1NiJ9.eyJ1aWQiOiIxMTUxIiwidW5hbWUiOiLlvKDkuIkiLCJleHAiOjE1OTczNjg1MTN9.FsfbpcwYwvKDHRv1R1_eomWw_pl-SVVOjLzLWUBw-R4");
        startActivityForResult(intent,1001);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("MainActivity", requestCode + "--" + resultCode);
    }
}
