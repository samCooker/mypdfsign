package com.github.barteksc.pdfviewer.model;

import android.graphics.Bitmap;

import java.util.Date;

/**
 * 2019-7-12
 *
 * @author Shicx
 */
public class HandwritingData {

    private String id;
    private int pageNo;
    /**
     * 图片相对左上角的坐标
     */
    private float px;
    private float py;
    /**
     * 图片的真实高度和宽度
     */
    private float imageWidth;
    private float imageHeight;
    /**
     * 图片base64和bitmap
     */
    private String base64Code;
    private Bitmap imageBitmap;

    /**
     * pdf文件的物理高度（用于计算相对pdf文件的Y坐标）
     */
    private float pdfFileHeight;
    private float pdfFileWidth;

    private String fileId;
    private String signerId;
    private String signerName;
    private Date signTime;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getPageNo() {
        return pageNo;
    }

    public void setPageNo(int pageNo) {
        this.pageNo = pageNo;
    }

    public float getImageWidth() {
        return imageWidth;
    }

    public void setImageWidth(float imageWidth) {
        this.imageWidth = imageWidth;
    }

    public float getImageHeight() {
        return imageHeight;
    }

    public void setImageHeight(float imageHeight) {
        this.imageHeight = imageHeight;
    }

    public String getBase64Code() {
        return base64Code;
    }

    public void setBase64Code(String base64Code) {
        this.base64Code = base64Code;
    }

    public Bitmap getImageBitmap() {
        return imageBitmap;
    }

    public void setImageBitmap(Bitmap imageBitmap) {
        this.imageBitmap = imageBitmap;
    }

    public float getPx() {
        return px;
    }

    public void setPx(float px) {
        this.px = px;
    }

    public float getPy() {
        return py;
    }

    public void setPy(float py) {
        this.py = py;
    }

    public float getPdfFileHeight() {
        return pdfFileHeight;
    }

    public void setPdfFileHeight(float pdfFileHeight) {
        this.pdfFileHeight = pdfFileHeight;
    }

    public float getPdfFileWidth() {
        return pdfFileWidth;
    }

    public void setPdfFileWidth(float pdfFileWidth) {
        this.pdfFileWidth = pdfFileWidth;
    }

    public String getSignerId() {
        return signerId;
    }

    public void setSignerId(String signerId) {
        this.signerId = signerId;
    }

    public String getSignerName() {
        return signerName;
    }

    public void setSignerName(String signerName) {
        this.signerName = signerName;
    }

    public Date getSignTime() {
        return signTime;
    }

    public void setSignTime(Date signTime) {
        this.signTime = signTime;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }
}
