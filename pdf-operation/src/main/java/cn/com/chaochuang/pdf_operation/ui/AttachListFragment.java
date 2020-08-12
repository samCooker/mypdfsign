package cn.com.chaochuang.pdf_operation.ui;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.DisplayMetrics;
import android.util.Log;
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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import cn.com.chaochuang.pdf_operation.R;
import cn.com.chaochuang.pdf_operation.model.AppResponse;
import cn.com.chaochuang.pdf_operation.model.DocAttachData;
import cn.com.chaochuang.pdf_operation.model.DocData;
import cn.com.chaochuang.pdf_operation.utils.Constants;
import cn.com.chaochuang.pdf_operation.utils.FileUtil;
import cn.com.chaochuang.pdf_operation.utils.OkHttpUtil;
import cn.com.chaochuang.pdf_operation.utils.WpsOpener;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import static cn.com.chaochuang.pdf_operation.utils.Constants.MSG_DOWNLOAD_ERROR;
import static cn.com.chaochuang.pdf_operation.utils.Constants.MSG_RESPONSE_MSG;
import static cn.com.chaochuang.pdf_operation.utils.Constants.MSG_RESPONSE_SUCCESS;
import static cn.com.chaochuang.pdf_operation.utils.Constants.MSG_SHOW_LOADING;
import static cn.com.chaochuang.pdf_operation.utils.Constants.URL_DOC_DOWNLOAD_FILE;
import static cn.com.chaochuang.pdf_operation.utils.Constants.URL_DOC_GET_MD5;

/**
 * 2019-8-14
 *
 * @author Shicx
 */
public class AttachListFragment extends DialogFragment {

    private Context context;
    private View listViewContainer;
    private ListView listView;
    private ScrollView scrollView;
    private AttachListAdapter attachListAdapter;
    private TextView cancelButton;
    private ProgressBar loadingBar;
    private OkHttpUtil httpUtil;
    private MyHandler myHandler;

    private String baseUrl;
    private String businessId;
    private List<DocAttachData> attachList = new ArrayList<>();
    private Map<String,View> viewMap = new HashMap<>();

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

