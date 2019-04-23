package cn.com.chaochuang.pdf_operation;

import android.Manifest;
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
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import cn.com.chaochuang.pdf_operation.utils.Constants;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.util.FitPolicy;
import com.github.gcacace.signaturepad.views.SignaturePad;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 2019-4-23
 *
 * @author Shicx
 */

public class SignPdfView extends AppCompatActivity {

    private PDFView pdfView;
    private RelativeLayout rlPdfView;
    private View signView;
    private SignaturePad signaturePad;
    private FloatingActionsMenu actionsMenu;
    private FloatingActionButton handWriteItem,closeViewItem;
    private String filePath;

    /**
     * 手写批注菜单按钮
     */
    public FloatingActionButton btnClose, btnClear, btnUndo, btnRedo, btnSave, btnPen , btnErase ;


    private int REQ_CODE=100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_sign_pdf);

        this.initParams();

        pdfView = findViewById(R.id.pdf_view);
        rlPdfView = findViewById(R.id.rl_pdf_view);

        this.initMenuBtn();

        //获取权限
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

    private void initMenuBtn() {
        actionsMenu = findViewById(R.id.action_menu);

        //region 手写批注
        handWriteItem = new FloatingActionButton(this);
        handWriteItem.setSize(FloatingActionButton.SIZE_NORMAL);
        handWriteItem.setIcon(R.drawable.ic_pdf_pen_f);
        handWriteItem.setColorNormalResId(R.color.pdf_btn_white);
        handWriteItem.setColorPressedResId(R.color.pdf_btn_press_white);
        handWriteItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSignView();
                showHandWriteBtns();
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
            }
        });
        //endregion

        //-------以下是手写批注的按钮-------

        //region 关闭手写页面
        btnClose = new FloatingActionButton(this);
        btnClose.setSize(FloatingActionButton.SIZE_NORMAL);
        btnClose.setIcon(R.drawable.ic_pdf_back_f);
        btnClose.setColorNormalResId(R.color.pdf_btn_white);
        btnClose.setColorPressedResId(R.color.pdf_btn_press_white);

        //region 点击事件
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signaturePad.setVisibility(View.INVISIBLE);
            }
        });
        //endregion
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
                //水平翻页，
                float offsetX = -pdfView.getCurrentXOffset()-(pdfView.getCurrentPage()*pdfView.getWidth());

                SavePdfUtil.insertImage(filePath,outPath,signaturePad.getTransparentSignatureBitmap(),pdfView.getCurrentPage()+1,pdfView.getZoom(),offsetX,-pdfView.getCurrentYOffset());

                File file = new File(outPath);
                final Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                Uri uri;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    //判断版本是否在7.0以上
                    uri = FileProvider.getUriForFile(SignPdfView.this, SignPdfView.this.getPackageName() + ".provider", file);
                    //添加这一句表示对目标应用临时授权该Uri所代表的文件
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NO_HISTORY);

                } else {
                    uri = Uri.fromFile(file);
                }

                intent.setDataAndType(uri,"application/pdf");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        startActivity(intent);
                    }
                });
            }
        });
        //endregion

        //region 手写签批撤销
        btnUndo = new FloatingActionButton(this);
        btnUndo.setSize(FloatingActionButton.SIZE_NORMAL);
        btnUndo.setIcon(R.drawable.ic_pdf_undo_f);
        btnUndo.setColorNormalResId(R.color.pdf_btn_white);
        btnUndo.setColorPressedResId(R.color.pdf_btn_press_white);
        btnUndo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
        //endregion

        //region 手写签批回退
        btnRedo = new FloatingActionButton(this);
        btnRedo.setSize(FloatingActionButton.SIZE_NORMAL);
        btnRedo.setIcon(R.drawable.ic_pdf_redo_f);
        btnRedo.setColorNormalResId(R.color.pdf_btn_white);
        btnRedo.setColorPressedResId(R.color.pdf_btn_press_white);
        btnRedo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
        //endregion

        //region 手写签批清空
        btnClear = new FloatingActionButton(this);
        btnClear.setSize(FloatingActionButton.SIZE_NORMAL);
        btnClear.setIcon(R.drawable.ic_pdf_delete_f);
        btnClear.setColorNormalResId(R.color.pdf_btn_white);
        btnClear.setColorPressedResId(R.color.pdf_btn_press_white);
        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        //endregion

        //region 设置画笔样式
        btnPen = new FloatingActionButton(this);
        btnPen.setSize(FloatingActionButton.SIZE_NORMAL);
        btnPen.setIcon(R.drawable.ic_pdf_sign_settings_f);
        btnPen.setColorNormalResId(R.color.pdf_btn_white);
        btnPen.setColorPressedResId(R.color.pdf_btn_press_white);
        btnPen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
        //endregion

        addActionBtns();

    }

    /**
     * 手写画布
     */
    private void showSignView() {
        LayoutInflater inflater = LayoutInflater.from(this);
        signView = inflater.inflate(R.layout.sign_view,rlPdfView,false);

        SignaturePad _padView = rlPdfView.findViewById(R.id.signature_pad);
        if(_padView==null) {
            rlPdfView.addView(signView,1);
        }
        signaturePad = signView.findViewById(R.id.signature_pad);
        signaturePad.setMinWidth(1.6f);
        signaturePad.setMaxWidth(2.6f);
        signaturePad.setOnSignedListener(new SignaturePad.OnSignedListener() {
            @Override
            public void onStartSigning() {
                if (signaturePad.getPoints() != null && signaturePad.getPoints().size() > 0) {
                    //TimedPoint timedPoint = signaturePad.getPoints().get(0);
                    //Log.d("signaturePad", timedPoint.x + ":" + timedPoint.y);
                }

            }

            @Override
            public void onSigned() {
            }

            @Override
            public void onClear() {

            }
        });
    }

    /**
     * 获取参数
     */
    private void initParams() {
        Intent intent = getIntent();
        if (intent.hasExtra(Constants.KEY_FILE_PATH)) {
            filePath = intent.getStringExtra(Constants.KEY_FILE_PATH);
        }
    }

    public void openPdfFile(){
        pdfView.fromFile(new File(filePath))
                .defaultPage(0)
                .swipeHorizontal(false)
                .pageSnap(true)
                .pageFling(true)
                .enableAnnotationRendering(true)
                .scrollHandle(null)
                .spacing(0)
                .autoSpacing(true)
                .pageFitPolicy(FitPolicy.HEIGHT).load();
    }

    /**
     * 显示主菜单按钮
     */
    private void addActionBtns(){
        actionsMenu.addButton(handWriteItem);
        actionsMenu.addButton(closeViewItem);
    }

    /**
     * 显示手写批注按钮，去除主菜单按钮
     */
    private void showHandWriteBtns() {
        removeActionBtns();
        addHandWriteBtns();
    }

    /**
     * 去除主菜单按钮
     */
    private void removeActionBtns(){
        actionsMenu.removeButton(handWriteItem);
        actionsMenu.removeButton(closeViewItem);
    }

    /**
     * 显示手写批注按钮
     */
    private void addHandWriteBtns(){
        actionsMenu.addButton(btnSave);
        actionsMenu.addButton(btnPen);
        actionsMenu.addButton(btnUndo);
        actionsMenu.addButton(btnRedo);
        actionsMenu.addButton(btnClear);
        actionsMenu.addButton(btnClose);
    }
}
