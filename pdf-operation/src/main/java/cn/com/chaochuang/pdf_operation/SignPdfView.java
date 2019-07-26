package cn.com.chaochuang.pdf_operation;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.*;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import cn.com.chaochuang.pdf_operation.model.AppResponse;
import cn.com.chaochuang.pdf_operation.utils.Constants;
import cn.com.chaochuang.pdf_operation.utils.ImageTools;
import cn.com.chaochuang.pdf_operation.utils.OkHttpUtil;
import com.alibaba.fastjson.JSON;
import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.model.HandwritingData;
import com.github.barteksc.pdfviewer.util.FitPolicy;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import okhttp3.*;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static cn.com.chaochuang.pdf_operation.utils.Constants.*;

/**
 * 2019-4-23
 *
 * @author Shicx
 */

public class SignPdfView extends AppCompatActivity {

    private static final String TAG = SignPdfView.class.getSimpleName();

    private ProgressDialog progressDialog;

    private PDFView pdfView;
    private FloatingActionMenu actionsMenu;
    private FloatingActionButton handWriteItem,closeViewItem;

    private OkHttpUtil httpUtil;
    private PdfActionReceiver pdfActionReceiver;

    /**
     * 本地数据
     * */
    private SharedPreferences penSettingData;

    private String fileId;
    private String filePath;
    private String serverUrl;
    private String serverToken;
    private int prePage=0;

    /**
     * 手写批注菜单按钮
     */
    public FloatingActionButton btnClose, btnClear, btnUndo, btnRedo, btnSave, btnPen , btnErase;

    private boolean eraseFlag = false;


