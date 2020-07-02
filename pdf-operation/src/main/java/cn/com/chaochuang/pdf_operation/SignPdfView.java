package cn.com.chaochuang.pdf_operation;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
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
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.bigkoo.alertview.AlertView;
import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnDrawListener;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnLongPressListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.listener.OnTapListener;
import com.github.barteksc.pdfviewer.util.FitPolicy;
import com.shockwave.pdfium.util.SizeF;

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

import cn.com.chaochuang.pdf_operation.model.AppResponse;
import cn.com.chaochuang.pdf_operation.model.EntryData;
import cn.com.chaochuang.pdf_operation.model.PdfCommentBean;
import cn.com.chaochuang.pdf_operation.ui.EraseSettingFragment;
import cn.com.chaochuang.pdf_operation.ui.FontTextView;
import cn.com.chaochuang.pdf_operation.ui.JumpToFragment;
import cn.com.chaochuang.pdf_operation.ui.PenSettingFragment;
import cn.com.chaochuang.pdf_operation.ui.TextInputFragment;
import cn.com.chaochuang.pdf_operation.ui.actionsheet.ActionSheet;
import cn.com.chaochuang.pdf_operation.ui.actionsheet.OnActionListener;
import cn.com.chaochuang.pdf_operation.ui.listener.OnClickItemListener;
import cn.com.chaochuang.pdf_operation.utils.Constants;
import cn.com.chaochuang.pdf_operation.utils.ImageTools;
import cn.com.chaochuang.pdf_operation.utils.OkHttpUtil;
import cn.com.chaochuang.writingpen.model.CommentData;
import cn.com.chaochuang.writingpen.model.SignBitmapData;
import cn.com.chaochuang.writingpen.ui.DrawPenView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import static cn.com.chaochuang.pdf_operation.utils.Constants.*;

/**
 * 2019-4-23
 *
 * @author Shicx
 */

public class SignPdfView extends AppCompatActivity implements OnDrawListener, OnTapListener, OnLongPressListener, OnLoadCompleteListener {

    private static final String TAG = SignPdfView.class.getSimpleName();

    private ProgressDialog progressDialog;
    private JumpToFragment jumpToFragment;

    private PDFView pdfView;
    private DrawPenView drawPenView;
    private LinearLayout actionsMenu;
    private ImageView arrowLeft,arrowRight;
    private DisplayMetrics outMetrics = new DisplayMetrics();

    private OkHttpUtil httpUtil;
    private MyHandler actionHandler;

    private PenSettingFragment penSettingFragment;
    private EraseSettingFragment eraseSettingFragment;

    /**
     * 文字输入弹窗
     */
    private TextInputFragment textInputDlg;
    private FontTextView textView;
    private float txtX,txtY;
    public static Typeface simsunTypeface;

    private Paint paint;
    private Paint commentPaint;

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
    private int currentPage=0;
    private boolean updatePdfFlag =false;
    //隐藏文字批注
    private boolean hideTextBtn=false;

    /**
     * 公文相关
     */
    private String flowInstId;
    private String nodeInstId;

    /**
     * 是否为公文手写签批（显示提交按钮）
     */
    private boolean isDocMode = false;
    private boolean modifyFlag = false;

    private boolean readMode = false;
    private boolean hideAnnot = false;

    private List<EntryData> entryDataList;

    private AppCompatButton closeViewItem,jumpToItem;
    /**
     * 手写批注菜单按钮
     */
    private AppCompatButton btnSave, btnPen , btnErase , btnEraseSetting, btnSubmit, btnTextInput,btnEditText;

    /**
     * 顶部提示栏
     */
    private TextView barTextView;

    private boolean eraseFlag = false;
    private boolean textInputFlag = false;
    private boolean isDestroy = false;

    /**
     * 手写签批 虚线边框padding
     * */
    private float editLinePadding = 10f;
    private ActionSheet editActionSheet;
    private ActionSheet infoActionSheet;
    private CommentData editComment;

    private int REQ_CODE=100;
    private long exitTime;

