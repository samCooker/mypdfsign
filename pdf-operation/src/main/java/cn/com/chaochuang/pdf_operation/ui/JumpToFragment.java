package cn.com.chaochuang.pdf_operation.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.NumberPicker;
import cn.com.chaochuang.pdf_operation.R;

/**
 * 2019-8-14
 *
 * @author Shicx
 */
public class JumpToFragment extends DialogFragment {

    private NumberPicker pageNoPicker;
    private Integer currentPage;
    private Integer maxPage;
    private OnJumpToListener onJumpToListener;

    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(),AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
        builder.setNegativeButton("关闭", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setPositiveButton("跳转", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(onJumpToListener!=null){
                    int jumpToNo = pageNoPicker.getValue();
                    onJumpToListener.jumpTo(jumpToNo);
                }
            }
        });
        builder.setCancelable(false);
        setCancelable(false);

        View contentView = LayoutInflater.from(getActivity()).inflate(R.layout.fg_jump_to, null);
        pageNoPicker = contentView.findViewById(R.id.np_page_no);
        pageNoPicker.setMinValue(1);
        pageNoPicker.setMaxValue(maxPage);
        pageNoPicker.setValue(currentPage);
        AlertDialog dialog = builder.create();
        dialog.setView(contentView);

        return dialog;
    }

    public void showFragmentDlg(android.support.v4.app.FragmentManager fragmentManager, String tag, int pageNo, int maxPage){
        this.show(fragmentManager,tag);
        this.currentPage = pageNo;
        this.maxPage = maxPage;
    }

    public void setOnJumpToListener(OnJumpToListener onJumpToListener) {
        this.onJumpToListener = onJumpToListener;
    }

    public interface OnJumpToListener{
        void jumpTo(int page);
    }
}
