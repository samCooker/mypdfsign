package cn.com.chaochuang.pdf_operation.ui;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;

import cn.com.chaochuang.pdf_operation.R;
import cn.com.chaochuang.pdf_operation.model.AppResponse;
import cn.com.chaochuang.pdf_operation.model.AttachData;
import cn.com.chaochuang.pdf_operation.utils.Constants;
import cn.com.chaochuang.pdf_operation.utils.OkHttpUtil;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static cn.com.chaochuang.pdf_operation.utils.Constants.MSG_RESPONSE_MSG;
import static cn.com.chaochuang.pdf_operation.utils.Constants.MSG_RESPONSE_SUCCESS;

/**
 * 2019-8-14
 *
 * @author Shicx
 */
public class AttachListFragment extends DialogFragment {

    private Context context;
    private View listViewContainer;
    private ListView listView;
    private Button cancelButton;
    private ProgressBar loadingBar;
    private OkHttpUtil httpUtil;
    private MyHandler myHandler;

    private String baseUrl;
    private List<AttachData> attachList;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context=context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        listViewContainer = inflater.inflate(R.layout.fg_attach_list,container);
        myHandler = new MyHandler(this);
        return listViewContainer;
    }

    @Override
    public void onStart() {
        super.onStart();

        cancelButton = listViewContainer.findViewById(R.id.btn_setting_cancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        loadingBar = listViewContainer.findViewById(R.id.progress_loading);

        listView = listViewContainer.findViewById(R.id.list_view_attach);

        if(attachList==null||attachList.size()>0) {
            httpUtil.get(baseUrl + Constants.URL_DOC_ATTACH_LIST, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    sendMessage(MSG_RESPONSE_MSG, "请求错误");
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String data = response.body().string();
                    AppResponse resData = JSON.parseObject(data, AppResponse.class);
                    if (resData.getData() != null) {
                        attachList = JSON.parseArray(resData.getData().toString(), AttachData.class);
                    }
                    sendMessage(MSG_RESPONSE_SUCCESS, null);
                }
            });
        }else{
            showAttachList();
        }

    }

    private void showAttachList(){
        loadingBar.setVisibility(View.INVISIBLE);
        AttachListAdapter attachListAdapter = new AttachListAdapter(context,attachList);
        listView.setAdapter(attachListAdapter);
    }

    public void showFragmentDlg(android.support.v4.app.FragmentManager fragmentManager, String tag, OkHttpUtil httpUtil,String baseUrl){
        this.show(fragmentManager,tag);
        this.httpUtil = httpUtil;
        this.baseUrl = baseUrl;
    }

    private void sendMessage(int what,String message){
        Message msg = Message.obtain();
        msg.what = what;
        msg.obj = message;
        myHandler.sendMessage(msg);
    }

    private void showToast(String msg){
        loadingBar.setVisibility(View.INVISIBLE);
        Toast.makeText(this.context,msg,Toast.LENGTH_LONG).show();
    }

    private static class MyHandler extends Handler {
        private final WeakReference<AttachListFragment> fragmentWeakReference;

        public MyHandler(AttachListFragment fragment) {
            fragmentWeakReference = new WeakReference<>(fragment);
        }

        @Override
        public void handleMessage(Message msg) {
            AttachListFragment listFragment = fragmentWeakReference.get();
            Object dataObj = msg.obj;
            switch (msg.what){
                case MSG_RESPONSE_MSG:
                    listFragment.showToast(dataObj.toString());
                    break;
                case MSG_RESPONSE_SUCCESS:
                    listFragment.showAttachList();
                    break;
            }
        }
    }

}
