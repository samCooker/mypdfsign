package cn.com.chaochuang.pdf_operation.ui;

import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.com.chaochuang.pdf_operation.R;
import cn.com.chaochuang.pdf_operation.model.EntryData;
import cn.com.chaochuang.pdf_operation.model.EntryListAdapter;
import cn.com.chaochuang.pdf_operation.ui.listener.OnClickItemListener;
import cn.com.chaochuang.pdf_operation.utils.Constants;
import cn.com.chaochuang.pdf_operation.utils.OkHttpUtil;

/**
 * 2020-4-14
 *
 * @author Shicx
 */

public class TextInputFragment extends DialogFragment {

    private Context context;
    private View inputView;
    private EditText editText;
    private String text,userName;
    private Button cancelButton,okButton;
    private ListView entryList;
    private OnClickItemListener onClickItemListener;
    private List<EntryData> entryDataList;
    private int maxWidth=1000;
    private int maxHeight=1600;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context=context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        inputView = inflater.inflate(R.layout.fg_text_input, container);

        return inputView;
    }

    @Override
    public void onResume() {
        super.onResume();

        if(getDialog().getWindow()!=null) {

            Display display = getDialog().getWindow().getWindowManager().getDefaultDisplay();
            DisplayMetrics outMetrics = new DisplayMetrics();
            display.getRealMetrics(outMetrics);

            int width = outMetrics.widthPixels;
            if(width>maxWidth){
                width = maxWidth;
            }

            getDialog().getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);

        }


    }

    @Override
    public void onStart() {
        super.onStart();
        setCancelable(false);
        editText = inputView.findViewById(R.id.et_text_input);

        cancelButton = inputView.findViewById(R.id.btn_setting_cancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String txt = editText.getText().toString();
                onClickItemListener.onCancelAction(txt);
                dismiss();
            }
        });

        okButton = inputView.findViewById(R.id.btn_setting_ok);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String txt = editText.getText().toString();
                if (txt.trim().length() == 0) {
                    Toast.makeText(context, "请输入内容", Toast.LENGTH_SHORT).show();
                    return;
                }
                onClickItemListener.onOkAction(txt);
                dismiss();
            }
        });

        editText.setText(this.text);
        if(this.text!=null) {
            editText.setSelection(this.text.length());
        }
        editText.setFocusable(true);
        editText.setFocusableInTouchMode(true);
        editText.requestFocus();
        InputMethodManager inputManager = (InputMethodManager) editText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);

        //词条列表
        entryList = inputView.findViewById(R.id.list_entry);

        if(entryDataList==null||entryDataList.size()==0){
            entryDataList = new ArrayList<>();
            EntryData entryData = new EntryData();
            entryData.setWord("同意");
            entryDataList.add(entryData);
            entryData = new EntryData();
            entryData.setWord("已阅");
            entryDataList.add(entryData);
        }
        entryList.setAdapter(new EntryListAdapter(context,R.layout.item_entry,entryDataList));
        entryList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                EntryData entryData = entryDataList.get(position);
                Editable editable = editText.getText();
                if(editable!=null){
                    editable.append(entryData.getWord());
                    editText.setText(editable);
                    editText.setSelection(editable.length());
                }
            }
        });

        inputManager.showSoftInput(editText, 0);
    }

    public void showFragmentDlg(String txt, String userName, android.support.v4.app.FragmentManager fragmentManager, String tag, List<EntryData> entryDataList){
        this.text=txt;
        this.userName=userName;
        this.show(fragmentManager,tag);
        this.entryDataList = entryDataList;
    }

    public void setOnClickItemListener(OnClickItemListener onClickItemListener) {
        this.onClickItemListener = onClickItemListener;
    }
}
