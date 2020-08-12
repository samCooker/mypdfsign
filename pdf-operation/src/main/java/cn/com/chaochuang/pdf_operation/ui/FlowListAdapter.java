package cn.com.chaochuang.pdf_operation.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import cn.com.chaochuang.pdf_operation.R;
import cn.com.chaochuang.pdf_operation.model.DocAttachData;
import cn.com.chaochuang.pdf_operation.model.DocFlowData;

import static cn.com.chaochuang.pdf_operation.utils.Constants.DATA_FORMAT1;

/**
 * Created by Shicx on 2020/8/11.
 */
public class FlowListAdapter extends BaseAdapter {

    private List<DocFlowData> flowDataList;
    private LayoutInflater inflater;
    private Context context;

    public FlowListAdapter(Context context, List<DocFlowData> flowDataList){
        this.flowDataList = flowDataList;
        this.context = context;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return flowDataList.size();
    }

    @Override
    public Object getItem(int position) {
        return flowDataList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if(convertView==null){
            convertView = inflater.inflate(R.layout.item_flow,null);
            viewHolder = new ViewHolder();
            viewHolder.noView = convertView.findViewById(R.id.tv_no);
            viewHolder.nodeView = convertView.findViewById(R.id.tv_node_name);
            viewHolder.opinionView = convertView.findViewById(R.id.tv_opinion);
            viewHolder.userNameView = convertView.findViewById(R.id.tv_user_name);
            viewHolder.dateView = convertView.findViewById(R.id.tv_date);
            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) convertView.getTag();
        }
        DocFlowData flowData = flowDataList.get(position);
        String index = position+1+"";
        viewHolder.noView.setText(index);
        viewHolder.nodeView.setText(flowData.getNodeName());
        viewHolder.opinionView.setText(flowData.getOpinion());
        viewHolder.userNameView.setText(flowData.getDealerName());
        if(flowData.getDealTime()!=null) {
            viewHolder.nodeView.setBackgroundResource(R.drawable.shape_flow_node);
            viewHolder.dateView.setText(DATA_FORMAT1.format(flowData.getDealTime()));
            viewHolder.dateView.setTextColor(context.getResources().getColor(R.color.pdf_black));
        }else{
            viewHolder.nodeView.setBackgroundResource(R.drawable.shape_flow_node_active);
            viewHolder.dateView.setText("在办");
            viewHolder.dateView.setTextColor(context.getResources().getColor(R.color.pdf_red));
        }

        return convertView;
    }


    class ViewHolder{
        private TextView noView;
        private TextView nodeView;
        private TextView opinionView;
        private TextView userNameView;
        private TextView dateView;
    }
}
