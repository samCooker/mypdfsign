/*
 * FileName:    PdfCommentBean.java
 * Description: 
 * Company:     南宁超创信息工程有限公司
 * Copyright:    ChaoChuang (c) 2019
 * History:     2019年08月14日 (shicx) 1.0 Create
 */
package cn.com.chaochuang.pdf_operation.model;

import com.github.barteksc.pdfviewer.model.HandwritingData;

import java.util.List;

/**
 * @author shicx
 */
public class PdfCommentBean {

    private Integer curPage;
    private List<HandwritingData> handwritingList;

    public Integer getCurPage() {
        return curPage;
    }

    public void setCurPage(Integer curPage) {
        this.curPage = curPage;
    }

    public List<HandwritingData> getHandwritingList() {
        return handwritingList;
    }

    public void setHandwritingList(List<HandwritingData> handwritingList) {
        this.handwritingList = handwritingList;
    }
}
