package cn.com.chaochuang.pdf_operation.ui;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.com.chaochuang.pdf_operation.R;
import cn.com.chaochuang.pdf_operation.model.AppResponse;
import cn.com.chaochuang.pdf_operation.model.DocAttachData;
import cn.com.chaochuang.pdf_operation.model.DocData;
import cn.com.chaochuang.pdf_operation.model.DocFlowData;
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
public class FlowListFragment extends DialogFragment {

    private Context context;
    private View listViewContainer;
    private ListView listView;
    private ScrollView scrollView;
    private TextView cancelButton;
    private ProgressBar loadingBar;
    private OkHttpUtil httpUtil;

    private String baseUrl;
    private String businessId;
    private MyHandler myHandler;
    private List<DocFlowData> flowDataList = new ArrayList<>();
    private Map<String,View> viewMap = new HashMap<>();

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context=context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        listViewContainer = inflater.inflate(R.layout.fg_flow_list,container);
        myHandler = new MyHandler(this);
        return listViewContainer;
    }

    @Override
    public void onStart() {
        super.onStart();

        DisplayMetrics dm = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);

        int height = dm.heightPixels*2/3;
        scrollView = listViewContainer.findViewById(R.id.scroll_flow);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,height);
        scrollView.setLayoutParams(layoutParams);

        cancelButton = listViewContainer.findViewById(R.id.btn_flow_cancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        loadingBar = listViewContainer.findViewById(R.id.flow_data_loading);

        listView = listViewContainer.findViewById(R.id.list_view_flow);

        if(flowDataList.size()==0) {
            loadingBar.setVisibility(View.VISIBLE);
            httpUtil.get(baseUrl + "mobile/doc/task/historylist.mo?id=" + businessId, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    sendMessage(MSG_RESPONSE_MSG, "请求错误");
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String data = response.body().string();
                    AppResponse resData = JSON.parseObject(data, AppResponse.class);
                    if (resData.getData() != null) {
                        flowDataList = JSON.parseArray(resData.getData().toString(), DocFlowData.class);
                        sendMessage(MSG_RESPONSE_SUCCESS, null);
                    }else{
                        sendMessage(MSG_RESPONSE_MSG, resData.getMessage()!=null?resData.getMessage():"获取数据失败");
                    }
                }
            });
        }else{
            showFlowList();
        }

    }

    private void showFlowList(){
        loadingBar.setVisibility(View.GONE);
        FlowListAdapter listAdapter = new FlowListAdapter(context,flowDataList);
        listView.setAdapter(listAdapter);
    }

    public void showFragmentDlg(android.support.v4.app.FragmentManager fragmentManager, String tag, OkHttpUtil httpUtil,String baseUrl,String businessId){
        this.show(fragmentManager,tag);
        this.httpUtil = httpUtil;
        this.baseUrl = baseUrl;
        this.businessId = businessId;
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
        private final WeakReference<FlowListFragment> fragmentWeakReference;

        public MyHandler(FlowListFragment fragment) {
            fragmentWeakReference = new WeakReference<>(fragment);
        }

        @Override
        public void handleMessage(Message msg) {
            FlowListFragment listFragment = fragmentWeakReference.get();
            Object dataObj = msg.obj;
            switch (msg.what){
                case MSG_RESPONSE_MSG:
                    listFragment.showToast(dataObj.toString());
                    break;
                case MSG_RESPONSE_SUCCESS:
                    listFragment.showFlowList();
                    break;
            }
        }
    }

}
