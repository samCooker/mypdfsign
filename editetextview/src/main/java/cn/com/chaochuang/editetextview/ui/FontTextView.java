package cn.com.chaochuang.editetextview.ui;

import android.content.Context;
import android.graphics.*;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import java.io.File;

import cn.com.chaochuang.editetextview.R;
import cn.com.chaochuang.editetextview.data.TouchPos;


/**
 * Created by Shicx on 2020-04-07.
 */
public class FontTextView extends AppCompatTextView {

    private Context context;
    private float downX, downY, originLeft, originTop, originRight, originBottom;
    private int viewMinWidth = 200, viewMinHeight = 100;

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
    private int viewTouchPadding = 40;

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
        this.borderPaint.setColor(getResources().getColor(R.color.edit_view_border));
        this.borderPaint.setPathEffect(new DashPathEffect(new float[]{4, 4}, 0));

        //图形
        moveBitmap = BitmapFactory.decodeResource(getResources(),R.drawable.ic_move);
        expendBitmap = BitmapFactory.decodeResource(getResources(),R.drawable.ic_expend);
        drawPaint = new Paint();

        setPadding(10,10,10,10);
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

    public void scale(float ex, float ey) {

        int zoomX = (int) (originRight + ex), zoomY = (int) (originBottom + ey);
        if (zoomX < viewMinWidth) {
            zoomX = viewMinWidth;
        }
        if (zoomY < viewMinHeight) {
            zoomY = viewMinHeight;
        }
        setLayoutParams(new RelativeLayout.LayoutParams(zoomX, zoomY));
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
                        case TouchPos.POS_CENTER:
                            setX(l);
                            setY(t);
                            break;
                        //点击右下角放大缩小
                        case TouchPos.POS_BOTTOM_RIGHT:
                            scale(moveX, moveY);
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

        //画上边框
        canvas.drawRect(getLeft(), getTop(), getRight(), getBottom(), borderPaint);
        canvas.drawBitmap(moveBitmap,getLeft()-20,getTop()-20,drawPaint);
        canvas.drawBitmap(expendBitmap,getRight()-20,getBottom()-20,drawPaint);
    }
}