    private int REQ_CODE=100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_sign_pdf);

        //获取本地数据
        penSettingData = getSharedPreferences(PenSettingFragment.PEN_SETTING_DATA, Context.MODE_PRIVATE);

        this.initParams();

        pdfView = findViewById(R.id.pdf_view);

        //加载提示框
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("正在加载");
        progressDialog.setCancelable(true);
        this.initMenuBtn();
        this.initBroadcaseReceiver();
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
                downloadOrOpenFile();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(REQ_CODE == requestCode){
            downloadOrOpenFile();
        }
    }

    private void initMenuBtn() {
        actionsMenu = findViewById(R.id.action_menu);
        //region 手写批注
        handWriteItem = new FloatingActionButton(this);
        handWriteItem.setButtonSize(FloatingActionButton.SIZE_NORMAL);
        handWriteItem.setImageResource(R.drawable.ic_pdf_pen_f);
        handWriteItem.setColorNormalResId(R.color.pdf_btn_white);
        handWriteItem.setColorPressedResId(R.color.pdf_btn_press_white);
        handWriteItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                float penWidth = penSettingData.getFloat(PenSettingFragment.PEN_WIDTH,PenSettingFragment.defaultWidth);
                int penColor = penSettingData.getInt(PenSettingFragment.PEN_COLOR, Color.BLACK);
                pdfView.showSignView(penWidth,penColor);
                showHandWriteBtns();
            }
        });
        //endregion

        //region 关闭PDF预览页面
        closeViewItem = new FloatingActionButton(this);
        closeViewItem.setButtonSize(FloatingActionButton.SIZE_NORMAL);
        closeViewItem.setImageResource(R.drawable.ic_pdf_close_f);
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
        btnClose.setButtonSize(FloatingActionButton.SIZE_NORMAL);
        btnClose.setImageResource(R.drawable.ic_pdf_back_f);
        btnClose.setColorNormalResId(R.color.pdf_btn_white);
        btnClose.setColorPressedResId(R.color.pdf_btn_press_white);

        //region 点击事件
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!pdfView.isHandwritingEmpty()){
                    final AlertDialog.Builder builder = new AlertDialog.Builder(SignPdfView.this,AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
                    builder.setMessage("有未保存的手写内容，是否退出");
                    builder.setTitle("退出手写模式");
                    builder.setPositiveButton("确定",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    pdfView.hideSignView();
                                    showActionBtns();
                                    Toast.makeText(SignPdfView.this,"已关闭手写批注模式",Toast.LENGTH_SHORT).show();
                                }
                            });
                    builder.setNegativeButton("取消",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,int which) {
                                    dialog.dismiss();
                                }
                            });

                    final Dialog dialog = builder.create();
                    dialog.setCancelable(false);
                    dialog.show();
                }else{
                    pdfView.hideSignView();
                    showActionBtns();
                }

            }
        });
        //endregion
        //endregion

        //region 保存手写签批
        btnSave = new FloatingActionButton(this);
        btnSave.setButtonSize(FloatingActionButton.SIZE_NORMAL);
        btnSave.setImageResource(R.drawable.ic_pdf_save_f);
        btnSave.setColorNormalResId(R.color.pdf_btn_white);
        btnSave.setColorPressedResId(R.color.pdf_btn_press_white);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!pdfView.isHandwritingEmpty()){
                    showProgressDialog("正在保存");
                    new Thread(){
                        @Override
                        public void run() {
                            Bitmap bim = pdfView.getSignBitmap();
                            float wr = pdfView.getDisplayWRadio();
                            float hr = pdfView.getDisplayHRadio();
                            if (bim != null) {
                                float x = pdfView.getSignX();
                                float y = pdfView.getSignY();
                                //获取没有放大或缩小的图片数据
                                Bitmap originBitmap = ImageTools.scaleBitmap(bim,1/pdfView.getZoom());
                                float width = originBitmap.getWidth() / wr;
                                float height = originBitmap.getHeight() / hr;
                                HandwritingData handwritingData = new HandwritingData();
                                handwritingData.setPx(x/wr);
                                handwritingData.setPy(y/hr);
                                handwritingData.setImageWidth(width);
                                handwritingData.setImageHeight(height);
                                handwritingData.setImageBitmap(originBitmap);
                                handwritingData.setPdfFileHeight(pdfView.getPhysicalPdfHeight());
                                handwritingData.setPdfFileWidth(pdfView.getPhysicalPdfWidth());
                                handwritingData.setPageNo(pdfView.getCurrentPage());
                                //添加到图层
                                pdfView.addHandwritingData(handwritingData);
                                //保存到远程服务器
                                saveHandwritingData(handwritingData);
                            }else{
                                broadcastIntent(BC_HIDE_LOADING);
                            }
                        }
                    }.start();
                }else{
                    Toast.makeText(SignPdfView.this,"无手写内容",Toast.LENGTH_LONG).show();
                }

            }
        });
        //endregion

        //region 手写签批撤销
        btnUndo = new FloatingActionButton(this);
        btnUndo.setButtonSize(FloatingActionButton.SIZE_NORMAL);
        btnUndo.setImageResource(R.drawable.ic_pdf_undo_f);
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
        btnRedo.setButtonSize(FloatingActionButton.SIZE_NORMAL);
        btnRedo.setImageResource(R.drawable.ic_pdf_redo_f);
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
        btnClear.setButtonSize(FloatingActionButton.SIZE_NORMAL);
        btnClear.setImageResource(R.drawable.ic_pdf_delete_f);
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
        btnPen.setButtonSize(FloatingActionButton.SIZE_NORMAL);
        btnPen.setImageResource(R.drawable.ic_pdf_sign_settings_f);
        btnPen.setColorNormalResId(R.color.pdf_btn_white);
        btnPen.setColorPressedResId(R.color.pdf_btn_press_white);
        btnPen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PenSettingFragment penSettingFragment = new PenSettingFragment();
                penSettingFragment.showFragmentDlg(getFragmentManager(), "penSettingFragment", new PenSettingFragment.OnSaveListener() {
                    @Override
                    public void onSaveAction() {
                        float penWidth = penSettingData.getFloat(PenSettingFragment.PEN_WIDTH,PenSettingFragment.defaultWidth);
                        int penColor = penSettingData.getInt(PenSettingFragment.PEN_COLOR, Color.BLACK);
                        pdfView.setPenWidth(penWidth,penColor);
                    }
                });
            }
        });
        //endregion

        //region 设置画笔样式
        btnErase = new FloatingActionButton(this);
        btnErase.setButtonSize(FloatingActionButton.SIZE_NORMAL);
        btnErase.setImageResource(R.drawable.ic_pdf_erase);
        btnErase.setColorNormalResId(R.color.pdf_btn_white);
        btnErase.setColorPressedResId(R.color.pdf_btn_press_white);
        btnErase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                eraseFlag = !eraseFlag;
                if(eraseFlag) {
                    Toast.makeText(SignPdfView.this,"已进入橡皮擦模式",Toast.LENGTH_LONG).show();
                    removeHandWriteBtns();
                    btnErase.setImageResource(R.drawable.ic_pdf_close_f);
                    actionsMenu.addMenuButton(btnErase);
                }else{
                    Toast.makeText(SignPdfView.this,"已退出橡皮擦模式",Toast.LENGTH_LONG).show();
                    btnErase.setImageResource(R.drawable.ic_pdf_erase);
                    actionsMenu.removeMenuButton(btnErase);
                    addHandWriteBtns();
                }
                pdfView.signEraseMode(eraseFlag);
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
        if (intent.hasExtra(PARAM_FILE_ID)) {
            fileId = intent.getStringExtra(Constants.PARAM_FILE_ID);
        }
        if (intent.hasExtra(Constants.KEY_SERVER_URL)) {
            serverUrl = intent.getStringExtra(Constants.KEY_SERVER_URL);
        }
        if (intent.hasExtra(Constants.KEY_SERVER_TOKEN)) {
            serverToken = intent.getStringExtra(Constants.KEY_SERVER_TOKEN);
        }

        httpUtil = new OkHttpUtil(true,serverToken);
    }


    private void initBroadcaseReceiver() {
        pdfActionReceiver = new PdfActionReceiver();

        IntentFilter handwritingListFilter = new IntentFilter(BC_HANDWRITING_LIST);
        IntentFilter resSuccessFilter = new IntentFilter(BC_RESPONSE_SUCCESS);
        IntentFilter showLoadingFilter = new IntentFilter(BC_SHOW_LOADING);
        IntentFilter hideLoadingFilter = new IntentFilter(BC_HIDE_LOADING);
        IntentFilter resFailureFilter = new IntentFilter(BC_RESPONSE_FAILURE);
        IntentFilter saveSuccessFilter = new IntentFilter(BC_SAVE_HANDWRITING_SUCCESS);

        registerReceiver(pdfActionReceiver,handwritingListFilter);
        registerReceiver(pdfActionReceiver,resFailureFilter);
        registerReceiver(pdfActionReceiver,resSuccessFilter);
        registerReceiver(pdfActionReceiver,showLoadingFilter);
        registerReceiver(pdfActionReceiver,hideLoadingFilter);
        registerReceiver(pdfActionReceiver,saveSuccessFilter);
    }

    public void openPdfFile(){
        pdfView.fromFile(new File(filePath))
                .defaultPage(prePage)
                .swipeHorizontal(true)
                .pageSnap(true)
                .pageFling(true)
                .enableAnnotationRendering(true)
                .scrollHandle(null)
                .spacing(0)
                .autoSpacing(true)
                //宽度自适应（不可修改，修改后插入手写坐标会发生变化）
                .pageFitPolicy(FitPolicy.WIDTH).load();
    }

    private void saveHandwritingData(HandwritingData handwritingData) {
        if(handwritingData!=null&&handwritingData.getImageBitmap()!=null) {
            HandwritingData saveBean = new HandwritingData();
            saveBean.setFileId(fileId);
            saveBean.setPageNo(handwritingData.getPageNo());
            saveBean.setPx(handwritingData.getPx());
            saveBean.setPy(handwritingData.getPy());
            saveBean.setImageWidth(handwritingData.getImageWidth());
            saveBean.setImageHeight(handwritingData.getImageHeight());
            saveBean.setPdfFileWidth(handwritingData.getPdfFileWidth());
            saveBean.setPdfFileHeight(handwritingData.getPdfFileHeight());
            String base64Str = ImageTools.bitmapToBase64(handwritingData.getImageBitmap());
            saveBean.setBase64Code(base64Str);
            httpUtil.post(serverUrl + URL_HANDWRITING_SAVE, "jsonData="+JSON.toJSONString(saveBean), new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    broadcastIntent(BC_RESPONSE_FAILURE);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if(response.isSuccessful()&&response.body()!=null) {
                        broadcastIntent(BC_SAVE_HANDWRITING_SUCCESS);
                    }else {
                        broadcastIntent(BC_RESPONSE_FAILURE);
                    }
                }
            });
        }
    }

    /**
     * 下载文件
     */
    private void downloadOrOpenFile(){
        broadcastIntent(BC_SHOW_LOADING,"正在下载文件");
        httpUtil.get(serverUrl + URL_GET_MD5 + "?fileId=" + fileId, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                broadcastIntent(BC_RESPONSE_FAILURE,"文件下载失败，请尝试重新打开");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                File pdfRoot = new File(getExternalCacheDir(),"comment_pdf");
                if(!pdfRoot.exists()){
                    pdfRoot.mkdir();
                }
                if(response.isSuccessful()&&response.body()!=null) {
                    String data = response.body().string();
                    AppResponse resData = JSON.parseObject(data, AppResponse.class);
                    if(resData.getData()!=null) {
                        Map dataMap = JSON.parseObject(resData.getData().toString());
                        File pdfFile = new File(pdfRoot,dataMap.get("mdfCode")+"/"+dataMap.get("fileName"));
                        if(pdfFile.exists()){
                            filePath = pdfFile.getAbsolutePath();
                            findHandwritingData();
                        }else{
                            //下载文件
                            downloadFile(pdfFile);
                        }
                    }
                }else{
                    broadcastIntent(BC_RESPONSE_FAILURE,"文件下载失败，请尝试重新打开");
                }
            }
        });
    }

    private void downloadFile(final File file){
        if(!file.getParentFile().exists()){
            file.getParentFile().mkdirs();
        }
        OkHttpClient httpClient = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).build();
        Request request = new Request.Builder().url(serverUrl +URL_DOWNLOAD_FILE+"?fileId=" + fileId).header(Constants.HEADER_TOKEN_NAME,serverToken).build();
        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                broadcastIntent(BC_RESPONSE_FAILURE,"文件下载失败，请尝试重新打开");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                ResponseBody responseBody = null;
                BufferedInputStream bis = null;
                FileOutputStream fos = null;
                try {
                    if (call.isCanceled()) {
                        broadcastIntent(BC_RESPONSE_FAILURE,"已取消文件下载");
                        return;
                    }
                    if (response.isSuccessful()) {
                        responseBody = response.body();
                        long total = responseBody.contentLength();
                        bis = new BufferedInputStream(responseBody.byteStream());
                        fos = new FileOutputStream(file);
                        byte[] bytes = new byte[1024 * 8];
                        int len;
                        long current = 0;
                        while ((len = bis.read(bytes)) != -1) {
                            fos.write(bytes, 0, len);
                            fos.flush();
                            current += len;
                            //计算进度
                            int progress = (int) (100 * current / total);
                            Log.d(TAG,"下载进度："+progress);
                        }
                        broadcastIntent(BC_HIDE_LOADING);
                        filePath = file.getAbsolutePath();
                        findHandwritingData();
                    } else {
                        broadcastIntent(BC_HIDE_LOADING);
                        broadcastIntent(BC_RESPONSE_FAILURE,"文件下载失败");
                    }
                } catch (Exception e) {
                    broadcastIntent(BC_HIDE_LOADING);
                    broadcastIntent(BC_RESPONSE_FAILURE,"文件下载异常");
                } finally {
                    if (null != responseBody) {
                        responseBody.close();
                    }
                    if(bis!=null) {
                        bis.close();
                    }
                    if(fos!=null) {
                        fos.close();
                    }
                }
            }
        });
    }

    private void findHandwritingData(){
        //查询批注数据
        showProgressDialog("正在打开文件");
        httpUtil.get(serverUrl + Constants.URL_HANDWRITING_LIST+"?fileId="+fileId, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                broadcastIntent(BC_RESPONSE_FAILURE,"请求错误");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(response.isSuccessful()&&response.body()!=null) {
                    String data = response.body().string();
                    AppResponse resData = JSON.parseObject(data, AppResponse.class);
                    if(resData.getData()!=null) {
                        httpUtil.setCommentDataList(resData.getData().toString());
                    }
                    broadcastIntent(BC_HANDWRITING_LIST);
                }else {
                    String msg = "文件打开失败，请尝试重新打开";
                    broadcastIntent(BC_RESPONSE_FAILURE,msg);
                }
            }
        });
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
        actionsMenu.addMenuButton(handWriteItem);
        actionsMenu.addMenuButton(closeViewItem);
    }

    /**
     * 去除主菜单按钮
     */
    private void removeActionBtns(){
        actionsMenu.removeMenuButton(handWriteItem);
        actionsMenu.removeMenuButton(closeViewItem);
    }

    /**
     * 显示手写批注按钮
     */
    private void addHandWriteBtns(){
        actionsMenu.addMenuButton(btnSave);
        actionsMenu.addMenuButton(btnPen);
        actionsMenu.addMenuButton(btnUndo);
        actionsMenu.addMenuButton(btnRedo);
//        actionsMenu.addMenuButton(btnErase);
        actionsMenu.addMenuButton(btnClear);
        actionsMenu.addMenuButton(btnClose);
    }
    private void removeHandWriteBtns(){
        actionsMenu.removeMenuButton(btnSave);
        actionsMenu.removeMenuButton(btnPen);
        actionsMenu.removeMenuButton(btnUndo);
        actionsMenu.removeMenuButton(btnRedo);
//        actionsMenu.removeMenuButton(btnErase);
        actionsMenu.removeMenuButton(btnClear);
        actionsMenu.removeMenuButton(btnClose);
    }

    private void showProgressDialog(){
        if(progressDialog!=null){
            progressDialog.show();
        }
    }
    private void showProgressDialog(String msg){
        if(progressDialog!=null){
            progressDialog.setMessage(msg);
            progressDialog.show();
        }
    }
    private void hideProgressDialog(){
        if(progressDialog!=null){
            progressDialog.hide();
        }
    }


    //广播事件
    public void broadcastIntent(String actionName,String message){
        Intent intent = new Intent();
        intent.setAction(actionName);
        intent.putExtra(KEY_SHOW_MESSAGE,message);
        sendBroadcast(intent);
    }
    public void broadcastIntent(String actionName){
        Intent intent = new Intent();
        intent.setAction(actionName);
        sendBroadcast(intent);
    }

    public class PdfActionReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            String msg = intent.getStringExtra(KEY_SHOW_MESSAGE);
            if(action==null){
                return;
            }
            switch (action){
                case BC_SHOW_LOADING:
                    showProgressDialog();
                    break;
                case BC_HIDE_LOADING:
                    hideProgressDialog();
                    break;
                case BC_HANDWRITING_LIST:
                    hideProgressDialog();
                    List<HandwritingData> dataList = httpUtil.getCommentDataList();
                    if(dataList!=null){
                        for (HandwritingData dataBean:dataList){
                            if(dataBean.getBase64Code()!=null){
                                Bitmap bitmap = ImageTools.base64ToBitmap(dataBean.getBase64Code());
                                dataBean.setImageBitmap(bitmap);
                                pdfView.addHandwritingData(dataBean);
                            }
                        }
                    }
                    //打开PDF文件
                    openPdfFile();
                    break;
                case BC_RESPONSE_FAILURE:
                    hideProgressDialog();
                    if(msg==null){
                        msg = "操作失败";
                    }
                    Toast.makeText(SignPdfView.this,msg,Toast.LENGTH_LONG).show();
                    break;
                case BC_RESPONSE_SUCCESS:
                    hideProgressDialog();
                    if(msg==null){
                        msg = "操作成功";
                    }
                    Toast.makeText(SignPdfView.this,msg,Toast.LENGTH_LONG).show();
                    break;
                case BC_SAVE_HANDWRITING_SUCCESS:
                    hideProgressDialog();
                    pdfView.invalidate();
                    pdfView.signClear();
                    Toast.makeText(SignPdfView.this,"保存成功",Toast.LENGTH_LONG).show();
                    break;
                default:
            }
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(pdfActionReceiver);
    }
}
