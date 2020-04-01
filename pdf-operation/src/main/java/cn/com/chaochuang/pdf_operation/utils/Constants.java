package cn.com.chaochuang.pdf_operation.utils;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * 2019-4-23
 *
 * @author Shicx
 */

public class Constants {

    public static final SimpleDateFormat DATA_FORMAT1 = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA);

    public static final String HEADER_TOKEN_NAME = "token";

    public static final String KEY_DOWNLOAD_DIALOG_SHOW="downloadDialogShow";
    public static final String KEY_DOWNLOAD_TXT="downloadTxt";
    public static final String KEY_DOWNLOAD_PROGRESS="downloadProgress";
    public static final String KEY_SHOW_MESSAGE="showMessage";
    public static final String KEY_PDF_PAGE="pdfPage";
    public static final String KEY_OFFSET_X="offsetX";
    public static final String KEY_OFFSET_Y="offsetY";
    public static final String KEY_FILE_PATH="pdfFilePath";
    public static final String KEY_SERVER_URL="serverUrl";
    public static final String KEY_SERVER_TOKEN="serverToken";
    public static final String KEY_PEN_ONLY="penOnly";
    public static final String KEY_CURRENT_PAGE="currentPage";
    //是否显示提交按钮
    public static final String KEY_IS_DOC_MODE="isDocMode";
    //是否只读模式
    public static final String KEY_IS_READ_MODE="isReadMode";
    public static final String KEY_IS_HIDE_ANNOT="hideAnnot";

    //公文手写签批相关
    public static final String KEY_DOC_ID="docId";
    public static final String KEY_FLOW_INST_ID="flowInstId";
    public static final String KEY_NODE_INST_ID="nodeInstId";

    // 会议同步相关
    public static final String KEY_WEB_SOCKET_URL = "webSocketUrl";
    public static final String KEY_MEETING_REOCRD_ID="meetingRecordId";
    //是否主持人
    public static final String KEY_IS_HOST="isHost";

    public static final String PARAM_FILE_ID="fileId";
    public static final String PARAM_USER_ID="userId";
    public static final String PARAM_USER_NAME="userName";

    public static final String URL_HANDWRITING_LIST ="comment/list.mo";
    public static final String URL_HANDWRITING_SAVE ="comment/save.mo";
    public static final String URL_HANDWRITING_DELETE ="comment/delete.mo";
    public static final String URL_SAVE_PDF ="comment/savetopdf.mo";
    public static final String URL_GET_MD5 ="comment/getmdf.mo";
    public static final String URL_DOWNLOAD_FILE ="comment/download.mo";
    public static final String URL_FIND_MEETING_MEMBERS ="meeting/members.mo";
    public static final String URL_FIND_MEETING_HANDWRITING ="meeting/hwlist.mo";

    public static final int MSG_FIND_COMMENT_LIST =1;
    public static final int MSG_SAVE_COMMENT_LIST =2;
    public static final int MSG_SAVE_COMMENT_AND_SUBMIT =21;
    public static final int MSG_DEL_COMMENT_LIST =3;
    public static final int MSG_SHOW_CONFIRM_DLG =4;
    public static final int MSG_SHOW_LOADING =5;
    public static final int MSG_HIDE_LOADING =6;
    public static final int MSG_REFRESH_PDF_VIEW =7;
    public static final int MSG_PDF_PAGE_CHANGE =8;
    public static final int MSG_RESPONSE_MSG =9;
    public static final int MSG_DOWNLOAD_ERROR =10;
}
