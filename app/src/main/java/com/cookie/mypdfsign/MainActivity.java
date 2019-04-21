package com.cookie.mypdfsign;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Toast;
import com.cookie.pdfcreator.SavePdfUtil;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;
import com.github.barteksc.pdfviewer.util.FitPolicy;
import com.github.gcacace.signaturepad.utils.TimedPoint;
import com.github.gcacace.signaturepad.views.SignaturePad;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private PDFView pdfView;
    private SignaturePad signaturePad;
    private FloatingActionsMenu actionsMenu;
    private FloatingActionButton handwritingItem,closeViewItem;
    private String filePath;

    /**
     * 手写批注菜单按钮
     */
    public FloatingActionButton btnClose, btnClear, btnUndo, btnRedo, btnSave, btnPen , btnErase ;


    private int REQ_CODE=100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        filePath = Environment.getExternalStorageDirectory().getPath() + "/测试公文.pdf";

        pdfView = findViewById(R.id.pdf_view);
        signaturePad = findViewById(R.id.signature_pad);
        signaturePad.setMinWidth(3.0f);
        signaturePad.setMaxWidth(5.0f);
        signaturePad.setPenColorRes(R.mipmap.ic_pen);
        signaturePad.setOnSignedListener(new SignaturePad.OnSignedListener() {
            @Override
            public void onStartSigning() {
                if (signaturePad.getPoints() != null && signaturePad.getPoints().size() > 0) {
                    TimedPoint timedPoint = signaturePad.getPoints().get(0);
                    Log.d("signaturePad", timedPoint.x + ":" + timedPoint.y);
                }

            }

            @Override
            public void onSigned() {
            }

            @Override
            public void onClear() {

            }
        });

        actionsMenu = findViewById(R.id.action_menu);

        //region 手写批注
        handwritingItem = new FloatingActionButton(this);
        handwritingItem.setSize(FloatingActionButton.SIZE_NORMAL);
        handwritingItem.setIcon(R.drawable.ic_pdf_pen_f);
        handwritingItem.setColorNormalResId(R.color.pdf_btn_white);
        handwritingItem.setColorPressedResId(R.color.pdf_btn_press_white);
        handwritingItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signaturePad.setVisibility(View.VISIBLE);
            }
        });
        //endregion

        //region 关闭PDF预览页面
        closeViewItem = new FloatingActionButton(this);
        closeViewItem.setSize(FloatingActionButton.SIZE_NORMAL);
        closeViewItem.setIcon(R.drawable.ic_pdf_close_f);
        closeViewItem.setColorNormalResId(R.color.pdf_btn_white);
        closeViewItem.setColorPressedResId(R.color.pdf_btn_press_white);
        closeViewItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signaturePad.setVisibility(View.INVISIBLE);
            }
        });
        //endregion

        //region 保存手写签批
        btnSave = new FloatingActionButton(this);
        btnSave.setSize(FloatingActionButton.SIZE_NORMAL);
        btnSave.setIcon(R.drawable.ic_pdf_save_f);
        btnSave.setColorNormalResId(R.color.pdf_btn_white);
        btnSave.setColorPressedResId(R.color.pdf_btn_press_white);
        btnSave.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String outPath =Environment.getExternalStorageDirectory().getPath()+"/签名.pdf";
                Log.d("signaturePad","height:"+pdfView.getHeight());
                Log.d("signaturePad","width:"+pdfView.getWidth());
                Log.d("signaturePad","offset x:"+pdfView.getCurrentXOffset());
                Log.d("signaturePad","offset y:"+pdfView.getCurrentYOffset());
                Log.d("signaturePad","x:"+pdfView.getX());
                Log.d("signaturePad","y:"+pdfView.getY());

                //水平翻页，
                float offsetX = -pdfView.getCurrentXOffset()-(pdfView.getCurrentPage()*pdfView.getWidth());

                DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
                Log.d("signaturePad","displayMetrics:"+displayMetrics.density);
                Log.d("signaturePad","displayMetrics:"+displayMetrics.widthPixels);

                SavePdfUtil.insertImage(filePath,outPath,signaturePad.getTransparentSignatureBitmap(),pdfView.getCurrentPage()+1,pdfView.getZoom(),offsetX,-pdfView.getCurrentYOffset(),displayMetrics.density);

                File file = new File(outPath);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                Uri uri;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    //判断版本是否在7.0以上
                    uri = FileProvider.getUriForFile(MainActivity.this, MainActivity.this.getPackageName() + ".provider", file);
                    //添加这一句表示对目标应用临时授权该Uri所代表的文件
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NO_HISTORY);

                } else {
                    uri = Uri.fromFile(file);
                }

                intent.setDataAndType(uri,"application/pdf");
                startActivity(intent);
            }
        });
        //endregion


        actionsMenu.addButton(handwritingItem);
        actionsMenu.addButton(btnSave);
        actionsMenu.addButton(closeViewItem);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String[] permissions = new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
            };
            List<String> mPermissionList = new ArrayList<String>();
            for (String permission : permissions) {
                if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    mPermissionList.add(permission);
                }
            }
            if (!mPermissionList.isEmpty()) {
                //未授予的权限
                permissions = mPermissionList.toArray(new String[mPermissionList.size()]);
                ActivityCompat.requestPermissions(this, permissions, REQ_CODE);
            }else{
                openPdfFile();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(REQ_CODE == requestCode){
            openPdfFile();
        }
    }

    public void openPdfFile(){

        pdfView.fromFile(new File(filePath))
                .defaultPage(0)
                .swipeHorizontal(true)
                .pageSnap(true)
                .pageFling(true)
                .enableAnnotationRendering(true)
                .scrollHandle(null)
                .spacing(0)
                .autoSpacing(false)
                .pageFitPolicy(FitPolicy.HEIGHT).load();
    }
}
