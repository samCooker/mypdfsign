package cn.com.chaochuang.pdf_operation;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import cn.com.chaochuang.pdf_operation.utils.Constants;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.util.FitPolicy;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 2019-4-23
 *
 * @author Shicx
 */

public class SignPdfView extends AppCompatActivity {

    private static final String TAG = SignPdfView.class.getSimpleName();

    private ProgressDialog progressDialog;

    private PDFView pdfView;
    private FloatingActionsMenu actionsMenu;
    private FloatingActionButton handWriteItem,closeViewItem;
    private String filePath;
    private int currentPage;

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
        //加载提示框
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("正在加载");
        progressDialog.setCancelable(true);
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
                pdfView.showSignView();
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
                finish();
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
                pdfView.hideSignView();
                showActionBtns();
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
                pdfView.insertSignImages();
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
                pdfView.signUndo();
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
                pdfView.signRedo();
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
                final AlertDialog.Builder builder = new AlertDialog.Builder(SignPdfView.this, R.style.Theme_AppCompat_Light_Dialog_Alert_EinkSign);
                builder.setMessage("是否清空");
                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        pdfView.signClear();
                    }
                });
                Dialog dialog = builder.create();
                dialog.setCancelable(true);
                dialog.show();
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
                .defaultPage(currentPage-1)
                .swipeHorizontal(true)
                .pageSnap(true)
                .pageFling(true)
                .enableAnnotationRendering(true)
                .scrollHandle(null)
                .spacing(0)
                .autoSpacing(true)
                .pageFitPolicy(FitPolicy.WIDTH).load();
    }

    /**
     * 显示手写批注按钮，去除主菜单按钮
     */
    private void showHandWriteBtns() {
        removeActionBtns();
        addHandWriteBtns();
    }

    private void showActionBtns(){
        removeHandWriteBtns();
        addActionBtns();
    }


    /**
     * 显示主菜单按钮
     */
    private void addActionBtns(){
        actionsMenu.addButton(handWriteItem);
        actionsMenu.addButton(closeViewItem);
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
    private void removeHandWriteBtns(){
        actionsMenu.removeButton(btnSave);
        actionsMenu.removeButton(btnPen);
        actionsMenu.removeButton(btnUndo);
        actionsMenu.removeButton(btnRedo);
        actionsMenu.removeButton(btnClear);
        actionsMenu.removeButton(btnClose);
    }

    private void showProgressDialog(){
        if(progressDialog!=null){
            progressDialog.show();
        }
    }
    private void hideProgressDialog(){
        if(progressDialog!=null){
            progressDialog.hide();
        }
    }
}
