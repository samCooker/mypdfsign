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
        intent.putExtra(Constants.PARAM_USER_NAME, "测试人员测试人员");
        intent.putExtra(Constants.PARAM_FILE_ID, "40289fbc7356428e01735653abda0000");
        intent.putExtra(Constants.KEY_MULTI_TO_ONE, true);
        intent.putExtra(Constants.KEY_SERVER_URL, "http://192.168.31.60:8089/pdf_handwriting/");
        intent.putExtra(Constants.KEY_WEB_SOCKET_URL, "ws://192.168.31.60:8089/pdf_handwriting/meeting/ws/");
//        intent.putExtra(Constants.KEY_MEETING_REOCRD_ID, "1");
        intent.putExtra(Constants.KEY_IS_HOST, true);
        intent.putExtra(Constants.KEY_IS_DOC_MODE, true);
        intent.putExtra(Constants.KEY_IS_HIDE_ANNOT, false);
        intent.putExtra(Constants.KEY_UPDATE_PDF, false);
        intent.putExtra(Constants.KEY_CURRENT_PAGE, 0);
        intent.putExtra(Constants.KEY_ENTRY_LIST, "[{\"id\":33,\"userId\":2073,\"word\":\"同意\"},{\"id\":35,\"userId\":2073,\"word\":\"已阅\"},{\"id\":36,\"userId\":2073,\"word\":\"知道了，呈上级阅示。\"},{\"id\":37,\"userId\":2073,\"word\":\"这是一个很长的词条这是一个很长的词条这是一个很长的词条这是一个很长的词条这是一个很长的词条这是一个很长的词条这是一个很长的词条这是一个很长的词条这是一个很长的词条这是一个很长的词条这是一个很长的词条这是一个很长的词条这是一个很长的词条这是一个很长的词条这是一个很长的词条\"}]");
        intent.putExtra(Constants.KEY_SERVER_TOKEN, "eyJhbGciOiJIUzI1NiJ9.eyJ1aWQiOiIxIiwidW5hbWUiOiJhZG1pbiIsImV4cCI6MTU5NTA1NDgxOH0.vxZBkH92_iqjWOqv1wZ77gpvLtTeUDN_Wjw62axccLo");
        startActivityForResult(intent,1001);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("MainActivity", requestCode + "--" + resultCode);
    }
}
