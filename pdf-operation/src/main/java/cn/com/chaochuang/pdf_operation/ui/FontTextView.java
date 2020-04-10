package cn.com.chaochuang.pdf_operation.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

import java.io.File;

import cn.com.chaochuang.pdf_operation.R;
import cn.com.chaochuang.pdf_operation.model.TouchPos;
import cn.com.chaochuang.writingpen.model.CommentData;


/**
 * Created by Shicx on 2020-04-07.
 */
public class FontTextView extends AppCompatTextView {

    /**
     * 最大高度和宽度
     */
    public static int viewMaxWidth=800,viewMaxHeight=500;
    /**
     * 边框留白大小
     */
    public static int borderPadding = 80;

    private CommentData commentData;
    private Context context;
    private float downX, downY, originLeft, originTop, originRight, originBottom;
    /**
     * 边框画笔
     */
    private Paint borderPaint;
    private Paint drawPaint;
    private Bitmap moveBitmap,expendBitmap,deleteBitmap;
    /**
     * 第一个接触点位置标示
     */
    private int touchPos;
    /**
     *
     */
    private int viewTouchPadding = borderPadding;

    private int viewMinWidth = borderPadding*2, viewMinHeight = borderPadding*2;
    /**
     * 圆形边框宽度
     */
    private int circleBorderWidth=2;

    private OnTextClickListener onTextClickListener;

    public FontTextView(Context context) {
        super(context);
        initTextView(context);
    }

