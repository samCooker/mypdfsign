package cn.com.chaochuang.writingpen.ui;

import android.app.Activity;
import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import cn.com.chaochuang.writingpen.model.SignBitmapData;
import cn.com.chaochuang.writingpen.utils.BasePenExtend;
import cn.com.chaochuang.writingpen.utils.BrushPen;
import cn.com.chaochuang.writingpen.utils.IPenConfig;
import cn.com.chaochuang.writingpen.utils.SteelPen;


/**
 * @author shiming
 * @version v1.0 create at 2017/8/24
 */
public class DrawPenView extends View {
    private static final String TAG = "DrawPenView";
    private Paint mPaint;//画笔
    private Canvas mCanvas;//画布
    private Bitmap mBitmap;
    private Context mContext;
    public static int mCanvasCode = IPenConfig.STROKE_TYPE_PEN;
    private BasePenExtend mStokeBrushPen;
    private boolean mIsCanvasDraw;

    private float penWidth;
    private int penColor;
    private int penType;
    private boolean penOnly=true;

    public DrawPenView(Context context) {
        super(context);
        initParameter(context);
    }

    public DrawPenView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initParameter(context);
    }

    public DrawPenView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initParameter(context);
    }

    private void initParameter(Context context) {
        mContext = context;
        DisplayMetrics dm = new DisplayMetrics();
        ((Activity) mContext).getWindowManager().getDefaultDisplay().getMetrics(dm);
        mBitmap = Bitmap.createBitmap(dm.widthPixels, dm.heightPixels, Bitmap.Config.ARGB_8888);
        mStokeBrushPen = new SteelPen(context);
        mStokeBrushPen.clearSaveCor();
        initPaint();
        initCanvas();
    }


    private void initPaint() {

        if(mPaint==null) {
            mPaint = new Paint();
        }

        mPaint.setColor(this.penColor);
        mPaint.setStrokeWidth(this.penWidth);

        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeCap(Paint.Cap.ROUND);//结束的笔画为圆心
        mPaint.setStrokeJoin(Paint.Join.ROUND);//连接处元
        mPaint.setAlpha(0xFF);
        mPaint.setAntiAlias(true);
        mPaint.setStrokeMiter(1.0f);
        mStokeBrushPen.setPaint(mPaint);

        setCanvasCode(penType);
    }


    public void setPenSetting(float penWidth, int penColor,boolean penOnly,int penType){
        this.penWidth = penWidth;
        this.penColor = penColor;
        this.penType = penType;
        this.penOnly = penOnly;

        initPaint();
    }

    public void setEraseWidth(float width){
        mStokeBrushPen.setEraseWidth(width);
    }

    public boolean isPenOnly() {
        return penOnly;
    }

    private void initCanvas() {
        mCanvas = new Canvas(mBitmap);
        //设置画布的颜色的问题
        mCanvas.drawColor(Color.TRANSPARENT);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(mBitmap, 0, 0, mPaint);
        switch (mCanvasCode) {
            case IPenConfig.STROKE_TYPE_PEN:
            case IPenConfig.STROKE_TYPE_BRUSH:
                mStokeBrushPen.draw(canvas);
                break;
            case IPenConfig.STROKE_TYPE_ERASER:
                clearView();
                break;
            default:
                Log.e(TAG, "onDraw" + Integer.toString(mCanvasCode));
                break;
        }
        super.onDraw(canvas);
    }


    public void setCanvasCode(int canvasCode) {
        mCanvasCode = canvasCode;
        switch (mCanvasCode) {
            case IPenConfig.STROKE_TYPE_PEN:
                mStokeBrushPen = new SteelPen(mContext);
                break;
            case IPenConfig.STROKE_TYPE_BRUSH:
                mStokeBrushPen = new BrushPen(mContext);
                break;

        }
        //设置
        if (mStokeBrushPen.isNull()){
            mStokeBrushPen.setPaint(mPaint);
        }
        invalidate();
    }

    /**
     * event.getAction() //获取触控动作比如ACTION_DOWN
     * event.getPointerCount(); //获取触控点的数量，比如2则可能是两个手指同时按压屏幕
     * event.getPointerId(nID); //对于每个触控的点的细节，我们可以通过一个循环执行getPointerId方法获取索引
     * event.getX(nID); //获取第nID个触控点的x位置,记录的第一个点为getX，getY
     * event.getY(nID); //获取第nID个点触控的y位置
     * event.getPressure(nID); //LCD可以感应出用户的手指压力，当然具体的级别由驱动和物理硬件决定的
     * event.getDownTime() //按下开始时间
     * event.getEventTime() // 事件结束时间
     * event.getEventTime()-event.getDownTime()); //总共按下时花费时间
     *
     * @param event
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if(mStokeBrushPen.isEraser()){
            mStokeBrushPen.onTouchEvent(event, mCanvas);
            invalidate();
            return true;
        }

        Log.d(TAG,"touch event");


        //新增 判断是否为手写笔
        int type = event.getToolType(event.getActionIndex());
        if(penOnly&&type != MotionEvent.TOOL_TYPE_STYLUS){
            return false;
        }

        mIsCanvasDraw = true;
        MotionEvent event2 = MotionEvent.obtain(event);
        mStokeBrushPen.onTouchEvent(event2, mCanvas);
        //event会被下一次事件重用，这里必须生成新的，否则会有问题
        //getActionMask:触摸的动作,按下，抬起，滑动，多点按下，多点抬起
        switch (event2.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                if (mGetTimeListner!=null)
                mGetTimeListner.stopTime();
                break;
            case MotionEvent.ACTION_MOVE:
                if (mGetTimeListner!=null)
                mGetTimeListner.stopTime();
                break;
            case MotionEvent.ACTION_UP:
                long time = System.currentTimeMillis();
                if (mGetTimeListner!=null)
                mGetTimeListner.getTime(time);
                break;
            default:
                break;
        }
        invalidate();
        return true;
    }
    /**
     *
     * @return 判断是否有绘制内容在画布上
     */
    public boolean getHasDraw(){
        return mIsCanvasDraw;
    }
    /**
     * 清除画布，记得清除点的集合
     */
    public void clearView() {
        mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        mCanvas.drawPaint(mPaint);
        mPaint.setXfermode(null);
        mIsCanvasDraw = false;
        mStokeBrushPen.clear();
        mStokeBrushPen.clearSaveCor();
        initPaint();
        //initCanvas();
        invalidate();
    }

    public void setEraseMode(boolean isErase){
        mStokeBrushPen.setEraser(isErase);
    }

    public boolean isEraseMode(){
        return mStokeBrushPen.isEraser();
    }

    public TimeListener mGetTimeListner;

    public void setGetTimeListener(TimeListener l) {
        mGetTimeListner = l;
    }

    public Bitmap getBitmap() {
        return mBitmap;
    }

    public interface TimeListener {
        void getTime(long l);

        void stopTime();
    }

    private int mBackColor = Color.TRANSPARENT;

    /**
     * 逐行扫描 清楚边界空白。功能是生成一张bitmap位于正中间，不是位于顶部，此关键的是我们画布需要
     * 成透明色才能生效
     * @param blank 边距留多少个像素
     * @return tks github E-signature
     */
    public SignBitmapData getBitmapWithBlank(int blank) {
        if (mBitmap != null) {

            SignBitmapData bitmapData = new SignBitmapData();
            int bitmapH = mBitmap.getHeight();
            int bitmapW = mBitmap.getWidth();

            int left = mStokeBrushPen.minX - blank > 0 ? (int) mStokeBrushPen.minX - blank : 0;
            int top = mStokeBrushPen.minY - blank > 0 ? (int)mStokeBrushPen.minY - blank : 0;
            int right = mStokeBrushPen.maxX + blank > bitmapW - 1 ? bitmapW - 1 : (int)mStokeBrushPen.maxX + blank;
            int bottom = mStokeBrushPen.maxY + blank > bitmapH - 1 ? bitmapH - 1 : (int)mStokeBrushPen.maxY + blank;

            Bitmap signBitmap = Bitmap.createBitmap(mBitmap, left,top,right-left,bottom-top);

            //判断bitmap是否为空白
            int[] pixs = new int[signBitmap.getWidth()];
            boolean isStop=false;
            for (int y = 0; y <signBitmap.getHeight(); y++) {
                signBitmap.getPixels(pixs, 0, signBitmap.getWidth(), 0, y, signBitmap.getWidth(), 1);
                isStop = false;
                for (int pix : pixs) {
                    if (pix != mBackColor) {
                        isStop = true;
                        break;
                    }
                }
                if (isStop) {
                    break;
                }
            }
            //说明有笔迹内容
            if(isStop){
                bitmapData.setSignBitmap(signBitmap);
                bitmapData.setMinX(left);
                bitmapData.setMaxX(right);
                bitmapData.setMinY(top);
                bitmapData.setMaxY(bottom);

                mStokeBrushPen.clearSaveCor();
                return bitmapData;
            }
            return null;
        } else {
            return null;
        }
    }

}
