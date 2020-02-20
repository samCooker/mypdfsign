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
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
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
import cn.com.chaochuang.pdf_operation.ui.TextInputFragment;
import cn.com.chaochuang.pdf_operation.utils.Constants;
import cn.com.chaochuang.pdf_operation.utils.ImageTools;
import cn.com.chaochuang.pdf_operation.utils.MeetingWsListener;
import cn.com.chaochuang.pdf_operation.utils.OkHttpUtil;
import cn.com.chaochuang.writingpen.model.CommentData;
import com.alibaba.fastjson.JSON;
import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnHandwritingDeleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.listener.OnTapListener;
import com.github.barteksc.pdfviewer.util.FitPolicy;
import okhttp3.*;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
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
    private MyHandler actionHandler;

    private PenSettingFragment penSettingFragment;
    private TextInputFragment textInputFragment;

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

    /**
     * 公文相关
     */
    private String flowInstId;
    private String nodeInstId;

    /**
     * 是否为公文手写签批（显示提交按钮）
     */
    private boolean isDocMode = false;
    private boolean isTextInput = false;

    private AppCompatButton handWriteItem,closeViewItem,meetingMemItem,jumpToItem,textInputItem;
    /**
     * 手写批注菜单按钮
     */
    private AppCompatButton btnClose, btnClear, btnSave, btnPen , btnErase , btnSubmit;

    private boolean eraseFlag = false;
    private boolean isDestroy = false;

    private int REQ_CODE=100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isDestroy = false;
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

        //事件
        actionHandler = new MyHandler(this);

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
        }else{
            downloadOrOpenFile();
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

        initCommonBtn();

        //region 手写批注
        handWriteItem = getMenuButton(getResources().getDrawable(R.drawable.ic_pen),"手 写");
        handWriteItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                float penWidth = penSettingData.getFloat(PenSettingFragment.PEN_WIDTH,PenSettingFragment.defaultWidth);
                int penColor = penSettingData.getInt(PenSettingFragment.PEN_COLOR, Color.BLACK);
                int penType = penSettingData.getInt(PenSettingFragment.PEN_TYPE,PenSettingFragment.STROKE_TYPE_PEN);
                pdfView.setSignaturePad(penWidth,penColor,penOnly,penType);
                addHandWriteBtns();
            }
        });
        //endregion

        //region 关闭PDF预览页面
        closeViewItem = getMenuButton(getResources().getDrawable(R.drawable.ic_close),"关 闭");
        closeViewItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(100);
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
                    sendMessage(MSG_SHOW_LOADING,"正在保存");
                    new Thread(){
                        @Override
                        public void run() {
                            CommentData handwritingData = pdfView.getSignBitmap();
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
                                sendMessage(MSG_HIDE_LOADING,null);
                            }
                        }
                    }.start();
                }else{
                    Toast.makeText(SignPdfView.this,"无手写内容",Toast.LENGTH_LONG).show();
                }

            }
        });
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
        penSettingFragment = new PenSettingFragment();
        penSettingFragment.setOnSaveListener(new PenSettingFragment.OnSaveListener() {
            @Override
            public void onSaveAction() {
                float penWidth = penSettingData.getFloat(PenSettingFragment.PEN_WIDTH,PenSettingFragment.defaultWidth);
                int penColor = penSettingData.getInt(PenSettingFragment.PEN_COLOR, Color.BLACK);
                int penType = penSettingData.getInt(PenSettingFragment.PEN_TYPE,PenSettingFragment.STROKE_TYPE_PEN);
                penOnly = penSettingData.getBoolean(PenSettingFragment.PEN_ONLY,true);
                pdfView.setPenSetting(penWidth,penColor,penOnly,penType);
            }
        });
        btnPen = getMenuButton(getResources().getDrawable(R.drawable.ic_setting),"设 置");
        btnPen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                penSettingFragment.showFragmentDlg(getSupportFragmentManager(), "penSettingFragment");
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
                            sendMessage(MSG_RESPONSE_MSG,"操作失败");
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
                                    sendMessage(MSG_RESPONSE_MSG,"操作失败");
                                }

                            }else {
                                sendMessage(MSG_RESPONSE_MSG,"操作失败");
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

        //公文提交按钮
        if(isDocMode){
            btnSubmit = getMenuButton(getResources().getDrawable(R.drawable.ic_submit),"提 交");
            btnSubmit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.putExtra("docSubmitFlag",true);
                    setResult(100,intent);
                    finish();
                }
            });
        }

        textInputFragment = new TextInputFragment();
        textInputFragment.setOnSaveListener(new TextInputFragment.OnSaveListener() {
            @Override
            public void onTextDataSave(CommentData textData) {
                //保存文字批注
                pdfView.addTextData(textData);
            }
        });
        textInputItem = getMenuButton(getResources().getDrawable(R.drawable.ic_text),"文 字");
        textInputItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isTextInput = true;
                Toast.makeText(SignPdfView.this,"请点击需要插入文字的位置",Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * 会议同步按钮（与会人员）
     */
    private void initMeetingBtn(){
        initCommonBtn();
        closeViewItem = getMenuButton(getResources().getDrawable(R.drawable.ic_exit),"退出同步");
        closeViewItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void initCommonBtn(){

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
        if (intent.hasExtra(KEY_IS_DOC_MODE)) {
            isDocMode = intent.getBooleanExtra(KEY_IS_DOC_MODE,false);
        }

        //公文相关
        if (intent.hasExtra(KEY_FLOW_INST_ID)) {
            flowInstId = intent.getStringExtra(KEY_FLOW_INST_ID);
        }
        if (intent.hasExtra(KEY_NODE_INST_ID)) {
            nodeInstId = intent.getStringExtra(KEY_NODE_INST_ID);
        }
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
                .onTap(new OnTapListener() {
                    @Override
                    public boolean onTap(MotionEvent e) {
                        boolean _canEdit = pdfView.checkCanEdit(e.getX(),e.getY());
                        if(isTextInput&&!_canEdit){
                            //新增
                            CommentData commentData = new CommentData();
                            textInputFragment.showInputFragment(commentData,getSupportFragmentManager(),"textInputFragment",false);
                        }
                        isTextInput = false;
                        return false;
                    }
                })
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

    private void saveHandwritingData(final CommentData handwritingData) {
        if(handwritingData!=null&&handwritingData.getImageBitmap()!=null) {
            CommentData saveBean = new CommentData();
            saveBean.setFileId(fileId);
            saveBean.setFlowInstId(flowInstId);
            saveBean.setNodeInstId(nodeInstId);
            saveBean.setPageNo(handwritingData.getPageNo());
            saveBean.setPx(handwritingData.getPx());
            saveBean.setPy(handwritingData.getPy());
            saveBean.setImageWidth(handwritingData.getImageWidth());
            saveBean.setImageHeight(handwritingData.getImageHeight());
            saveBean.setPdfFileWidth(handwritingData.getPdfFileWidth());
            saveBean.setPdfFileHeight(handwritingData.getPdfFileHeight());
            saveBean.setSignerId(handwritingData.getSignerId());
            saveBean.setSignerName(handwritingData.getSignerName());
            saveBean.setSignType(CommentData.TYPE_HANDWRITING);
            String base64Str = ImageTools.bitmapToBase64(handwritingData.getImageBitmap());
            saveBean.setSignContent(base64Str);
            httpUtil.post(serverUrl + URL_HANDWRITING_SAVE, "jsonData="+JSON.toJSONString(saveBean), new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    sendMessage(MSG_RESPONSE_MSG,"操作失败");
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
                            sendMessage(MSG_SAVE_COMMENT_LIST,handwritingData.getId());
                        }else{
                            sendMessage(MSG_RESPONSE_MSG,"操作失败");
                        }

                    }else {
                        sendMessage(MSG_RESPONSE_MSG,"操作失败");
                    }
                }
            });
        }
    }

    /**
     * 添加一个手写签批到屏幕上显示
     * @param commentData
     */
    public void addHandwritingDataAndRefresh(CommentData commentData) {
        if(CommentData.TYPE_HANDWRITING.equals(commentData.getSignType())){
            if(commentData.getSignContent()!=null&&!"".equals(commentData.getSignContent().trim())){
                Bitmap bitmap = ImageTools.base64ToBitmap(commentData.getSignContent());
                commentData.setImageBitmap(bitmap);
            }
            pdfView.addHandwritingData(commentData);
        }else if(CommentData.TYPE_TEXT.equals(commentData.getSignType())){
            pdfView.addTextData(commentData);
        }

        sendMessage(MSG_REFRESH_PDF_VIEW,null);
    }

    public void removeHandwritingData(String id){
        pdfView.removeHandwritingData(id);
        sendMessage(MSG_REFRESH_PDF_VIEW,null);
    }

    public void jumpToPage(int page){
        pdfView.jumpTo(page,true);
    }

    private void deleteCommentById(final String id){
        if(id!=null){
            sendMessage(MSG_SHOW_LOADING,"正在删除");
            httpUtil.get(serverUrl + URL_HANDWRITING_DELETE + "?id="+id, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    sendMessage(MSG_RESPONSE_MSG,"操作失败");
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if(response.isSuccessful()&&response.body()!=null) {
                        String data = response.body().string();
                        AppResponse resData = JSON.parseObject(data, AppResponse.class);
                        if(AppResponse.RES_SUCCESS.equals(resData.getSuccess())){
                            sendMessage(MSG_DEL_COMMENT_LIST,id);
                        }else{
                            sendMessage(MSG_RESPONSE_MSG,"操作失败");
                        }

                    }else {
                        sendMessage(MSG_RESPONSE_MSG,"操作失败");
                    }
                }
            });
        }
    }

    /**
     * 下载文件
     */
    private void downloadOrOpenFile(){
        sendMessage(MSG_SHOW_LOADING,"正在下载文件");
        httpUtil.get(serverUrl + URL_GET_MD5 + "?fileId=" + fileId, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                sendMessage(MSG_RESPONSE_MSG,"文件下载失败，请尝试重新打开");
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
                            sendMessage(MSG_RESPONSE_MSG,"文件打开失败");
                        }

                    }
                }else{
                    sendMessage(MSG_RESPONSE_MSG,"文件下载失败，请尝试重新打开");
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
                sendMessage(MSG_RESPONSE_MSG,"文件下载失败，请尝试重新打开");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                ResponseBody responseBody = null;
                BufferedInputStream bis = null;
                FileOutputStream fos = null;
                try {
                    if (call.isCanceled()) {
                        sendMessage(MSG_RESPONSE_MSG,"已取消文件下载");
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
                        sendMessage(MSG_HIDE_LOADING,null);
                        filePath = file.getAbsolutePath();
                        findHandwritingData();
                    } else {
                        sendMessage(MSG_HIDE_LOADING,null);
                        sendMessage(MSG_RESPONSE_MSG,"文件下载失败");
                    }
                } catch (Exception e) {
                    sendMessage(MSG_HIDE_LOADING,null);
                    sendMessage(MSG_RESPONSE_MSG,"文件下载异常");
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
        sendMessage(MSG_SHOW_LOADING,"正在打开文件");
        httpUtil.get(serverUrl + Constants.URL_HANDWRITING_LIST+"?fileId="+fileId, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                sendMessage(MSG_RESPONSE_MSG,"请求错误");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(response.isSuccessful()&&response.body()!=null) {
                    String data = response.body().string();
                    AppResponse resData = JSON.parseObject(data, AppResponse.class);
                    if(resData.getData()!=null) {
                        PdfCommentBean commentBean = JSON.parseObject(resData.getData().toString(),PdfCommentBean.class);
                        httpUtil.setHandwritingList(commentBean.getHandwritingList());
                        httpUtil.setTextDataList(commentBean.getTextDataList());
                    }
                    sendMessage(MSG_FIND_COMMENT_LIST,null);
                }else {
                    String msg = "文件打开失败，请尝试重新打开";
                    sendMessage(MSG_RESPONSE_MSG,msg);
                }
            }
        });
    }

    private void findMeetingHandwriting(){
        //查询批注数据
        sendMessage(MSG_SHOW_LOADING,"正在打开文件");
        httpUtil.get(serverUrl + Constants.URL_FIND_MEETING_HANDWRITING+"?fileId="+fileId+"&recordId="+meetingRecordId, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                sendMessage(MSG_RESPONSE_MSG,"请求错误");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(response.isSuccessful()&&response.body()!=null) {
                    String data = response.body().string();
                    AppResponse resData = JSON.parseObject(data, AppResponse.class);
                    if(resData.getData()!=null) {
                        PdfCommentBean commentBean = JSON.parseObject(resData.getData().toString(),PdfCommentBean.class);
                        httpUtil.setHandwritingList(commentBean.getHandwritingList());
                        httpUtil.setTextDataList(commentBean.getTextDataList());
                        if(commentBean.getCurPage()!=null) {
                            currentPage = commentBean.getCurPage();
                        }
                    }
                    sendMessage(MSG_FIND_COMMENT_LIST,null);
                }else {
                    String msg = "文件打开失败，请尝试重新打开";
                    sendMessage(MSG_RESPONSE_MSG,msg);
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
            if(btnSubmit!=null) {
                actionsMenu.addView(btnSubmit);
            }
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

    private void httpResponseMsg(String msg){
        hideProgressDialog();
        Toast.makeText(SignPdfView.this,msg,Toast.LENGTH_LONG).show();
    }

    private void refreshPdfView(){
        if(pdfView!=null){
            pdfView.invalidate();
        }
    }

    private void showConfirmDialog(String msg){
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
        dialog.setCancelable(true);
        dialog.show();
    }

    /**
     * 查询批注成功
     */
    private void findCommentSuccess(){
        hideProgressDialog();
        List<CommentData> dataList = httpUtil.getHandwritingList();
        if(dataList!=null){
            for (CommentData dataBean:dataList){
                if(CommentData.TYPE_HANDWRITING.equals(dataBean.getSignType())){
                    if(dataBean.getSignContent()!=null&&!"".equals(dataBean.getSignContent().trim())){
                        Bitmap bitmap = ImageTools.base64ToBitmap(dataBean.getSignContent());
                        dataBean.setImageBitmap(bitmap);
                    }
                    pdfView.addHandwritingData(dataBean);
                }else if(CommentData.TYPE_TEXT.equals(dataBean.getSignType())){
                    pdfView.addTextData(dataBean);
                }
            }
        }
        //打开PDF文件
        openPdfFile();
        //显示按钮
        addActionBtns();
    }

    /**
     * 批注保存成功
     */
    private void saveCommentSuccess(String id){
        hideProgressDialog();
        //刷新页面
        pdfView.invalidate();
        pdfView.signClear();
        Toast.makeText(SignPdfView.this,"保存成功",Toast.LENGTH_LONG).show();
        if(meetingWsListener!=null&&isHost){
            //同步模式
            meetingWsListener.sendMessage(WebSocketMessage.TYPE_HANDWRITING_ADD,meetingRecordId,id);
        }
        //退出签批模式
        pdfView.hideSignView();
        addActionBtns();
    }

    /**
     * 删除批注成功
     * @param id
     */
    private void deleteCommentSuccess(String id){
        hideProgressDialog();
        pdfView.removeHandwritingData(id);
        if(pdfView!=null){
            pdfView.invalidate();
        }
        if(meetingWsListener!=null&&isHost){
            //同步模式
            meetingWsListener.sendMessage(WebSocketMessage.TYPE_HANDWRITING_DELETE,meetingRecordId,id);
        }
    }

    /**
     * 发送消息
     * @param what
     * @param message
     */
    public void sendMessage(int what,String message){
        Message msg = Message.obtain();
        msg.what = what;
        msg.obj = message;
        actionHandler.sendMessage(msg);
    }
    public void sendMessage(Message msg){
        actionHandler.sendMessage(msg);
    }
    /**
     *
     */
    private static class MyHandler extends Handler{

        private final WeakReference<SignPdfView> signPdfViewRef;

        public MyHandler(SignPdfView signPdfView) {
            signPdfViewRef = new WeakReference<>(signPdfView);
        }

        @Override
        public void handleMessage(Message msg) {
            SignPdfView signPdfView = signPdfViewRef.get();
            if(signPdfView.isDestroy){
                return;
            }
            Object dataObj = msg.obj;
            switch (msg.what){
                case MSG_FIND_COMMENT_LIST:
                    signPdfView.findCommentSuccess();
                    break;
                case MSG_SAVE_COMMENT_LIST:
                    if(dataObj!=null) {
                        signPdfView.saveCommentSuccess(dataObj.toString());
                    }
                    break;
                case MSG_DEL_COMMENT_LIST:
                    if(dataObj!=null) {
                        signPdfView.deleteCommentSuccess(dataObj.toString());
                    }
                    break;
                case MSG_SHOW_CONFIRM_DLG:
                    if(dataObj!=null){
                        signPdfView.showConfirmDialog(dataObj.toString());
                    }
                    break;
                case MSG_HIDE_LOADING:
                    signPdfView.hideProgressDialog();
                    break;
                case MSG_SHOW_LOADING:
                    if(dataObj!=null) {
                        signPdfView.showProgressDialog(dataObj.toString());
                    }
                    break;
                case MSG_REFRESH_PDF_VIEW:
                    signPdfView.refreshPdfView();
                    break;
                case MSG_PDF_PAGE_CHANGE:
                    signPdfView.jumpToPage(msg.arg1);
                    break;
                case MSG_RESPONSE_MSG:
                    if(dataObj!=null) {
                        signPdfView.httpResponseMsg(dataObj.toString());
                    }
                    break;
                default:
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isDestroy = true;
        if(meetingWsListener!=null) {
            meetingWsListener.closeSocket();
        }
        if(progressDialog!=null&&progressDialog.isShowing()){
            progressDialog.dismiss();
        }

        setResult(1001);
    }
}