    public FontTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initTextView(context);
    }

    public FontTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initTextView(context);
    }

    private void initTextView(Context context) {
        this.context = context;

        setMaxWidth(viewMaxWidth);
        setMaxHeight(viewMaxHeight);

        //边框画笔
        this.borderPaint = new Paint();
        this.borderPaint.setStyle(Paint.Style.STROKE);
        this.borderPaint.setStrokeWidth(4f);
        this.borderPaint.setColor(getResources().getColor(R.color.pdf_comment_border));
        this.borderPaint.setPathEffect(new DashPathEffect(new float[]{4, 4}, 0));

        //图形
        moveBitmap = getBitmapFromVectorDrawable(R.drawable.edit_move);
        expendBitmap = getBitmapFromVectorDrawable(R.drawable.edit_expend);
        deleteBitmap = getBitmapFromVectorDrawable(R.drawable.edit_delete);
        drawPaint = new Paint();

        setPadding(borderPadding,borderPadding,borderPadding,borderPadding);

        //数据实体
        commentData = new CommentData();
    }

    public Bitmap getBitmapFromVectorDrawable(int drawableId) {
        Drawable drawable = ContextCompat.getDrawable(this.context, drawableId);
        if(drawable!=null) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                drawable = (DrawableCompat.wrap(drawable)).mutate();
            }

            Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                    drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);

            return bitmap;
        }
        return null;
    }

    /**
     * 设置字体文件
     *
     * @param fontFile
     */
    public void setTypeface(File fontFile) {
        if (fontFile != null && fontFile.exists()) {
            Typeface typeface = Typeface.createFromFile(fontFile);
            setTypeface(typeface);
        }
    }

    /**
     * 拉伸
     * @param ex
     * @param ey
     */
    public void expendLayout(float ex, float ey) {

        int expendX = (int) (originRight + ex), expendY = (int) (originBottom + ey);
        if (expendX < viewMinWidth) {
            expendX = viewMinWidth;
        }
        if (expendY < viewMinHeight) {
            expendY = viewMinHeight;
        }
        setLayoutParams(new RelativeLayout.LayoutParams(expendX, expendY));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downX = event.getX();
                downY = event.getY();

                originTop = getTop();
                originLeft = getLeft();
                originRight = getRight();
                originBottom = getBottom();

                touchPos = checkTouchPosition();
                Log.d("touchPos", touchPos + "");
                break;
            case MotionEvent.ACTION_UP:
                if (touchPos == TouchPos.POS_CENTER){
                    //编辑
                    if(onTextClickListener !=null){
                        onTextClickListener.onTextEdit();
                    }
                }else if(touchPos == TouchPos.POS_TOP_LEFT){
                    //删除
                    if(onTextClickListener!=null){
                        onTextClickListener.onTextDelete();
                    }
                }
                break;
            case MotionEvent.ACTION_MOVE:
                //
                float moveX = event.getX() - downX;
                float moveY = event.getY() - downY;
                if (Math.abs(moveX) > 3 || Math.abs(moveY) > 3) {
                    float l = (getX() + moveX);
                    float t = (getY() + moveY);

                    switch (touchPos) {
                        //点击中心移动
                        case TouchPos.POS_TOP_RIGHT:
                            //判断是否超出屏幕
                            setX(l);
                            setY(t);
                            break;
                        //点击右下角放大缩小
                        case TouchPos.POS_BOTTOM_RIGHT:
                            expendLayout(moveX, moveY);
                            break;
                    }
                }
                break;
        }
        return true;
    }

    /**
     * 判断第一个接触点的位置
     *
     * @return
     */
    private int checkTouchPosition() {
        if (downX <= viewTouchPadding && downY <= viewTouchPadding) {
            //左上角
            return TouchPos.POS_TOP_LEFT;
        }
        if (downX >= viewTouchPadding && downX <= originRight - originLeft - viewTouchPadding && downY <= viewTouchPadding) {
            //顶部
            return TouchPos.POS_TOP;
        }
        if (downX >= originRight - originLeft - viewTouchPadding && downY <= viewTouchPadding) {
            //右上角
            return TouchPos.POS_TOP_RIGHT;
        }
        if (downX <= viewTouchPadding && downY >= viewTouchPadding && downY <= originBottom - originTop - viewTouchPadding) {
            //左边
            return TouchPos.POS_LEFT;
        }
        if (downX <= viewTouchPadding && downY >= originBottom - originTop - viewTouchPadding) {
            //左下角
            return TouchPos.POS_BOTTOM_LEFT;
        }
        if (downX >= viewTouchPadding && downX <= originRight - originLeft - viewTouchPadding && downY >= originBottom - originTop - viewTouchPadding) {
            //底部
            return TouchPos.POS_BOTTOM;
        }
        if (downX >= originRight - originLeft - viewTouchPadding && downY >= originBottom - originTop - viewTouchPadding) {
            //右下角
            return TouchPos.POS_BOTTOM_RIGHT;
        }
        if (downX >= originRight - originLeft - viewTouchPadding && downY >= viewTouchPadding && downY <= originBottom - originTop - viewTouchPadding) {
            //右边
            return TouchPos.POS_RIGHT;
        }
        return 0;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float left = getLeft()+borderPadding/2f;
        float top = getTop()+borderPadding/2f;
        float right = getRight()-borderPadding/2f;
        float bottom = getBottom()-borderPadding/2f;
        //边框
        canvas.drawRect(left, top, right, bottom, borderPaint);

        //删除图标
        drawBorderCorner(left,top,canvas,deleteBitmap);
        //移动图标
        drawBorderCorner(right,top,canvas,moveBitmap);
        //拉伸图标
        drawBorderCorner(right,bottom,canvas,expendBitmap);

    }

    /**
     * 画上边角的圆圈及图像
     * @param x 原点x坐标
     * @param y 原点y坐标
     * @param canvas
     * @param bitmap
     */
    private void drawBorderCorner(float x,float y,Canvas canvas, Bitmap bitmap) {

        canvas.save();
        //右上角实心圆
        canvas.translate(x,y);
        drawPaint.setColor(getResources().getColor(R.color.pdf_comment_circle));
        drawPaint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(0,0,borderPadding/2f,drawPaint);
        //右上角圆边框
        drawPaint.setColor(getResources().getColor(R.color.pdf_comment_border));
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeWidth(circleBorderWidth);
        canvas.drawCircle(0,0,borderPadding/2f-circleBorderWidth,drawPaint);
        //移动图标
        canvas.drawBitmap(bitmap,-bitmap.getWidth()/2f,-bitmap.getHeight()/2f,drawPaint);
        canvas.restore();
    }

    /**
     * 点击事件
     * @param onTextClickListener
     */
    public void setOnTextClickListener(OnTextClickListener onTextClickListener){
        this.onTextClickListener = onTextClickListener;
    }
    public interface OnTextClickListener {

        void onTextEdit();

        void onTextDelete();
    }

    /**
     *
     * @return
     */
    public int getViewTouchPadding() {
        return viewTouchPadding;
    }

    public CommentData getCommentData() {
        return commentData;
    }

    public void setCommentData(CommentData commentData) {
        this.commentData = commentData;
    }


}