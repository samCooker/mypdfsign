package cn.com.chaochuang.pdf_operation.model;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import cn.com.chaochuang.pdf_operation.R;

import java.util.List;

/**
 * 2019-8-8
 *
 * @author Shicx
 */
public class MeetingMemberAdaptor extends BaseAdapter {

    private Context context;
    private List<MeetingMemberData> memberDataList;

    public MeetingMemberAdaptor(Context context,List<MeetingMemberData> memberDataList) {
        this.memberDataList = memberDataList;
        this.context = context;
    }

    @Override
    public int getCount() {
        return memberDataList.size();
    }

    @Override
    public Object getItem(int position) {
        return memberDataList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView==null){
            convertView = LayoutInflater.from(context).inflate(R.layout.item_meeting_member, null);
        }
        TextView nameView = convertView.findViewById(R.id.tv_member_name);
        TextView noView = convertView.findViewById(R.id.tv_member_no);

        MeetingMemberData memberData = memberDataList.get(position);
        nameView.setText(memberData.getUname());
        String no = position+1+"";
        noView.setText(no);
        return convertView;
    }
}
