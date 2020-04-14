package cn.com.chaochuang.pdf_operation.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import cn.com.chaochuang.pdf_operation.R;
import cn.com.chaochuang.pdf_operation.ui.listener.OnClickItemListener;

/**
 * 2020-4-14
 *
 * @author Shicx
 */

public class TextInputFragment extends DialogFragment {

    private Context context;
    private View inputView;
    private EditText editText;
    private Button cancelButton,okButton;
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

        inputView =inflater.inflate(R.layout.fg_text_input,container);

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
                dismiss();
            }
        });
        okButton = inputView.findViewById(R.id.btn_setting_ok);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String txt = editText.getText().toString();
                if(txt.trim().length()==0){
                    Toast.makeText(context,"请输入内容",Toast.LENGTH_SHORT).show();
                    return;
                }
                onClickItemListener.onOkAction(txt);
                dismiss();
            }
        });
    }

    public void showFragmentDlg(String txt,android.support.v4.app.FragmentManager fragmentManager, String tag){
        if(editText!=null){
            editText.setFocusable(true);
            if(txt!=null) {
                editText.setText(txt);
                editText.setSelection(txt.length());
                editText.requestFocus();
            }
        }
        this.show(fragmentManager,tag);
    }

    public void setOnClickItemListener(OnClickItemListener onClickItemListener) {
        this.onClickItemListener = onClickItemListener;
    }
}
