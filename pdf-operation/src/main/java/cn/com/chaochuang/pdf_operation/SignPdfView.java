package cn.com.chaochuang.pdf_operation;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.*;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import cn.com.chaochuang.pdf_operation.model.*;
import cn.com.chaochuang.pdf_operation.ui.JumpToFragment;
import cn.com.chaochuang.pdf_operation.ui.MeetingMemberFragment;
import cn.com.chaochuang.pdf_operation.ui.PenSettingFragment;
import cn.com.chaochuang.pdf_operation.utils.Constants;
import cn.com.chaochuang.pdf_operation.utils.ImageTools;
import cn.com.chaochuang.pdf_operation.utils.MeetingWsListener;
import cn.com.chaochuang.pdf_operation.utils.OkHttpUtil;
import com.alibaba.fastjson.JSON;
import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnHandwritingDeleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.model.HandwritingData;
import com.github.barteksc.pdfviewer.util.FitPolicy;
import okhttp3.*;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
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
    private JumpToFragment jumpToFragment;

    private PDFView pdfView;
    private LinearLayout actionsMenu;
    private ImageView arrowLeft,arrowRight;
    private DisplayMetrics outMetrics = new DisplayMetrics();

    private OkHttpUtil httpUtil;
    private MeetingWsListener meetingWsListener;
    private PdfActionReceiver pdfActionReceiver;

    /**
     * 本地数据
     * */
    private SharedPreferences penSettingData;

    private String fileId;
    private String filePath;
    private String serverUrl;
    private String serverToken;
    private String userId;
    private String userName;
    private Boolean penOnly;
    private int currentPage=0;

    /**
     * 会议同步相关
     */
    private String webSocketUrl;
    private String meetingRecordId;
    private Boolean isHost;

    private AppCompatButton handWriteItem,closeViewItem,meetingMemItem,jumpToItem;
    /**
     * 手写批注菜单按钮
     */
    private AppCompatButton btnClose, btnClear, btnUndo, btnRedo, btnSave, btnPen , btnErase;

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

        //http请求服务
        httpUtil = new OkHttpUtil(true,serverToken);

        //菜单工具栏
        actionsMenu = findViewById(R.id.ll_menu_tool);
        arrowLeft = findViewById(R.id.iv_arrow_left);
        arrowRight = findViewById(R.id.iv_arrow_right);
        getWindowManager().getDefaultDisplay().getMetrics(outMetrics);

        //会议同步websocket服务
        if(meetingRecordId!=null&&!"".equals(meetingRecordId.trim())&&webSocketUrl!=null&&!"".equals(webSocketUrl.trim())) {
            meetingWsListener = new MeetingWsListener();
            meetingWsListener.startRunning(this, webSocketUrl + meetingRecordId + "?" + HEADER_TOKEN_NAME + "=" + serverToken);

            if(isHost){
                //是主持人,显示菜单按钮
                initMenuBtn();
            }else{
                //与会人员
                initMeetingBtn();
                pdfView.setNotChangePage(true);
            }
        }else{
            initMenuBtn();
        }

        //加载提示框
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("正在加载");
        progressDialog.setCancelable(false);

        //dialog
        jumpToFragment = new JumpToFragment();
        jumpToFragment.setOnJumpToListener(new JumpToFragment.OnJumpToListener() {
            @Override
            public void jumpTo(int page) {
                pdfView.jumpTo(page-1,true);
            }
        });

        //广播事件
        this.initBroadcastReceiver();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

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

    /**
     * pdf 签批按钮显示
     */
    private void initMenuBtn() {
        //region 手写批注
        handWriteItem = getMenuButton(getResources().getDrawable(R.drawable.ic_pen),"手 写");
        handWriteItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                float penWidth = penSettingData.getFloat(PenSettingFragment.PEN_WIDTH,PenSettingFragment.defaultWidth);
                int penColor = penSettingData.getInt(PenSettingFragment.PEN_COLOR, Color.BLACK);
                pdfView.setSignaturePad(penWidth,penColor,penOnly);
                addHandWriteBtns();
            }
        });
        //endregion

        //region 关闭PDF预览页面
        closeViewItem = getMenuButton(getResources().getDrawable(R.drawable.ic_close),"关 闭");
        closeViewItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        //endregion

        //-------以下是手写批注的按钮-------

        //region 关闭手写页面
        btnClose = getMenuButton(getResources().getDrawable(R.drawable.ic_exit),"退出手写");
        //region 点击事件
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(pdfView.hasHandwriting()){
                    final AlertDialog.Builder builder = new AlertDialog.Builder(SignPdfView.this,AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
                    builder.setMessage("有未保存的手写内容，是否退出");
                    builder.setTitle("退出手写模式");
                    builder.setPositiveButton("确定",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    pdfView.hideSignView();
                                    addActionBtns();
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
                    addActionBtns();
                }

            }
        });
        //endregion
        //endregion

        //region 保存手写签批
        btnSave = getMenuButton(getResources().getDrawable(R.drawable.ic_save),"保 存");
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(pdfView.hasHandwriting()){
                    broadcastIntent(BC_SHOW_LOADING,"正在保存");
                    new Thread(){
                        @Override
                        public void run() {
                            HandwritingData handwritingData = pdfView.getSignBitmap();
                            float wr = pdfView.getDisplayWRadio();
                            float hr = pdfView.getDisplayHRadio();
                            if (handwritingData!=null&&handwritingData.getImageBitmap() != null) {
                                float x = handwritingData.getSignX();
                                float y = handwritingData.getSignY();
                                //获取没有放大或缩小的图片数据
                                Bitmap originBitmap = ImageTools.scaleBitmap(handwritingData.getImageBitmap(),1/pdfView.getZoom());
                                float width = originBitmap.getWidth() / wr;
                                float height = originBitmap.getHeight() / hr;
                                handwritingData.setPx(x/wr);
                                handwritingData.setPy(y/hr);
                                handwritingData.setImageWidth(width);
                                handwritingData.setImageHeight(height);
                                handwritingData.setImageBitmap(originBitmap);
                                handwritingData.setPdfFileHeight(pdfView.getPhysicalPdfHeight());
                                handwritingData.setPdfFileWidth(pdfView.getPhysicalPdfWidth());
                                handwritingData.setPageNo(pdfView.getCurrentPage());
                                handwritingData.setSignerId(userId);
                                handwritingData.setSignerName(userName);
                                handwritingData.setSignTime(new Date());
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
//        btnUndo = getMenuButton(getResources().getDrawable(R.drawable.ic_undo),"撤 销");
//        btnUndo.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                pdfView.signUndo();
//            }
//        });
        //endregion

        //region 手写签批回退
//        btnRedo = getMenuButton(getResources().getDrawable(R.drawable.ic_redo),"恢 复");
//        btnRedo.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                pdfView.signRedo();
//            }
//        });
        //endregion

        //region 手写签批清空
        btnClear = getMenuButton(getResources().getDrawable(R.drawable.ic_clear),"清 空");
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
        btnPen = getMenuButton(getResources().getDrawable(R.drawable.ic_setting),"设 置");
        btnPen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PenSettingFragment penSettingFragment = new PenSettingFragment();
                penSettingFragment.showFragmentDlg(getSupportFragmentManager(), "penSettingFragment", new PenSettingFragment.OnSaveListener() {
                    @Override
                    public void onSaveAction() {
                        float penWidth = penSettingData.getFloat(PenSettingFragment.PEN_WIDTH,PenSettingFragment.defaultWidth);
                        int penColor = penSettingData.getInt(PenSettingFragment.PEN_COLOR, Color.BLACK);
                        penOnly = penSettingData.getBoolean(PenSettingFragment.PEN_ONLY,true);
                        pdfView.setPenSetting(penWidth,penColor,penOnly);
                    }
                });
            }
        });
        //endregion

        //region 设置橡皮擦
        btnErase = getMenuButton(getResources().getDrawable(R.drawable.ic_erase),"橡皮檫");
        btnErase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                eraseFlag = !eraseFlag;
                if(eraseFlag) {
                    Toast.makeText(SignPdfView.this,"已进入橡皮擦模式",Toast.LENGTH_LONG).show();
                    btnErase.setCompoundDrawablesWithIntrinsicBounds(null,getResources().getDrawable(R.drawable.ic_close),null,null);
                    btnErase.setText("退出橡皮擦模式");
                    actionsMenu.removeAllViews();
                    actionsMenu.addView(btnErase);
                }else{
                    Toast.makeText(SignPdfView.this,"已退出橡皮擦模式",Toast.LENGTH_LONG).show();
                    btnErase.setCompoundDrawablesWithIntrinsicBounds(null,getResources().getDrawable(R.drawable.ic_erase),null,null);
                    btnErase.setText("橡皮檫");
                    addHandWriteBtns();
                }
                pdfView.signEraseMode(eraseFlag);
            }
        });
        //endregion

        if(meetingWsListener!=null&&isHost){
            meetingMemItem = getMenuButton(getResources().getDrawable(R.drawable.ic_people),"与会人员");
            meetingMemItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    httpUtil.get(serverUrl + URL_FIND_MEETING_MEMBERS + "?recordId=" + meetingRecordId, new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            broadcastIntent(BC_RESPONSE_FAILURE);
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            if(response.isSuccessful()&&response.body()!=null) {
                                String data = response.body().string();
                                AppResponse resData = JSON.parseObject(data, AppResponse.class);
                                if(AppResponse.RES_SUCCESS.equals(resData.getSuccess())&&resData.getData()!=null){
                                    final List<MeetingMemberData> memberDataList = JSON.parseArray(resData.getData().toString(),MeetingMemberData.class);
                                    SignPdfView.this.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            MeetingMemberFragment fragment = new MeetingMemberFragment();
                                            MeetingMemberAdaptor adaptor = new MeetingMemberAdaptor(getApplicationContext(),memberDataList);
                                            fragment.showFragmentDlg(getSupportFragmentManager(),"meetingMemberList",adaptor);


                                        }
                                    });
                                }else{
                                    broadcastIntent(BC_RESPONSE_FAILURE);
                                }

                            }else {
                                broadcastIntent(BC_RESPONSE_FAILURE);
                            }
                        }
                    });
                }
            });
        }

        jumpToItem = getMenuButton(getResources().getDrawable(R.drawable.ic_jump_to),"跳转到");
        jumpToItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                jumpToFragment.showFragmentDlg(getSupportFragmentManager(),"PdfJumpToFragment",pdfView.getCurrentPage()+1,pdfView.getPageCount());
            }
        });
    }

    /**
     * 会议同步按钮（与会人员）
     */
    private void initMeetingBtn(){

        closeViewItem = getMenuButton(getResources().getDrawable(R.drawable.ic_exit),"退出同步");
        closeViewItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private AppCompatButton getMenuButton(Drawable drawable, String btnName) {
        AppCompatButton menuButton = new AppCompatButton(this);
        menuButton.setBackgroundResource(R.drawable.round_buttom);
        menuButton.setText(btnName);
        menuButton.setTextSize(TypedValue.COMPLEX_UNIT_SP,16);
        menuButton.setCompoundDrawablesWithIntrinsicBounds(null,drawable,null,null);
        return menuButton;
    }

    /**
     * 获取参数
     */
    private void initParams() {
        Intent intent = getIntent();
        if (intent.hasExtra(Constants.KEY_FILE_PATH)) {
            filePath = intent.getStringExtra(Constants.KEY_FILE_PATH);
        }
        if (intent.hasExtra(Constants.PARAM_USER_ID)) {
            userId = intent.getStringExtra(Constants.PARAM_USER_ID);
        }
        if (intent.hasExtra(PARAM_USER_NAME)) {
            userName = intent.getStringExtra(PARAM_USER_NAME);
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
        penOnly = penSettingData.getBoolean(PenSettingFragment.PEN_ONLY,true);
        if (intent.hasExtra(Constants.KEY_WEB_SOCKET_URL)) {
            webSocketUrl = intent.getStringExtra(Constants.KEY_WEB_SOCKET_URL);
        }
        if (intent.hasExtra(Constants.KEY_MEETING_REOCRD_ID)) {
            meetingRecordId = intent.getStringExtra(Constants.KEY_MEETING_REOCRD_ID);
        }
        if (intent.hasExtra(Constants.KEY_MEETING_REOCRD_ID)) {
            isHost = intent.getBooleanExtra(KEY_IS_HOST,false);
        }
        if (intent.hasExtra(KEY_CURRENT_PAGE)) {
            currentPage = intent.getIntExtra(KEY_CURRENT_PAGE,0);
        }
    }


    private void initBroadcastReceiver() {
        pdfActionReceiver = new PdfActionReceiver();

        IntentFilter handwritingListFilter = new IntentFilter(BC_HANDWRITING_LIST);
        IntentFilter resSuccessFilter = new IntentFilter(BC_RESPONSE_SUCCESS);
        IntentFilter showLoadingFilter = new IntentFilter(BC_SHOW_LOADING);
        IntentFilter hideLoadingFilter = new IntentFilter(BC_HIDE_LOADING);
        IntentFilter resFailureFilter = new IntentFilter(BC_RESPONSE_FAILURE);
        IntentFilter saveSuccessFilter = new IntentFilter(BC_SAVE_HANDWRITING_SUCCESS);
        IntentFilter deleteSuccessFilter = new IntentFilter(BC_DELETE_HANDWRITING);
        IntentFilter showTipFilter = new IntentFilter(BC_SHOW_TIP);
        IntentFilter refreshPdfViewFilter = new IntentFilter(BC_REFRESH_PDF_VIEW);

        registerReceiver(pdfActionReceiver,handwritingListFilter);
        registerReceiver(pdfActionReceiver,resFailureFilter);
        registerReceiver(pdfActionReceiver,resSuccessFilter);
        registerReceiver(pdfActionReceiver,showLoadingFilter);
        registerReceiver(pdfActionReceiver,hideLoadingFilter);
        registerReceiver(pdfActionReceiver,saveSuccessFilter);
        registerReceiver(pdfActionReceiver,deleteSuccessFilter);
        registerReceiver(pdfActionReceiver,showTipFilter);
        registerReceiver(pdfActionReceiver,refreshPdfViewFilter);
    }

    public void openPdfFile(){
        pdfView.fromFile(new File(filePath))
                .defaultPage(currentPage)
                .swipeHorizontal(true)
                .pageSnap(true)
                .pageFling(true)
                .enableAnnotationRendering(false)
                .scrollHandle(null)
                .spacing(0)
                .autoSpacing(true)
                .userData(userId)
                .onPageChange(new OnPageChangeListener() {
                    @Override
                    public void onPageChanged(int page, int pageCount) {
                        currentPage = page;
                        if(meetingWsListener!=null&&isHost){
                            WebSocketMessage webSocketMessage = new WebSocketMessage();
                            webSocketMessage.setPageNo(page);
                            webSocketMessage.setMessageType(WebSocketMessage.TYPE_PAGE_CHANGE);
                            webSocketMessage.setRecordId(meetingRecordId);
                            meetingWsListener.sendMessage(webSocketMessage);
                        }
                    }
                })
                .handwritingDeleteListener(new OnHandwritingDeleteListener() {
                    @Override
                    public void onDelete(final String id) {
                        final AlertDialog.Builder builder = new AlertDialog.Builder(SignPdfView.this,AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
                        builder.setMessage("是否删除批注");
                        builder.setTitle("删除");
                        builder.setPositiveButton("确定",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        deleteCommentById(id);
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
                    }
                })
                //宽度自适应（不可修改，修改后插入手写坐标会发生变化）
                .pageFitPolicy(FitPolicy.WIDTH).load();
    }

    private void saveHandwritingData(final HandwritingData handwritingData) {
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
            saveBean.setSignerId(handwritingData.getSignerId());
            saveBean.setSignerName(handwritingData.getSignerName());
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
                        String data = response.body().string();
                        AppResponse resData = JSON.parseObject(data, AppResponse.class);
                        if(resData.getData()!=null){
                            //添加到图层
                            handwritingData.setId(resData.getData().toString());
                            pdfView.addHandwritingData(handwritingData);
                            broadcastIntent(BC_SAVE_HANDWRITING_SUCCESS,handwritingData.getId());
                        }else{
                            broadcastIntent(BC_RESPONSE_FAILURE);
                        }

                    }else {
                        broadcastIntent(BC_RESPONSE_FAILURE);
                    }
                }
            });
        }
    }

    /**
     * 添加一个手写签批到屏幕上显示
     * @param handwritingData
     */
    public void addHandwritingData(HandwritingData handwritingData) {
        if(handwritingData.getBase64Code()!=null&&!"".equals(handwritingData.getBase64Code().trim())){
            Bitmap bitmap = ImageTools.base64ToBitmap(handwritingData.getBase64Code());
            handwritingData.setImageBitmap(bitmap);
        }
        pdfView.addHandwritingData(handwritingData);
        broadcastIntent(BC_REFRESH_PDF_VIEW);
    }

    public void removeHandwritingData(String id){
        pdfView.removeHandwritingData(id);
        broadcastIntent(BC_REFRESH_PDF_VIEW);
    }

    public void jumpToPage(int page){
        pdfView.jumpTo(page,true);
    }

    private void deleteCommentById(final String id){
        if(id!=null){
            broadcastIntent(BC_SHOW_LOADING,"正在删除");
            httpUtil.get(serverUrl + URL_HANDWRITING_DELETE + "?id="+id, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    broadcastIntent(BC_RESPONSE_FAILURE);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if(response.isSuccessful()&&response.body()!=null) {
                        String data = response.body().string();
                        AppResponse resData = JSON.parseObject(data, AppResponse.class);
                        if(AppResponse.RES_SUCCESS.equals(resData.getSuccess())){
                            broadcastIntent(BC_DELETE_HANDWRITING,id);
                        }else{
                            broadcastIntent(BC_RESPONSE_FAILURE);
                        }

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
                        if(!dataMap.isEmpty()){
                            File pdfFile = new File(pdfRoot,dataMap.get("mdfCode")+"/"+dataMap.get("fileName"));
                            if(pdfFile.exists()){
                                filePath = pdfFile.getAbsolutePath();
                                findHandwritingData();
                            }else{
                                //下载文件
                                downloadFile(pdfFile);
                            }
                        }else{
                            broadcastIntent(BC_RESPONSE_FAILURE,"文件打开失败");
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
        if (meetingRecordId != null && !"".equals(meetingRecordId.trim())) {
            findMeetingHandwriting();
        } else {
            findPdfHandwriting();
        }
    }

    private void findPdfHandwriting(){
        //查询批注数据
        broadcastIntent(BC_SHOW_LOADING,"正在打开文件");
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

    private void findMeetingHandwriting(){
        //查询批注数据
        broadcastIntent(BC_SHOW_LOADING,"正在打开文件");
        httpUtil.get(serverUrl + Constants.URL_FIND_MEETING_HANDWRITING+"?fileId="+fileId+"&recordId="+meetingRecordId, new Callback() {
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
                        PdfCommentBean commentBean = JSON.parseObject(resData.getData().toString(),PdfCommentBean.class);
                        httpUtil.setCommentDataList(commentBean.getHandwritingList());
                        if(commentBean.getCurPage()!=null) {
                            currentPage = commentBean.getCurPage();
                        }
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
     * 显示主菜单按钮
     */
    private void addActionBtns(){
        actionsMenu.removeAllViews();
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT);
        layoutParams.gravity = Gravity.CENTER;
        actionsMenu.setLayoutParams(layoutParams);
        arrowLeft.setVisibility(View.INVISIBLE);
        arrowRight.setVisibility(View.INVISIBLE);

        if(meetingRecordId!=null&&!isHost){
            //会议模式(与会人员)
            actionsMenu.addView(jumpToItem);
            actionsMenu.addView(closeViewItem);
        }else if(meetingRecordId!=null){
            //会议模式（主持人）
            actionsMenu.addView(handWriteItem);
            if(meetingMemItem!=null){
                actionsMenu.addView(meetingMemItem);
            }
            actionsMenu.addView(jumpToItem);
            actionsMenu.addView(closeViewItem);
        }else{
            actionsMenu.addView(handWriteItem);
            actionsMenu.addView(jumpToItem);
            actionsMenu.addView(closeViewItem);
        }
    }

    /**
     * 显示手写批注按钮
     */
    private void addHandWriteBtns(){
        actionsMenu.removeAllViews();
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT);

        if(handWriteItem.getWidth()*6>outMetrics.widthPixels){
            arrowLeft.setVisibility(View.VISIBLE);
            arrowRight.setVisibility(View.VISIBLE);
            layoutParams.gravity = Gravity.START;
        }else{
            arrowLeft.setVisibility(View.INVISIBLE);
            arrowRight.setVisibility(View.INVISIBLE);
            layoutParams.gravity = Gravity.CENTER;
        }

        actionsMenu.setLayoutParams(layoutParams);
        actionsMenu.addView(btnClose);
        actionsMenu.addView(btnSave);
//        actionsMenu.addView(btnUndo);
//        actionsMenu.addView(btnRedo);
        actionsMenu.addView(btnErase);
        actionsMenu.addView(btnClear);
        actionsMenu.addView(btnPen);
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
                    showProgressDialog(msg);
                    break;
                case BC_HIDE_LOADING:
                    hideProgressDialog();
                    break;
                case BC_HANDWRITING_LIST:
                    hideProgressDialog();
                    List<HandwritingData> dataList = httpUtil.getCommentDataList();
                    if(dataList!=null){
                        for (HandwritingData dataBean:dataList){
                            addHandwritingData(dataBean);
                        }
                    }
                    //打开PDF文件
                    openPdfFile();
                    //显示按钮
                    addActionBtns();
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
                    //刷新页面
                    pdfView.invalidate();
                    pdfView.signClear();
                    Toast.makeText(SignPdfView.this,"保存成功",Toast.LENGTH_LONG).show();
                    if(meetingWsListener!=null&&isHost){
                        //同步模式
                        meetingWsListener.sendMessage(WebSocketMessage.TYPE_HANDWRITING_ADD,meetingRecordId,msg);
                    }
                    break;
                case BC_DELETE_HANDWRITING:
                    hideProgressDialog();
                    removeHandwritingData(msg);
                    if(meetingWsListener!=null&&isHost){
                        //同步模式
                        meetingWsListener.sendMessage(WebSocketMessage.TYPE_HANDWRITING_DELETE,meetingRecordId,msg);
                    }
                    break;
                case BC_SHOW_TIP:
                    Toast.makeText(SignPdfView.this,msg,Toast.LENGTH_LONG).show();
                    AlertDialog.Builder builder = new AlertDialog.Builder(SignPdfView.this,AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
                    builder.setMessage(msg);
                    builder.setTitle("提示信息");
                    builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    final Dialog dialog = builder.create();
                    dialog.setCancelable(false);
                    dialog.show();
                    break;
                case BC_REFRESH_PDF_VIEW:
                    if(pdfView!=null){
                        pdfView.invalidate();
                    }
                    break;
                case BC_CHANGE_PAGE:
                    int page = intent.getIntExtra(KEY_PDF_PAGE,0);
                    if(pdfView!=null) {
                        pdfView.jumpTo(page, true);
                    }
                    break;
                default:
            }
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(pdfActionReceiver);
        if(meetingWsListener!=null) {
            meetingWsListener.closeSocket();
        }
    }
}
