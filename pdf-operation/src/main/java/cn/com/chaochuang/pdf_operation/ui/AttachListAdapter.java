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

/**
 * Created by Shicx on 2020/8/11.
 */
public class AttachListAdapter extends BaseAdapter {

    private List<DocAttachData> attachList;
    private LayoutInflater inflater;

    public AttachListAdapter(Context context,List<DocAttachData> attachList){
        this.attachList = attachList;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return attachList.size();
    }

    @Override
    public Object getItem(int position) {
        return attachList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if(convertView==null){
            convertView = inflater.inflate(R.layout.item_attach,null);
            viewHolder = new ViewHolder();
            viewHolder.tagView = convertView.findViewById(R.id.tv_tag_name);
            viewHolder.iconView = convertView.findViewById(R.id.iv_icon);
            viewHolder.loadingView = convertView.findViewById(R.id.tv_loading);
            viewHolder.nameView = convertView.findViewById(R.id.tv_name);
            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) convertView.getTag();
        }
        DocAttachData attachData = attachList.get(position);
        if(attachData.getTagName()!=null){
            viewHolder.tagView.setText(attachData.getTagName());
            viewHolder.iconView.setVisibility(View.GONE);
            viewHolder.nameView.setText("");
        }else{
            viewHolder.nameView.setText((position+1)+". "+attachData.getTrueName());
        }

        return convertView;
    }

    public void updateView(String progress, View view){
        ViewHolder viewHolder = (ViewHolder) view.getTag();
        viewHolder.loadingView.setText(progress);
    }

    class ViewHolder{
        private ImageView iconView;
        private TextView loadingView;
        private TextView tagView;
        private TextView nameView;
    }
}
