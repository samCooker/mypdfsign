/*
 * FileName:    PdfCommentBean.java
 * Description: 
 * Company:     南宁超创信息工程有限公司
 * Copyright:    ChaoChuang (c) 2019
 * History:     2019年08月14日 (shicx) 1.0 Create
 */
package cn.com.chaochuang.pdf_operation.model;

import cn.com.chaochuang.writingpen.model.CommentData;

import java.util.List;

/**
 * @author shicx
 */
public class PdfCommentBean {

    private Integer curPage;
    private List<CommentData> handwritingList;
    private List<CommentData> textDataList;

    public Integer getCurPage() {
        return curPage;
    }

    public void setCurPage(Integer curPage) {
        this.curPage = curPage;
    }

    public List<CommentData> getHandwritingList() {
        return handwritingList;
    }

    public void setHandwritingList(List<CommentData> handwritingList) {
        this.handwritingList = handwritingList;
    }

    public List<CommentData> getTextDataList() {
        return textDataList;
    }

    public void setTextDataList(List<CommentData> textDataList) {
        this.textDataList = textDataList;
    }
}
