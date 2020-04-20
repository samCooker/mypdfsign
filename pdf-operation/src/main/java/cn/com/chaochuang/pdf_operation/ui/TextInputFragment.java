package cn.com.chaochuang.pdf_operation.ui;

import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Date;

import cn.com.chaochuang.pdf_operation.R;
import cn.com.chaochuang.pdf_operation.ui.listener.OnClickItemListener;
import cn.com.chaochuang.pdf_operation.utils.Constants;

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
    private Button appendButton;
    private OnClickItemListener onClickItemListener;

    private int maxWidth=1000;

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
            Point point = new Point();
            display.getSize(point);

            int width = point.x;
            if(point.x>maxWidth){
                width = maxWidth;
            }

            getDialog().getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
        }


    }

    @Override
    public void onStart() {
        super.onStart();

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

        appendButton = inputView.findViewById(R.id.txt_btn_append);
        appendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String append = editText.getText().toString() + "  " + userName + " " + Constants.DATA_FORMAT1.format(new Date());
                editText.setText(append);
                editText.setSelection(append.length());
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
        inputManager.showSoftInput(editText, 0);
    }

    public void showFragmentDlg(String txt,String userName,android.support.v4.app.FragmentManager fragmentManager, String tag){
        this.text=txt;
        this.userName=userName;
        this.show(fragmentManager,tag);
    }

    public void setOnClickItemListener(OnClickItemListener onClickItemListener) {
        this.onClickItemListener = onClickItemListener;
    }
}
