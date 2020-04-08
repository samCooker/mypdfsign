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
import cn.com.chaochuang.pdf_operation.utils.DensityUtil;


/**
 * Created by Shicx on 2020-04-07.
 */
public class FontTextView extends AppCompatTextView {

    private Context context;
    private float downX, downY, originLeft, originTop, originRight, originBottom;
    private int viewMaxWidth,viewMaxHeight;
    /**
     * 边框画笔
     */
    private Paint borderPaint;
    private Paint drawPaint;
    private Bitmap moveBitmap,expendBitmap;
    /**
     * 第一个接触点位置标示
     */
    private int touchPos;
    /**
     *
     */
    private int borderPadding = 80;
    private int viewTouchPadding = borderPadding;

    private int viewMinWidth = borderPadding*2, viewMinHeight = borderPadding*2;

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

        //边框画笔
        this.borderPaint = new Paint();
        this.borderPaint.setStyle(Paint.Style.STROKE);
        this.borderPaint.setStrokeWidth(4f);
        this.borderPaint.setColor(getResources().getColor(R.color.pdf_comment_border));
        this.borderPaint.setPathEffect(new DashPathEffect(new float[]{4, 4}, 0));

        //图形
        moveBitmap = getBitmapFromVectorDrawable(R.drawable.ic_move);
        expendBitmap = getBitmapFromVectorDrawable(R.drawable.ic_expend);
        drawPaint = new Paint();

        setPadding(borderPadding,borderPadding,borderPadding,borderPadding);
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

    public void setViewMaxSize(){

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
                        case TouchPos.POS_TOP_LEFT:
                            //判断是否超出屏幕

                            setX(l);
                            setY(t);
                            break;
                        //点击右下角放大缩小
                        case TouchPos.POS_BOTTOM_RIGHT:
                            expendLayout(moveX, moveY);
                            break;
                        //编辑
                        case TouchPos.POS_CENTER:
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

        canvas.save();
        float left = getLeft()+borderPadding/2f;
        float top = getTop()+borderPadding/2f;
        float right = getRight()-borderPadding/2f;
        float bottom = getBottom()-borderPadding/2f;
        //边框
        canvas.drawRect(left, top, right, bottom, borderPaint);
        //左上角实心圆
        canvas.translate(left,top);
        drawPaint.setColor(getResources().getColor(R.color.pdf_comment_circle));
        drawPaint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(0,0,borderPadding/2f,drawPaint);
        //左上角圆边框
        drawPaint.setColor(getResources().getColor(R.color.pdf_comment_border));
        drawPaint.setStyle(Paint.Style.STROKE);
        canvas.drawCircle(0,0,borderPadding/2f,drawPaint);
        //移动图标
        canvas.drawBitmap(moveBitmap,-moveBitmap.getWidth()/2f,-moveBitmap.getHeight()/2f,drawPaint);
        canvas.restore();

        canvas.save();
        canvas.translate(right,bottom);
        //右下角实心圆
        drawPaint.setColor(getResources().getColor(R.color.pdf_comment_circle));
        drawPaint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(0,0,borderPadding/2f,drawPaint);
        //右下角圆边框
        drawPaint.setColor(getResources().getColor(R.color.pdf_comment_border));
        drawPaint.setStyle(Paint.Style.STROKE);
        canvas.drawCircle(0,0,borderPadding/2f,drawPaint);
        //拉伸图标
        canvas.drawBitmap(expendBitmap,-expendBitmap.getWidth()/2f,-expendBitmap.getHeight()/2f,drawPaint);
        canvas.restore();
    }
}
