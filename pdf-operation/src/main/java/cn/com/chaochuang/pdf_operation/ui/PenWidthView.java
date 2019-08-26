package cn.com.chaochuang.pdf_operation.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/**
 * 2019-8-21
 *
 * @author Shicx
 */
public class PenWidthView extends View {

    private float penWidth=10F;
    private int penColor= Color.BLACK;
    private int px,py;

    private Paint paint;

    public PenWidthView(Context context) {
        super(context);
        initPaint();
    }

    public PenWidthView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initPaint();
    }

    public PenWidthView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initPaint();
    }

    public void setPenConfig(float penWidth, int penColor){
        this.penWidth = penWidth;
        this.penColor = penColor;
        invalidate();
    }

    public void initPaint(){
        paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);//结束的笔画为圆心
        paint.setStrokeJoin(Paint.Join.ROUND);//连接处元
        paint.setAlpha(0xFF);
        paint.setAntiAlias(true);
        paint.setStrokeMiter(1.0f);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        paint.setStrokeWidth(penWidth);
        paint.setColor(penColor);
        int x = (px - 400) /2;
        int y = py/2;
        canvas.drawLine(x,y,x+400,y,paint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(measureWidth(widthMeasureSpec), measureHeight(heightMeasureSpec));
    }

    private int measureWidth(int measureSpec) {
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        int result = 100;
        if (specMode == MeasureSpec.AT_MOST) {//相当于我们设置为wrap_content
            result = specSize;
        } else if (specMode == MeasureSpec.EXACTLY) {//相当于我们设置为match_parent或者为一个具体的值
            result = specSize;
        }
        px = result;
        return result;
    }

    private int measureHeight(int measureSpec) {
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        int result = 100;
        if (specMode == MeasureSpec.AT_MOST) {
            result = specSize;
        } else if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        }
        py = result;
        return result;
    }
}
