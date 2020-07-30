package cn.com.chaochuang.pdf_operation.utils;

import android.content.Intent;
import android.os.Message;
import android.util.Log;
import cn.com.chaochuang.pdf_operation.SignPdfView;
import cn.com.chaochuang.pdf_operation.model.WebSocketMessage;
import cn.com.chaochuang.writingpen.model.CommentData;
import com.alibaba.fastjson.JSON;
import okhttp3.*;

import java.util.concurrent.TimeUnit;

import static cn.com.chaochuang.pdf_operation.utils.Constants.MSG_PDF_PAGE_CHANGE;
import static cn.com.chaochuang.pdf_operation.utils.Constants.MSG_SHOW_CONFIRM_DLG;

/**
 * 2018-5-14
 *
 * @author Shicx
 */

public class MeetingWsListener extends WebSocketListener {

    private static final String TAG = MeetingWsListener.class.getSimpleName();

    private WebSocket socketClient;
    private SignPdfView signPdfView;
    private String socketUrl;


    @Override
    public void onOpen(WebSocket webSocket, Response response) {
        super.onOpen(webSocket, response);
        Log.d(TAG,"onOpen");
        socketClient = webSocket;
    }

    @Override
    public void onFailure(WebSocket webSocket, Throwable t, Response response) {
        super.onFailure(webSocket, t, response);
        t.printStackTrace();
        Log.e(TAG,"同步出现了错误:"+t.getMessage());
    }

    @Override
    public void onMessage(WebSocket webSocket, String text) {
        super.onMessage(webSocket, text);
        Log.d(TAG,"onMessage:"+text);
        WebSocketMessage messageInfo = JSON.parseObject(text,WebSocketMessage.class);
        if(messageInfo==null){
            return ;
        }
        switch (messageInfo.getMessageType()){
            case WebSocketMessage.TYPE_HANDWRITING_ADD:
                CommentData handwritingData = JSON.parseObject(messageInfo.getMessageData(), CommentData.class);
                if(signPdfView!=null&&handwritingData!=null){
                    signPdfView.addHandwritingDataAndRefresh(handwritingData);
                }
                break;
            case WebSocketMessage.TYPE_HANDWRITING_DELETE:
                if(signPdfView!=null){
                    signPdfView.removeHandwritingData(messageInfo.getMessageData());
                }
                break;
            case WebSocketMessage.TYPE_PAGE_CHANGE:
                if(signPdfView!=null&&messageInfo.getPageNo()!=null){
                    Message msg = Message.obtain();
                    msg.what = MSG_PDF_PAGE_CHANGE;
                    msg.arg1 = messageInfo.getPageNo();
                    signPdfView.sendMessage(msg);
                }
                break;
            case WebSocketMessage.TYPE_ERROR_CONN:
                if(signPdfView!=null){
                    signPdfView.sendMessage(MSG_SHOW_CONFIRM_DLG,"服务连接错误");
                }
                break;
            default:
        }
    }

    @Override
    public void onClosed(WebSocket webSocket, int code, String reason) {
        super.onClosed(webSocket, code, reason);
        Log.d(TAG,"code:" + code + "onClosed:" + reason);
        closeSocket();
        if(signPdfView!=null) {
            signPdfView.sendMessage(MSG_SHOW_CONFIRM_DLG, "已断开连接，请重新打开");
        }
    }

    @Override
    public void onClosing(WebSocket webSocket, int code, String reason) {
        super.onClosing(webSocket, code, reason);
    }

    public void startRunning(SignPdfView signPdfView,String socketUrl){
        this.signPdfView = signPdfView;
        this.socketUrl = socketUrl;
        OkHttpClient client = new OkHttpClient.Builder().readTimeout(25000,  TimeUnit.MILLISECONDS).build();
        Request request = new Request.Builder().url(this.socketUrl).build();
        client.newWebSocket(request, this);
        client.dispatcher().executorService().shutdown();
    }

    public void sendMessage(String type,String meetingRecordId,String msg){
        if(socketClient!=null) {
            WebSocketMessage webSocketMessage = new WebSocketMessage();
            webSocketMessage.setMessageData(msg);
            webSocketMessage.setMessageType(type);
            webSocketMessage.setRecordId(meetingRecordId);
            socketClient.send(JSON.toJSONString(webSocketMessage));
        }
    }

    public void sendMessage(WebSocketMessage webSocketMessage){
        if(socketClient!=null) {
            socketClient.send(JSON.toJSONString(webSocketMessage));
        }
    }

    public void closeSocket(){
        if(socketClient!=null) {
            socketClient.close(1000, null);
        }
    }

}
