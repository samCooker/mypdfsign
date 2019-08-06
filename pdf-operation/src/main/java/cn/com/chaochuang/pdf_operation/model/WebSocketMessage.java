/*
 * FileName:    MessageInfo.java
 * Description:
 * Company:     南宁超创信息工程有限公司
 * Copyright:   ChaoChuang (c) 2018
 * History:     2018年05月15日 (shicx) 1.0 Create
 */

package cn.com.chaochuang.pdf_operation.model;

/**
 * @author shicx
 */
public class WebSocketMessage {

    public static final String TYPE_COMMENT_ID = "commentId";
    public static final String TYPE_PAGE_REFRESH = "filePageRefresh";
    public static final String TYPE_PAGE_CHANGE = "filePageChange";
    public static final String TYPE_GET_PAGE = "getPageNo";
    public static final String TYPE_HANDWRITING_ADD = "handwritingDataAdd";
    public static final String TYPE_HANDWRITING_DELETE = "handwritingDelete";
    public static final String TYPE_HANDWRITING_UPDATE = "handwritingUpdate";
    public static final String TYPE_REPEAT_CONN = "connectionRepeat";
    public static final String TYPE_FINISH_CONN = "connectionFinish";
    public static final String TYPE_ERROR_CONN = "connectionError";

    private String messageType;
    private String messageData;
    private String recordId;
    private Integer pageNo;

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getMessageData() {
        return messageData;
    }

    public void setMessageData(String messageData) {
        this.messageData = messageData;
    }

    public String getRecordId() {
        return recordId;
    }

    public void setRecordId(String recordId) {
        this.recordId = recordId;
    }

    public Integer getPageNo() {
        return pageNo;
    }

    public void setPageNo(Integer pageNo) {
        this.pageNo = pageNo;
    }
}

