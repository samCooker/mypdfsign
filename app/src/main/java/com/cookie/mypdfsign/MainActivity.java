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
        startActivity(intent);

    }


}