        DisplayMetrics dm = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);

        int height = dm.heightPixels*2/3;
        scrollView = listViewContainer.findViewById(R.id.scroll_attach);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,height);
        scrollView.setLayoutParams(layoutParams);

        cancelButton = listViewContainer.findViewById(R.id.btn_setting_cancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        loadingBar = listViewContainer.findViewById(R.id.progress_loading);

        listView = listViewContainer.findViewById(R.id.list_view_attach);

        if(attachList.size()==0) {
            loadingBar.setVisibility(View.VISIBLE);
            httpUtil.get(baseUrl + "mobile/doc/attachlist.mo?entityId=" + businessId, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    sendMessage(MSG_RESPONSE_MSG, "请求错误");
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String data = response.body().string();
                    AppResponse resData = JSON.parseObject(data, AppResponse.class);
                    if (resData.getData() != null) {


                        DocData docData = JSON.parseObject(resData.getData().toString(), DocData.class);
                        if(docData.getDocFile()!=null){
                            attachList.add(docData.getDocFile());
                            attachList.add(docData.getPdfFile());
                        }
                        if(docData.getPdfFileList()!=null&&docData.getPdfFileList().size()>0){
                            attachList.addAll(docData.getPdfFileList());
                        }
                        if(docData.getAttachList()!=null&docData.getAttachList().size()>0){
                            attachList.addAll(docData.getAttachList());
                        }
                        sendMessage(MSG_RESPONSE_SUCCESS, null);
                    }else{
                        sendMessage(MSG_RESPONSE_MSG, resData.getMessage()!=null?resData.getMessage():"获取附件失败");
                    }
                }
            });
        }else{
            showAttachList();
        }

    }

    private void showAttachList(){
        loadingBar.setVisibility(View.GONE);
        attachListAdapter = new AttachListAdapter(context,attachList);
        listView.setAdapter(attachListAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                DocAttachData attachData = attachList.get(position);
                viewMap.put(attachData.getId(),view);
                downloadOrOpenFile(position);
            }
        });
    }

    /**
     * 下载文件
     */
    private void downloadOrOpenFile(final int index){
        sendMessage(MSG_SHOW_LOADING,index+"@@正在获取");
        final DocAttachData attachData = attachList.get(index);
         httpUtil.get(baseUrl + URL_DOC_GET_MD5 + "?id=" + attachData.getId(), new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                sendMessage(MSG_RESPONSE_MSG,"文件下载失败");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                File pdfRoot = new File(context.getExternalCacheDir(),"download_files");
                if(!pdfRoot.exists()){
                    pdfRoot.mkdir();
                }
                if(response.isSuccessful()&&response.body()!=null) {
                    String data = response.body().string();
                    AppResponse resData = JSON.parseObject(data, AppResponse.class);
                    if(resData.getData()!=null) {
                        String md5 = resData.getData().toString();
                        File pdfFile = new File(pdfRoot,md5+"/"+attachData.getTrueName());
                        String fileMd5 = FileUtil.getFileMd5Code(pdfFile);
                        boolean isSame = fileMd5!=null&&fileMd5.equals(md5);
                        if(pdfFile.exists()&&isSame){
                            sendMessage(MSG_SHOW_LOADING,index+"@@正在打开");
                            WpsOpener.wpsOpenFile(context,pdfFile,null);
                        }else{
                            //下载文件
                            downloadFile(pdfFile,index);
                        }
                    }
                }else{
                    sendMessage(MSG_RESPONSE_MSG,"文件下载失败，请尝试重新打开");
                }
            }
        });
    }

    private void downloadFile(final File file,final int index){
        sendMessage(MSG_SHOW_LOADING,index+"@@正在下载");
        if(!file.getParentFile().exists()){
            file.getParentFile().mkdirs();
        }
        DocAttachData attachData = attachList.get(index);

        OkHttpClient httpClient = new OkHttpClient.Builder().connectTimeout(20, TimeUnit.SECONDS).build();
        Request request = new Request.Builder().url(baseUrl +URL_DOC_DOWNLOAD_FILE+"?id=" + attachData.getId()).header(Constants.HEADER_IS_JWT,"true").header(Constants.HEADER_TOKEN_NAME, httpUtil.getServerToken()).build();
        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                sendMessage(MSG_RESPONSE_MSG,"文件下载失败，请尝试重新打开");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                ResponseBody responseBody = null;
                BufferedInputStream bis = null;
                FileOutputStream fos = null;
                try {
                    if (call.isCanceled()) {
                        sendMessage(MSG_DOWNLOAD_ERROR,"已取消文件下载");
                        return;
                    }
                    if (response.isSuccessful()) {
                        responseBody = response.body();
                        long total = responseBody.contentLength();
                        bis = new BufferedInputStream(responseBody.byteStream());
                        fos = new FileOutputStream(file);
                        byte[] bytes = new byte[1024 * 8];
                        int len;
                        long current = 0;
                        while ((len = bis.read(bytes)) != -1) {
                            fos.write(bytes, 0, len);
                            fos.flush();
                            current += len;
                            //计算进度
                            int progress = (int) (100 * current / total);
                            Log.d("attach download","下载进度："+progress);
                            sendMessage(MSG_SHOW_LOADING,index+"@@"+progress+"%");
                        }
                        WpsOpener.wpsOpenFile(context,file,null);
                    } else {
                        sendMessage(MSG_RESPONSE_MSG,"文件下载失败");
                    }
                } catch (Exception e) {
                    sendMessage(MSG_RESPONSE_MSG,"文件下载异常");
                } finally {
                    if (null != responseBody) {
                        responseBody.close();
                    }
                    if(bis!=null) {
                        bis.close();
                    }
                    if(fos!=null) {
                        fos.close();
                    }
                }
            }
        });
    }

    public void updateDownloadItem(String indexStr,String progress){
        int index = Integer.parseInt(indexStr);
        DocAttachData attachData = attachList.get(index);
        View view = viewMap.get(attachData.getId());
        attachListAdapter.updateView(progress,view);
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
                case MSG_SHOW_LOADING:
                    String[] params = dataObj.toString().split("@@");
                    listFragment.updateDownloadItem(params[0],params[1]);
                    break;
            }
        }
    }

}
