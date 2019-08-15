package cn.com.chaochuang.writingpen.model;

import android.graphics.Bitmap;

/**
 * 2019-8-13
 *
 * @author Shicx
 */
public class SignBitmapData {

    private Bitmap signBitmap;
    private int minX;
    private int minY;
    private int maxX;
    private int maxY;

    public Bitmap getSignBitmap() {
        return signBitmap;
    }

    public void setSignBitmap(Bitmap signBitmap) {
        this.signBitmap = signBitmap;
    }

    public int getMinX() {
        return minX;
    }

    public void setMinX(int minX) {
        this.minX = minX;
    }

    public int getMinY() {
        return minY;
    }

    public void setMinY(int minY) {
        this.minY = minY;
    }

    public int getMaxX() {
        return maxX;
    }

    public void setMaxX(int maxX) {
        this.maxX = maxX;
    }

    public int getMaxY() {
        return maxY;
    }

    public void setMaxY(int maxY) {
        this.maxY = maxY;
    }
}
