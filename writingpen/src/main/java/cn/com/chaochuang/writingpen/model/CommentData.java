package cn.com.chaochuang.writingpen.model;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.RectF;

import java.util.Date;

/**
 * 2019-7-12
 *
 * @author Shicx
 */
public class CommentData {

    /**
     * 签批类型
     */
    public static final String TYPE_HANDWRITING = "0";
    public static final String TYPE_TEXT = "1";

    private String id;
    /**
     * 页号
     */
    private int pageNo;
    /**
     * 图片相对左上角的坐标
     */
    private float px;
    private float py;
    /**
     * pdf文件的物理高度（用于计算相对pdf文件的Y坐标）
     */
    private float pdfFileHeight;
    private float pdfFileWidth;
    /**
     * 放大缩小倍数
     */
    private float zoom;
    /**
     *
     */
    private String fileId;
    /**
     * 签批用户ID
     */
    private String signerId;
    /**
     * 签批用户姓名
     */
    private String signerName;
    /**
     * 签批时间
     */
    private Date signTime;
    /**
     * 图片的真实高度和宽度
     */
    private float imageWidth;
    private float imageHeight;
    /**
     * 签批类型（手写:0、文字:1）
     */
    private String signType;
    /**
     * 签批图片base64编码
     */
    private String signContent;
    /**
     * 文字内容
     */
    private String textContent;
    /**
     * 签批图片bitmap(不作为服务端保存的参数)
     */
    private Bitmap imageBitmap;
    /**
     * 屏幕上图片的原坐标
     */
    private float signX;
    private float signY;
    /**
     * 公文相关
     */
    private String flowInstId;
    private String nodeInstId;
    /**
     * 是否生成签批的pdf
     */
    private Boolean newSignPdf;

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

    public String getSignContent() {
        return signContent;
    }

    public void setSignContent(String signContent) {
        this.signContent = signContent;
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

    public float getSignX() {
        return signX;
    }

    public void setSignX(float signX) {
        this.signX = signX;
    }

    public float getSignY() {
        return signY;
    }

    public void setSignY(float signY) {
        this.signY = signY;
    }

    public String getSignType() {
        return signType;
    }

    public void setSignType(String signType) {
        this.signType = signType;
    }

    public String getFlowInstId() {
        return flowInstId;
    }

    public void setFlowInstId(String flowInstId) {
        this.flowInstId = flowInstId;
    }

    public String getNodeInstId() {
        return nodeInstId;
    }

    public void setNodeInstId(String nodeInstId) {
        this.nodeInstId = nodeInstId;
    }

    public float getZoom() {
        return zoom;
    }

    public void setZoom(float zoom) {
        this.zoom = zoom;
    }

    public String getTextContent() {
        return textContent;
    }

    public void setTextContent(String textContent) {
        this.textContent = textContent;
    }

    public Boolean getNewSignPdf() {
        return newSignPdf;
    }

    public void setNewSignPdf(Boolean newSignPdf) {
        this.newSignPdf = newSignPdf;
    }
}
