package cn.com.chaochuang.pdf_operation.utils;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * 2019-4-23
 * @author Shicx
 */
public class Constants {

    public static final SimpleDateFormat DATA_FORMAT1 = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA);

    public static final String HEADER_TOKEN_NAME = "token";

    //PDF后端服务URL(*必须)
    public static final String KEY_SERVER_URL = "serverUrl";
    //PDF后端服务token(*必须)
    public static final String KEY_SERVER_TOKEN = "serverToken";
    //文件ID(*必须)
    public static final String PARAM_FILE_ID = "fileId";
    //点击保存时是否在服务端更新原PDF文件（默认false）
    public static final String KEY_UPDATE_PDF = "updatePdf";
    //隐藏文字批注(默认false)
    public static final String KEY_HIDE_TXT_BTN = "hideTxtBtn";
    //用户ID(可选，保存批注信息时需要)
    public static final String PARAM_USER_ID = "userId";
    //用户姓名(可选，保存批注信息时需要)
    public static final String PARAM_USER_NAME = "userName";
    //打开PDF时的默认页号，从0开始（默认0）
    public static final String KEY_CURRENT_PAGE = "currentPage";
    //是否公文模式，公文模式下显示提交按钮（默认false）
    public static final String KEY_IS_DOC_MODE = "isDocMode";
    //是否只读模式(默认 false)
    public static final String KEY_IS_READ_MODE = "isReadMode";
    //是否隐藏批注(默认false)
    public static final String KEY_IS_HIDE_ANNOT = "hideAnnot";

    public static final String KEY_ENTRY_LIST = "entryList";

    //--公文手写签批相关--
    public static final String KEY_FLOW_INST_ID = "flowInstId";
    public static final String KEY_NODE_INST_ID = "nodeInstId";
    //把多个PDF合成，再打开合成的PDF
    public static final String KEY_MULTI_TO_ONE = "multiToOne";

    //--会议同步相关--
    public static final String KEY_WEB_SOCKET_URL = "webSocketUrl";
    public static final String KEY_MEETING_REOCRD_ID = "meetingRecordId";
    //是否主持人
    public static final String KEY_IS_HOST = "isHost";

    //-- 后端服务URL --
    public static final String URL_HANDWRITING_LIST = "comment/list.mo";
    public static final String URL_HANDWRITING_SAVE = "comment/save.mo";
    public static final String URL_HANDWRITING_DELETE = "comment/delete.mo";
    public static final String URL_SAVE_PDF = "comment/savetopdf.mo";
    public static final String URL_GET_MD5 = "comment/getmdf.mo";
    //获取合成的pdf的md5值
    public static final String URL_GET_ME_MD5 = "comment/getmemdf.mo";
    public static final String URL_DOWNLOAD_FILE = "comment/download.mo";
    public static final String URL_DOWNLOAD_ME_FILE = "comment/downloadme.mo";

    //-- handler 状态码 --
    public static final int MSG_TOAST = 0;
    public static final int MSG_FIND_COMMENT_LIST = 1;
    public static final int MSG_SAVE_COMMENT_LIST = 2;
    public static final int MSG_SAVE_COMMENT_AND_SUBMIT = 21;
    public static final int MSG_DEL_COMMENT_LIST = 3;
    public static final int MSG_SHOW_CONFIRM_DLG = 4;
    public static final int MSG_SHOW_LOADING = 5;
    public static final int MSG_HIDE_LOADING = 6;
    public static final int MSG_REFRESH_PDF_VIEW = 7;
    public static final int MSG_PDF_PAGE_CHANGE = 8;
    public static final int MSG_RESPONSE_MSG = 9;
    public static final int MSG_DOWNLOAD_ERROR = 10;
    public static final int MSG_CHANGE_LOADING = 11;
    public static final int MSG_FONT_DOWNLOAD_SUCCESS = 12;
}
