package cn.com.chaochuang.pdf_operation.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import cn.com.chaochuang.pdf_operation.R;
import cn.com.chaochuang.writingpen.model.CommentData;

/**
 * 2018-5-8
 *
 * @author Shicx
 */

public class TextInputFragment extends DialogFragment {

    private View textViewContent;
    private EditText editText;
    private TextView userNameTv;
    private AlertDialog textDialog;
    private OnSaveListener saveListener;
    private CommentData commentData;
    private Context context;
    private boolean readOnly;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        textViewContent = LayoutInflater.from(getActivity()).inflate(R.layout.fg_text_input, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
        if(!this.readOnly){
            builder.setPositiveButton("保存",new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if(saveListener!=null&&editText.getText()!=null){
                        commentData.setSignContent(editText.getText().toString());
                        saveListener.onTextDataSave(commentData);
                    }
                }
            }).setNegativeButton("取消",new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    textDialog.dismiss();
                }
            });
        }else{
            builder.setNegativeButton("关闭",new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    textDialog.dismiss();
                }
            });
        }
        textDialog = builder.create();
        textDialog.setCancelable(false);
        setCancelable(false);
        textDialog.setView(textViewContent);
        textDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                initEditText();
            }
        });

        editText = textViewContent.findViewById(R.id.et_text_input);
        if(this.readOnly){
            editText.setCursorVisible(false);
            editText.setFocusable(false);
            editText.setFocusableInTouchMode(false);
        }
        userNameTv = textViewContent.findViewById(R.id.tv_user_show);
        if(this.commentData !=null&&this.commentData.getSignerName()!=null){
            this.userNameTv.setText(this.commentData.getSignerName());
        }
        return textDialog;
    }

    public void setOnSaveListener(OnSaveListener saveListener) {
        this.saveListener = saveListener;
    }

    public void showInputFragment(CommentData commentData, FragmentManager fragmentManager, String tag, boolean readOnly) {
        this.commentData = commentData;
        this.readOnly = readOnly;
        this.show(fragmentManager,tag);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    /**
     * 初始化输入框
     */
    private void initEditText() {
        if(commentData !=null&& commentData.getSignContent()!=null){
            editText.setText(commentData.getSignContent());
            editText.setSelection(commentData.getSignContent().length());
        }
    }

    /**
     * 保存事件通知
     *
     *
     */
    public interface OnSaveListener {

        void onTextDataSave(CommentData textData);

    }
}
