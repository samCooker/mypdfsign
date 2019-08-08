package cn.com.chaochuang.pdf_operation.utils;

import android.content.Intent;
import android.util.Log;
import cn.com.chaochuang.pdf_operation.SignPdfView;
import cn.com.chaochuang.pdf_operation.model.WebSocketMessage;
import com.alibaba.fastjson.JSON;
import com.github.barteksc.pdfviewer.model.HandwritingData;
import okhttp3.*;
import okio.ByteString;

import java.util.concurrent.TimeUnit;

/**
 * 2018-5-14
 *
 * @author Shicx
 */

public class MeetingWsListener extends WebSocketListener {

    private static final String TAG = MeetingWsListener.class.getSimpleName();

    private WebSocket socketClient;

    private SignPdfView signPdfView;


    @Override
    public void onOpen(WebSocket webSocket, Response response) {
        super.onOpen(webSocket, response);
        Log.d(TAG,"onOpen");
        socketClient = webSocket;
    }

    @Override
    public void onFailure(WebSocket webSocket, Throwable t, Response response) {
        super.onFailure(webSocket, t, response);
        Log.e(TAG,"同步出现了错误:"+t.getMessage());
        closeSocket();


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
                HandwritingData handwritingData = JSON.parseObject(messageInfo.getMessageData(), HandwritingData.class);
                if(signPdfView!=null&&handwritingData!=null){
                    signPdfView.addHandwritingData(handwritingData);
                }
                break;
            case WebSocketMessage.TYPE_HANDWRITING_DELETE:
                if(signPdfView!=null){
                    signPdfView.removeHandwritingData(messageInfo.getMessageData());
                }
                break;
            case WebSocketMessage.TYPE_PAGE_CHANGE:
                if(signPdfView!=null&&messageInfo.getPageNo()!=null){
                    Intent intent = new Intent();
                    intent.setAction(Constants.BC_CHANGE_PAGE);
                    intent.putExtra(Constants.KEY_PDF_PAGE,messageInfo.getPageNo());
                    signPdfView.sendBroadcast(intent);
                }
                break;
            case WebSocketMessage.TYPE_ERROR_CONN:
                if(signPdfView!=null){
                    signPdfView.broadcastIntent(Constants.BC_SHOW_TIP,"服务连接错误");
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

    }

    @Override
    public void onClosing(WebSocket webSocket, int code, String reason) {
        super.onClosing(webSocket, code, reason);
    }

    public void startRunning(SignPdfView signPdfView,String socketUrl){
        this.signPdfView = signPdfView;
        OkHttpClient client = new OkHttpClient.Builder().readTimeout(25000,  TimeUnit.MILLISECONDS).build();
        Request request = new Request.Builder().url(socketUrl).build();
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
