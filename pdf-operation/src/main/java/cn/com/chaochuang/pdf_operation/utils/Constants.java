package cn.com.chaochuang.pdf_operation.utils;

/**
 * 2019-4-23
 *
 * @author Shicx
 */

public class Constants {

    public static final String HEADER_TOKEN_NAME = "token";

    public static final String KEY_DOWNLOAD_DIALOG_SHOW="downloadDialogShow";
    public static final String KEY_DOWNLOAD_TXT="downloadTxt";
    public static final String KEY_DOWNLOAD_PROGRESS="downloadProgress";
    public static final String KEY_SHOW_MESSAGE="showMessage";
    public static final String KEY_PDF_PAGE="pdfPage";
    public static final String KEY_FILE_PATH="pdfFilePath";
    public static final String KEY_SERVER_URL="serverUrl";
    public static final String KEY_SERVER_TOKEN="serverToken";
    public static final String KEY_PEN_ONLY="penOnly";

    // 会议同步相关
    public static final String KEY_WEB_SOCKET_URL = "webSocketUrl";
    public static final String KEY_MEETING_REOCRD_ID="meetingRecordId";
    //是否主持人
    public static final String KEY_IS_HOST="isHost";

    public static final String PARAM_FILE_ID="fileId";
    public static final String PARAM_USER_ID="userId";
    public static final String PARAM_USER_NAME="userName";

    public static final String URL_HANDWRITING_LIST ="handwriting/list.mo";
    public static final String URL_HANDWRITING_SAVE ="handwriting/save.mo";
    public static final String URL_HANDWRITING_DELETE ="handwriting/delete.mo";
    public static final String URL_GET_MD5 ="handwriting/getmdf.mo";
    public static final String URL_DOWNLOAD_FILE ="handwriting/download.mo";
    public static final String URL_FIND_MEETING_MEMBERS ="wsdata/meeting/members.mo";

    public static final String BC_HANDWRITING_LIST="handwritingList";
    public static final String BC_SHOW_TIP ="showTip";
    public static final String BC_SHOW_LOADING ="showLoading";
    public static final String BC_HIDE_LOADING ="hideLoading";
    public static final String BC_RESPONSE_FAILURE ="responseFailure";
    public static final String BC_RESPONSE_SUCCESS ="responseSuccess";
    public static final String BC_SAVE_HANDWRITING="saveHandwriting";
    public static final String BC_DELETE_HANDWRITING="deleteHandwriting";
    public static final String BC_SAVE_HANDWRITING_SUCCESS="saveHandwritingSuccess";
    public static final String BC_DOWNLOAD_FILE="downloadFile";
    public static final String BC_DOWNLOAD_FILE_FINISH="downloadFileFinish";
    public static final String BC_REFRESH_PDF_VIEW="refreshPdfView";
    public static final String BC_CHANGE_PAGE="changePage";
}