    private int screenWidth;
    private int screenHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);

        isDestroy = false;
        setContentView(R.layout.act_sign_pdf);

        //获取本地数据
        penSettingData = getSharedPreferences(PenSettingFragment.PEN_SETTING_DATA, Context.MODE_PRIVATE);

        this.initParams();

        pdfView = findViewById(R.id.pdf_view);

        drawPenView = findViewById(R.id.view_handwriting);

        simsunTypeface = Typeface.createFromAsset(getAssets(),"simsun.ttc");

        paint = new Paint();
        commentPaint = new Paint();
        commentPaint.setStyle(Paint.Style.STROKE);
        commentPaint.setStrokeWidth(4f);
        commentPaint.setColor(getResources().getColor(R.color.pdf_comment_border));
        commentPaint.setPathEffect(new DashPathEffect(new float[]{4, 4}, 0));

        //http请求服务
        httpUtil = new OkHttpUtil(true,serverToken);

        //菜单工具栏
        actionsMenu = findViewById(R.id.ll_menu_tool);
        arrowLeft = findViewById(R.id.iv_arrow_left);
        arrowRight = findViewById(R.id.iv_arrow_right);
        getWindowManager().getDefaultDisplay().getMetrics(outMetrics);

        initMenuBtn();
        initPenStyle();
        initEraseWidth();

        //加载提示框
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("正在加载");
        progressDialog.setCancelable(false);

        //顶部提示栏
        barTextView = findViewById(R.id.bar_msg_tv);

        //dialog
        jumpToFragment = new JumpToFragment();
        jumpToFragment.setOnJumpToListener(new JumpToFragment.OnJumpToListener() {
            @Override
            public void jumpTo(int page) {
                pdfView.jumpTo(page-1,true);
            }
        });

        //
        editActionSheet = new ActionSheet(this);
        editActionSheet.setTitle("操作");
        editActionSheet.setRootView(pdfView);
        editActionSheet.setTouchListener(new ActionSheet.ActionSheetTouchListener() {
            @Override
            public void onDismiss() {
                editComment = null;
                pdfView.invalidate();
            }
        });
        editActionSheet.addAction("删除", ActionSheet.Style.DESTRUCTIVE, new OnActionListener() {
            @Override
            public void onSelected(ActionSheet actionSheet, String title) {
                Log.d("actionsheet",title);
                if("删除".equals(title)){
                    if(editComment!=null){
                        deleteCommentById(editComment.getId());
                    }
                    editComment = null;
                    actionSheet.dismiss();
                }
            }
        });

        //
        infoActionSheet = new ActionSheet(this);
        infoActionSheet.setTitle("信息");
        infoActionSheet.setRootView(pdfView);
        infoActionSheet.setTouchListener(new ActionSheet.ActionSheetTouchListener() {
            @Override
            public void onDismiss() {
                editComment = null;
                pdfView.invalidate();
            }
        });

        //事件
        actionHandler = new MyHandler(this);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //屏幕宽高
        Display display = getWindowManager().getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        screenWidth = point.x;
        screenHeight = point.y;

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

        //region 跳转到按钮
        jumpToItem = getMenuButton(getResources().getDrawable(R.drawable.ic_jump_to), "跳转到");
        jumpToItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                jumpToFragment.showFragmentDlg(getSupportFragmentManager(), "PdfJumpToFragment", pdfView.getCurrentPage() + 1, pdfView.getPageCount());
            }
        });
        //endregion

        //region 关闭PDF预览页面
        closeViewItem = getMenuButton(getResources().getDrawable(R.drawable.ic_close), "关 闭");
        closeViewItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closePdfView();
            }
        });
        //endregion

        //公文提交按钮
        if (isDocMode) {
            btnSubmit = getMenuButton(getResources().getDrawable(R.drawable.ic_submit), "提 交");
            btnSubmit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CommentData handwritingData = getSignBitmapFromSignView();
                    if (handwritingData != null && handwritingData.getImageBitmap() != null) {
                        sendMessage(MSG_SHOW_LOADING, "正在保存");
                        //保存到远程服务器
                        handwritingData.setSignType(CommentData.TYPE_HANDWRITING);
                        saveHandwritingData(handwritingData,MSG_SAVE_COMMENT_AND_SUBMIT);
                    }else{
                        submitDocAndBack();
                    }
                }
            });
        }

        //-------以下是手写批注的按钮-------

        //region 保存手写签批
        btnSave = getMenuButton(getResources().getDrawable(R.drawable.ic_save), "保 存");
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread() {
                    @Override
                    public void run() {
                        CommentData commentData = null;
                        if(textInputFlag&&textView!=null){
                            commentData = textView.getSaveDate();
                            if(commentData!=null) {
                                commentData.setSignX(pdfView.getTextSignX(commentData.getSignX()));
                                commentData.setSignY(pdfView.getTextSignY(commentData.getSignY()));
                                commentData.setSignType(CommentData.TYPE_TEXT);
                            }
                        }else if(drawPenView.getHasDraw()){
                            commentData = getSignBitmapFromSignView();
                            if(commentData!=null) {
                                commentData.setSignType(CommentData.TYPE_HANDWRITING);
                            }
                        }

                        if (commentData != null && commentData.getImageBitmap() != null) {
                            sendMessage(MSG_SHOW_LOADING, "正在保存");
                            //保存到远程服务器
                            saveHandwritingData(commentData,MSG_SAVE_COMMENT_LIST);
                        } else {
                            sendMessage(MSG_SHOW_CONFIRM_DLG,"没有需要保存的内容");
                        }

                    }
                }.start();

            }
        });
        //endregion

        //region 设置画笔样式
        penSettingFragment = new PenSettingFragment();
        penSettingFragment.setOnSaveListener(new PenSettingFragment.OnSaveListener() {
            @Override
            public void onSaveAction() {
                initPenStyle();
            }
        });

        btnPen = getMenuButton(getResources().getDrawable(R.drawable.ic_setting), "设 置");
        btnPen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                penSettingFragment.showFragmentDlg(getSupportFragmentManager(), "penSettingFragment");
            }
        });
        //endregion

        //region 设置橡皮擦
        eraseSettingFragment = new EraseSettingFragment();
        eraseSettingFragment.setOnSaveListener(new EraseSettingFragment.OnSaveListener() {
            @Override
            public void onSaveAction() {
                initEraseWidth();
            }
        });
        btnEraseSetting = getMenuButton(getResources().getDrawable(R.drawable.ic_setting), "橡皮擦设置");
        btnEraseSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                eraseSettingFragment.showFragmentDlg(getSupportFragmentManager(), "eraseSettingFragment");
            }
        });
        //endregion

        //region 设置橡皮擦
        btnErase = getMenuButton(getResources().getDrawable(R.drawable.ic_erase), "橡皮檫");
        btnErase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                eraseFlag = !eraseFlag;
                if (eraseFlag) {
                    Toast.makeText(SignPdfView.this, "已进入橡皮擦模式", Toast.LENGTH_LONG).show();
                    btnErase.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.ic_close), null, null);
                    btnErase.setText("退出橡皮擦");
                    actionsMenu.removeAllViews();
                    actionsMenu.addView(btnErase);
                    actionsMenu.addView(btnEraseSetting);

                    arrowLeft.setVisibility(View.INVISIBLE);
                    arrowRight.setVisibility(View.INVISIBLE);
                    FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT);
                    layoutParams.gravity = Gravity.CENTER;
                    actionsMenu.setLayoutParams(layoutParams);

                } else {
                    Toast.makeText(SignPdfView.this, "已退出橡皮擦模式", Toast.LENGTH_LONG).show();
                    btnErase.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.ic_erase), null, null);
                    btnErase.setText("橡皮檫");
                    addActionBtns();
                }
                drawPenView.setEraseMode(eraseFlag);
            }
        });
        //endregion

        //region 文字批注
        btnTextInput = getMenuButton(getResources().getDrawable(R.drawable.ic_text), "文 字");
        btnTextInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(textInputFlag){
                    exitTextInputMode();
                }else{
                    intoTextInputMode();
                }
            }
        });

        btnEditText = getMenuButton(getResources().getDrawable(R.drawable.ic_text), "编 辑");
        btnEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(textView!=null) {
                    pdfView.removeView(textView);
                    textView.setEditMode(true);
                    textInputDlg.showFragmentDlg(textView.getCommentData().getTextContent(), userName, getSupportFragmentManager(), "textInputDlg",entryDataList);
                }else{
                    Toast.makeText(SignPdfView.this, "请先选择输入文字的位置", Toast.LENGTH_LONG).show();
                }
            }
        });
        //endregion

        //region 文字输入对话框
        textInputDlg = new TextInputFragment();
        textInputDlg.setOnClickItemListener(new OnClickItemListener() {

            @Override
            public void onOkAction(String txt) {
                hideMessageBar();
                if(textView ==null){

                    textView = new FontTextView(SignPdfView.this);
                    textView.setX(txtX-textView.getChopSize());
                    textView.setY(txtY-textView.getChopSize());
                    textView.setTypeface(simsunTypeface);
                    textView.setViewMaxWidth(screenWidth);
                    float _fsize = screenWidth*0.02F;
                    if(_fsize>45f){
                        _fsize = 45f;
                    }
                    textView.setTextSize(TypedValue.COMPLEX_UNIT_PX,_fsize);
                    textView.setViewMaxHeight(screenHeight-actionsMenu.getHeight());
                    textView.setOnTextClickListener(new FontTextView.OnTextClickListener() {
                        @Override
                        public void onTextEdit(CommentData commentData) {
                            pdfView.removeView(textView);
                            textView.setEditMode(true);
                            textInputDlg.showFragmentDlg(textView.getCommentData().getTextContent(),userName,getSupportFragmentManager(),"textInputDlg",entryDataList);
                        }

                        @Override
                        public void onTextDelete(CommentData commentData) {
                            pdfView.removeView(textView);
                            showMessageBar("请点击屏幕，选择输入文字的位置");
                            textView=null;
                        }
                    });
                }
                textView.setTextAndUserName(txt,userName);
                pdfView.addView(textView);
            }

            @Override
            public void onCancelAction(String txt) {
                if(textView!=null&&txt!=null&&txt.length()>0&&textView.isEditMode()) {
                    pdfView.addView(textView);
                }else{
                    showMessageBar("请点击屏幕，选择输入文字的位置");
                    textView = null;
                }
            }
        });
        //endregion
    }

    private void intoTextInputMode(){
        textInputFlag = true;
        showMessageBar("请点击屏幕，选择输入文字的位置");
        btnTextInput.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.ic_close), null, null);
        btnTextInput.setText("退出批注");
        actionsMenu.removeAllViews();
        actionsMenu.addView(btnTextInput);
        actionsMenu.addView(btnEditText);
        actionsMenu.addView(btnSave);

        arrowLeft.setVisibility(View.INVISIBLE);
        arrowRight.setVisibility(View.INVISIBLE);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT);
        layoutParams.gravity = Gravity.CENTER;
        actionsMenu.setLayoutParams(layoutParams);
    }

    /**
     * 退出文字批注
     */
    private void exitTextInputMode() {
        textInputFlag = false;
        btnTextInput.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.ic_text), null, null);
        btnTextInput.setText("文 字");
        if(textView!=null){
            pdfView.removeView(textView);
            textView=null;
        }
        hideMessageBar();
        addActionBtns();
    }

    /**
     * 初始化笔样式
     */
    private void initPenStyle(){
        float penWidth = penSettingData.getFloat(PenSettingFragment.PEN_WIDTH, PenSettingFragment.defaultWidth);
        int penColor = penSettingData.getInt(PenSettingFragment.PEN_COLOR, Color.BLACK);
        int penType = penSettingData.getInt(PenSettingFragment.PEN_TYPE, PenSettingFragment.STROKE_TYPE_PEN);

        drawPenView.setPenSetting(penWidth,penColor,true,penType);
    }

    private void initEraseWidth(){
        float eraseWidth = penSettingData.getFloat(EraseSettingFragment.ERASE_WIDTH, EraseSettingFragment.defaultWidth);
        drawPenView.setEraseWidth(eraseWidth);
    }

    private AppCompatButton getMenuButton(Drawable drawable, String btnName) {
        AppCompatButton menuButton = new AppCompatButton(this);
        menuButton.setBackgroundResource(R.drawable.round_buttom);
        menuButton.setText(btnName);
        menuButton.setTextSize(TypedValue.COMPLEX_UNIT_SP,16);
        menuButton.setCompoundDrawablesWithIntrinsicBounds(null,drawable,null,null);
        menuButton.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.MATCH_PARENT));
        return menuButton;
    }

    /**
     * 获取参数
     */
    private void initParams() {
        Intent intent = getIntent();
        //文件ID(*必须)
        if (intent.hasExtra(PARAM_FILE_ID)) {
            fileId = intent.getStringExtra(Constants.PARAM_FILE_ID);
        }
        //PDF后端服务URL(*必须)
        if (intent.hasExtra(Constants.KEY_SERVER_URL)) {
            serverUrl = intent.getStringExtra(Constants.KEY_SERVER_URL);
        }
        //PDF后端服务token(*必须)
        if (intent.hasExtra(Constants.KEY_SERVER_TOKEN)) {
            serverToken = intent.getStringExtra(Constants.KEY_SERVER_TOKEN);
        }
        //点击保存时是否在服务端生成签批后的PDF文件（默认false）
        if (intent.hasExtra(Constants.KEY_UPDATE_PDF)){
            updatePdfFlag = intent.getBooleanExtra(Constants.KEY_UPDATE_PDF,false);
        }
        //隐藏文字批注(默认false)
        if (intent.hasExtra(Constants.KEY_HIDE_TXT_BTN)){
            hideTextBtn = intent.getBooleanExtra(Constants.KEY_HIDE_TXT_BTN,false);
        }
        //用户ID(可选，保存批注信息时需要)
        if (intent.hasExtra(Constants.PARAM_USER_ID)) {
            userId = intent.getStringExtra(Constants.PARAM_USER_ID);
        }
        //用户姓名(可选，保存批注信息时需要)
        if (intent.hasExtra(PARAM_USER_NAME)) {
            userName = intent.getStringExtra(PARAM_USER_NAME);
        }
        //打开PDF时的默认页号，从0开始（默认0）
        if (intent.hasExtra(KEY_CURRENT_PAGE)) {
            currentPage = intent.getIntExtra(KEY_CURRENT_PAGE,0);
        }
        //是否公文模式，公文模式下显示提交按钮（默认false）
        if (intent.hasExtra(KEY_IS_DOC_MODE)) {
            isDocMode = intent.getBooleanExtra(KEY_IS_DOC_MODE,false);
        }
        //是否只读模式(默认 false)
        if (intent.hasExtra(KEY_IS_READ_MODE)) {
            readMode = intent.getBooleanExtra(KEY_IS_READ_MODE,false);
        }
        //是否隐藏批注(默认false)
        if (intent.hasExtra(KEY_IS_HIDE_ANNOT)) {
            hideAnnot = intent.getBooleanExtra(KEY_IS_HIDE_ANNOT,false);
        }
        //词条内容
        if (intent.hasExtra(KEY_ENTRY_LIST)) {
            String entryListJson = intent.getStringExtra(KEY_ENTRY_LIST);
            if(entryListJson!=null&&!"".equals(entryListJson.trim())){
                entryDataList = JSON.parseArray(entryListJson, EntryData.class);
            }
        }

        //--公文相关--
        if (intent.hasExtra(KEY_FLOW_INST_ID)) {
            flowInstId = intent.getStringExtra(KEY_FLOW_INST_ID);
        }
        if (intent.hasExtra(KEY_NODE_INST_ID)) {
            nodeInstId = intent.getStringExtra(KEY_NODE_INST_ID);
        }

        modifyFlag=false;
    }

    private void submitDocAndBack(){
        Intent intent = new Intent();
        intent.putExtra("docSubmitFlag", true);
        intent.putExtra("modifyFlag", modifyFlag);
        setResult(100, intent);
        finish();
    }

    /**
     * 打开pdf
     */
    public void openPdfFile(){
        pdfView.fromFile(new File(filePath))
                .defaultPage(currentPage)
                .swipeHorizontal(true)
                .pageSnap(true)
                .pageFling(true)
                .enableAnnotationRendering(false)
                .spacing(0)
                .autoSpacing(true)
                .onTap(this)
                .onLongPress(this)
                .onPageChange(new OnPageChangeListener() {
                    @Override
                    public void onPageChanged(int page, int pageCount) {
                        currentPage = page;
                    }
                })
                .onDraw(this)
                .onLoad(this)
                //宽度自适应（不可修改，修改后插入手写坐标会发生变化）
                .pageFitPolicy(FitPolicy.WIDTH).load();
    }

    /**
     * 页面点击事件
     * @return
     */
    @Override
    public boolean onTap(MotionEvent e) {
        Log.d("pdfview","x : "+e.getX()+ " y : "+e.getY() + ",screenWidth "+screenWidth+",screenHeight "+screenHeight);

        //文字批注模式
        if(textInputFlag) {

            if(textView!=null){
                return false;
            }

            txtX = e.getX();
            txtY = e.getY();
            textInputDlg.showFragmentDlg("",userName,getSupportFragmentManager(),"textInputDlg",entryDataList);

            return true;
        }
        return false;
    }

    /**
     * 长按事件
     * @param e MotionEvent that registered as a confirmed long press
     */
    @Override
    public void onLongPress(MotionEvent e){
        Log.d("pdfview","long press action : "+e.getAction());
        if(!hideAnnot) {
            checkCanEdit(e.getX(), e.getY());
        }
    }

    /**
     * 查看坐标是否有手写批注图片
     * @param x
     * @param y
     * @return
     */
    public boolean checkCanEdit(float x, float y){
        editComment = null;
        List<CommentData> handwritingList = httpUtil.getHandwritingList();
        for (CommentData data:handwritingList){
            if(data.getPageNo() == pdfView.getCurrentPage()){
                //只考虑水平滑动的情况
                float localTranslationX = pdfView.getPageOffset(data.getPageNo());
                float pointX;
                //相对屏幕pdf左上角的坐标
                float currentXOffset = pdfView.getCurrentXOffset();
                if(currentXOffset<0){
                    //考虑超出屏幕的偏移量
                    pointX = - currentXOffset + x;
                }else{
                    pointX =  x - currentXOffset;
                }
                pointX = pointX - localTranslationX;
                float pointY;
                float currentYOffset = pdfView.getCurrentYOffset();
                if(currentYOffset<0){
                    //考虑超出屏幕的偏移量
                    pointY = -currentYOffset + y;
                }else{
                    pointY = y - currentYOffset;
                }

                Log.d("touch",pointX + ":" + pointY);
                float zoom = pdfView.getZoom();
                boolean minFlag = pointX>data.getPx()*pdfView.getDisplayWRadio()*zoom&&pointY>data.getPy()*pdfView.getDisplayHRadio()*zoom;
                boolean maxFlag = pointX<(data.getPx()+data.getImageWidth())*pdfView.getDisplayWRadio()*zoom&&pointY<(data.getPy()+data.getImageHeight())*pdfView.getDisplayHRadio()*zoom;
                if(minFlag&&maxFlag) {
                    editComment = data;
                    pdfView.invalidate();
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * pdf文件加载完成
     * @param nbPages the number of pages in this PDF file
     */
    @Override
    public void loadComplete(int nbPages) {
        drawPenView.setLayoutParams(pdfView.getPdfLayoutParams());
        //显示按钮
        addActionBtns();
    }

    /**
     * 显示手写签批
     * @param canvas        The canvas on which to draw things.
     * @param pageWidth     The width of the current page.
     * @param pageHeight    The height of the current page.
     * @param displayedPage The current page index
     */
    @Override
    public void onLayerDrawn(Canvas canvas, float pageWidth, float pageHeight, int displayedPage) {
        if(httpUtil.getHandwritingList()!=null) {
            for (CommentData commentData : httpUtil.getHandwritingList()) {
                drawHandwritingPart(canvas, commentData);
            }
        }
        drawPenView.setLayoutParams(pdfView.getPdfLayoutParams());
    }

    /**
     * 手写批注显示
     * @param canvas
     * @param data
     */
    private void drawHandwritingPart(Canvas canvas, CommentData data){

        Bitmap renderedBitmap = data.getImageBitmap();

        if (renderedBitmap==null||renderedBitmap.isRecycled()) {
            return;
        }

        float localTranslationX;
        float localTranslationY;
        SizeF size = pdfView.getPageSize(data.getPageNo());

        if (pdfView.isSwipeVertical()) {
            localTranslationY = pdfView.getPageOffset(data.getPageNo());
            float maxWidth = pdfView.getMaxPageWidth();
            localTranslationX = pdfView.toCurrentScale(maxWidth - size.getWidth()) / 2;
        } else {
            localTranslationX = pdfView.getPageOffset(data.getPageNo());
            float maxHeight = pdfView.getMaxPageHeight();
            localTranslationY = pdfView.toCurrentScale(maxHeight - size.getHeight()) / 2;
        }
        canvas.translate(localTranslationX, localTranslationY);
        //原图片大小
        Rect srcRect = new Rect(0, 0, renderedBitmap.getWidth(), renderedBitmap.getHeight());
        //变换后的图片大小
        float x = data.getPx()*pdfView.getDisplayWRadio();
        float y = data.getPy()*pdfView.getDisplayHRadio();
        float zoom = pdfView.getZoom();
        RectF dstRect = new RectF(x*zoom,y*zoom,(x+data.getImageWidth()*pdfView.getDisplayWRadio())*zoom,(y+data.getImageHeight()*pdfView.getDisplayHRadio())*zoom);

        float translationX = pdfView.getCurrentXOffset() + localTranslationX;
        float translationY = pdfView.getCurrentYOffset() + localTranslationY;
        if (translationX + dstRect.left >= pdfView.getWidth() || translationX + dstRect.right <= 0 || translationY + dstRect.top >= pdfView.getHeight() || translationY + dstRect.bottom <= 0) {
            canvas.translate(-localTranslationX, -localTranslationY);
        }else{
            canvas.drawBitmap(renderedBitmap, srcRect, dstRect, paint);

            if (editComment != null && editComment.equals(data)) {
                String dateStr = data.getSignTime()!=null? DATA_FORMAT1.format(data.getSignTime()):"";
                String info = "签批人员：" + data.getSignerName() + "\n签批时间：" + dateStr;

                //绘制边界
                canvas.drawRect(dstRect.left - editLinePadding, dstRect.top - editLinePadding, dstRect.right + editLinePadding, dstRect.bottom + editLinePadding, commentPaint);

                RectF infoRecfF = new RectF(translationX + dstRect.left, translationY + dstRect.top, translationX + dstRect.right, translationY + dstRect.bottom);
                boolean curUserFlag = data.getSignerId() != null && data.getSignerId().equals(userId);
                if(curUserFlag&&editActionSheet!=null) {
                    editActionSheet.setTitle(info);
                    //删除按钮
                    editActionSheet.show(infoRecfF);
                }else if(infoActionSheet!=null){
                    infoActionSheet.setTitle(info);
                    infoActionSheet.show(infoRecfF);
                }
            }

            canvas.translate(-localTranslationX, -localTranslationY);
        }
    }

    /**
     * 保存手写签批
     * @param handwritingData
     */
    private void saveHandwritingData(final CommentData handwritingData,final int successEvent) {
        float wr = pdfView.getDisplayWRadio();
        float hr = pdfView.getDisplayHRadio();
        float x = handwritingData.getSignX();
        float y = handwritingData.getSignY();
        //获取没有放大或缩小的图片数据
        Bitmap originBitmap = ImageTools.scaleBitmap(handwritingData.getImageBitmap(), 1 / pdfView.getZoom());
        float width = originBitmap.getWidth() / wr;
        float height = originBitmap.getHeight() / hr;
        handwritingData.setZoom(pdfView.getZoom());
        handwritingData.setPx(x / wr);
        handwritingData.setPy(y / hr);
        handwritingData.setImageWidth(width);
        handwritingData.setImageHeight(height);
        handwritingData.setImageBitmap(originBitmap);
        handwritingData.setPdfFileHeight(pdfView.getPhysicalPdfHeight());
        handwritingData.setPdfFileWidth(pdfView.getPhysicalPdfWidth());
        handwritingData.setPageNo(pdfView.getCurrentPage());
        handwritingData.setSignerId(userId);
        handwritingData.setSignerName(userName);

        if(handwritingData.getSignTime()==null) {
            handwritingData.setSignTime(new Date());
        }

        CommentData saveBean = new CommentData();
        saveBean.setFileId(fileId);
        saveBean.setFlowInstId(flowInstId);
        saveBean.setNodeInstId(nodeInstId);
        saveBean.setUpdatePdfFlag(updatePdfFlag);
        saveBean.setPageNo(handwritingData.getPageNo());
        saveBean.setPx(handwritingData.getPx());
        saveBean.setPy(handwritingData.getPy());
        saveBean.setImageWidth(handwritingData.getImageWidth());
        saveBean.setImageHeight(handwritingData.getImageHeight());
        saveBean.setZoom(handwritingData.getZoom());
        saveBean.setPdfFileWidth(handwritingData.getPdfFileWidth());
        saveBean.setPdfFileHeight(handwritingData.getPdfFileHeight());
        saveBean.setSignerId(handwritingData.getSignerId());
        saveBean.setSignerName(handwritingData.getSignerName());
        saveBean.setTextContent(handwritingData.getTextContent());
        saveBean.setSignType(handwritingData.getSignType());
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
                        modifyFlag=true;
                        //添加到图层
                        handwritingData.setId(resData.getData().toString());
                        httpUtil.addHandwriting(handwritingData);
                        sendMessage(successEvent,handwritingData.getId());
                    }else{
                        sendMessage(MSG_RESPONSE_MSG,"操作失败");
                    }

                }else {
                    sendMessage(MSG_RESPONSE_MSG,"操作失败");
                }
            }
        });
    }

    /**
     * 获取签批bitmap
     * @return
     */
    public CommentData getSignBitmapFromSignView(){
        if(drawPenView!=null){
            CommentData data = new CommentData();
            SignBitmapData bitmapData = drawPenView.getBitmapWithBlank(10);
            if(bitmapData!=null) {
                data.setImageBitmap(bitmapData.getSignBitmap());
                data.setSignX(pdfView.getSignX(bitmapData.getMinX()));
                data.setSignY(pdfView.getSignY(bitmapData.getMinY()));
                return data;
            }
        }
        return null;
    }

    /**
     * 添加一个手写签批到屏幕上显示
     * @param commentData
     */
    public void addHandwritingDataAndRefresh(CommentData commentData) {
        if(commentData.getSignContent()!=null&&!"".equals(commentData.getSignContent().trim())){
            Bitmap bitmap = ImageTools.base64ToBitmap(commentData.getSignContent());
            commentData.setImageBitmap(bitmap);
        }
        httpUtil.addHandwriting(commentData);
        sendMessage(MSG_REFRESH_PDF_VIEW,null);
    }

    public void removeHandwritingData(String id){
        httpUtil.removeHandwriting(id);
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
                            modifyFlag=true;
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
                sendMessage(MSG_DOWNLOAD_ERROR,"文件下载失败，请尝试重新打开");
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
                            String fileMd5 = OkHttpUtil.getFileMd5Code(pdfFile);
                            boolean isSame = fileMd5!=null&&fileMd5.equals(dataMap.get("mdfCode"));
                            if(pdfFile.exists()&&isSame){
                                filePath = pdfFile.getAbsolutePath();
                                findHandwritingData();
                            }else{
                                //下载文件
                                downloadFile(pdfFile);
                            }
                        }else{
                            sendMessage(MSG_DOWNLOAD_ERROR,"文件打开失败");
                        }

                    }
                }else{
                    sendMessage(MSG_DOWNLOAD_ERROR,"文件下载失败，请尝试重新打开");
                }
            }
        });
    }

    private void downloadFile(final File file){
        if(!file.getParentFile().exists()){
            file.getParentFile().mkdirs();
        }
        OkHttpClient httpClient = new OkHttpClient.Builder().connectTimeout(20, TimeUnit.SECONDS).build();
        Request request = new Request.Builder().url(serverUrl +URL_DOWNLOAD_FILE+"?fileId=" + fileId).header(Constants.HEADER_TOKEN_NAME,serverToken).build();
        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                sendMessage(MSG_DOWNLOAD_ERROR,"文件下载失败，请尝试重新打开");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                ResponseBody responseBody = null;
                BufferedInputStream bis = null;
                FileOutputStream fos = null;
                try {
                    if (call.isCanceled()) {
                        sendMessage(MSG_DOWNLOAD_ERROR,"已取消文件下载");
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
                        sendMessage(MSG_DOWNLOAD_ERROR,"文件下载失败");
                    }
                } catch (Exception e) {
                    sendMessage(MSG_HIDE_LOADING,null);
                    sendMessage(MSG_DOWNLOAD_ERROR,"文件下载异常");
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
        if(hideAnnot) {
            sendMessage(MSG_FIND_COMMENT_LIST,null);
        }else{
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
        int menuCount = 1;
        actionsMenu.addView(closeViewItem);
        if(btnSubmit!=null) {
            actionsMenu.addView(btnSubmit);
            menuCount++;
        }

        if(!readMode) {

            if(!hideTextBtn) {
                actionsMenu.addView(btnTextInput);
            }

            actionsMenu.addView(btnSave);
            actionsMenu.addView(btnErase);
            actionsMenu.addView(btnPen);
            menuCount+=3;
        }

        if(pdfView!=null&&pdfView.getPageCount()>5) {
            actionsMenu.addView(jumpToItem);
            menuCount++;
        }

        int width = outMetrics.widthPixels;

        if(300*menuCount>width){
            arrowLeft.setVisibility(View.VISIBLE);
            arrowRight.setVisibility(View.VISIBLE);
            layoutParams.gravity = Gravity.START;
        }else{
            arrowLeft.setVisibility(View.INVISIBLE);
            arrowRight.setVisibility(View.INVISIBLE);
            layoutParams.gravity = Gravity.CENTER;
        }

        actionsMenu.setLayoutParams(layoutParams);
    }

    private void addCommonBtns(){
        actionsMenu.addView(closeViewItem);
    }

    private void showProgressDialog(String msg){
        if(progressDialog!=null){
            progressDialog.setMessage(msg);
            progressDialog.show();
        }
    }
    private void changeProgressMsgDialog(String msg){
        if(progressDialog!=null){
            progressDialog.setMessage(msg);
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
        new AlertView("提示", msg, null, new String[]{"确定"}, null, SignPdfView.this, AlertView.Style.Alert, null).setCancelable(true).show();
    }

    /**
     * 查询批注成功
     */
    private void findCommentSuccess(){
        hideProgressDialog();
        //打开PDF文件
        openPdfFile();
    }

    /**
     * 批注保存成功
     */
    private void saveCommentSuccess(String id){
        hideProgressDialog();
        //刷新页面
        pdfView.invalidate();
        drawPenView.clearView();
        Toast.makeText(SignPdfView.this,"保存成功",Toast.LENGTH_LONG).show();

        if(textInputFlag){
            exitTextInputMode();
        }
    }

    /**
     * 删除批注成功
     * @param id
     */
    private void deleteCommentSuccess(String id){
        hideProgressDialog();
        httpUtil.removeHandwriting(id);
        if(pdfView!=null){
            pdfView.invalidate();
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
                case MSG_TOAST:
                    signPdfView.showMessageBar(dataObj.toString());
                    break;
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
                case MSG_CHANGE_LOADING:
                    if(dataObj!=null) {
                        signPdfView.changeProgressMsgDialog(dataObj.toString());
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
                case MSG_DOWNLOAD_ERROR:
                    if(dataObj!=null) {
                        signPdfView.httpResponseMsg(dataObj.toString());
                    }
                    signPdfView.addCommonBtns();
                    break;
                case MSG_SAVE_COMMENT_AND_SUBMIT:
                    signPdfView.submitDocAndBack();
                    break;
                case MSG_FONT_DOWNLOAD_SUCCESS:
                    signPdfView.hideProgressDialog();
                    signPdfView.intoTextInputMode();
                    break;
                default:
            }
        }
    }

    /**
     * 在顶部栏显示文本信息
     * @param txt
     */
    private void showMessageBar(String txt){
        if(txt!=null&&txt.trim()!="") {
            barTextView.setText(txt);
            barTextView.setVisibility(View.VISIBLE);
        }
    }
    private void hideMessageBar(){
        barTextView.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isDestroy = true;
        if(progressDialog!=null&&progressDialog.isShowing()){
            progressDialog.dismiss();
        }

        setResult(1001);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (KeyEvent.KEYCODE_BACK == keyCode) {
            if ((System.currentTimeMillis() - exitTime) > 2000) {
                Toast.makeText(getApplicationContext(), "再按一次退出程序", Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            } else {
                closePdfView();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void closePdfView(){
        Intent intent = new Intent();
        intent.putExtra("modifyFlag", modifyFlag);
        setResult(100, intent);
        finish();
    }
}
