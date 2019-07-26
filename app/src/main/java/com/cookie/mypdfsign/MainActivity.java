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
        intent.putExtra(Constants.KEY_FILE_PATH, Environment.getExternalStorageDirectory().getPath() + "/测试公文.pdf");
        intent.putExtra(Constants.PARAM_FILE_ID, "2c9f80816c2cfb25016c2d0065120000");
        intent.putExtra(Constants.KEY_SERVER_URL, "http://192.168.31.60:8089/filecomment/");
        intent.putExtra(Constants.KEY_SERVER_TOKEN, "eyJhbGciOiJIUzI1NiJ9.eyJ1aWQiOiIxIiwidXNlck5hbWUiOiLns7vnu5_nrqHnkIblkZgiLCJleHAiOjE1NjQxNDA5ODN9.om3mkIuOIjCAz1AMipCjFkQ4OgiG9Oe3fZwT_a6iVls");
        startActivity(intent);

    }


}
