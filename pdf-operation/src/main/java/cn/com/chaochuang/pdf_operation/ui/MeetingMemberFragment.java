package cn.com.chaochuang.pdf_operation.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import cn.com.chaochuang.pdf_operation.R;
import cn.com.chaochuang.pdf_operation.model.MeetingMemberAdaptor;

/**
 * 2019-8-8
 *
 * @author Shicx
 */
public class MeetingMemberFragment extends DialogFragment {

    private MeetingMemberAdaptor meetingMemberAdaptor;
    private ListView listView;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(),AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
        builder.setNegativeButton("关闭", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setCancelable(false);
        setCancelable(false);

        View contentView = LayoutInflater.from(getActivity()).inflate(R.layout.fg_meeting_member, null);
        listView = contentView.findViewById(R.id.lv_member_list);
        listView.setAdapter(meetingMemberAdaptor);

        AlertDialog dialog = builder.create();
        dialog.setView(contentView);

        return dialog;
    }

    public void showFragmentDlg(FragmentManager fragmentManager, String tag, MeetingMemberAdaptor meetingMemberAdaptor){
        this.show(fragmentManager,tag);
        this.meetingMemberAdaptor = meetingMemberAdaptor;
    }
}
