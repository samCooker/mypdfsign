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

    public static final String TYPE_HANDWRITING = "0";

    private String id;
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

    private float zoom;

    private String fileId;
    private String signerId;
    private String signerName;
    private Date signTime;

    /**
     * 图片的真实高度和宽度
     */
    private float imageWidth;
    private float imageHeight;

    private String signType;
    /**
     * 图片base64和bitmap
     */
    private String signContent;
    private Bitmap imageBitmap;

    /**
     * 屏幕上图片的原坐标
     */
    private float signX;
    private float signY;

    /**
     * 文字内容
     */
    private String txtContent;

    //TODO 暂时无用
    private Rect srcRect;
    private RectF targetRect;

    /**
     * 公文相关
     */
    private String flowInstId;
    private String nodeInstId;

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

    public Rect getSrcRect() {
        return srcRect;
    }

    public void setSrcRect(Rect srcRect) {
        this.srcRect = srcRect;
    }

    public RectF getTargetRect() {
        return targetRect;
    }

    public void setTargetRect(RectF targetRect) {
        this.targetRect = targetRect;
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

    public String getTxtContent() {
        return txtContent;
    }

    public void setTxtContent(String txtContent) {
        this.txtContent = txtContent;
    }
}
