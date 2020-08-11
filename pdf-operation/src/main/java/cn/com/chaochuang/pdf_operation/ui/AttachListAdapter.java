package cn.com.chaochuang.pdf_operation.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.List;
import java.util.Map;

import cn.com.chaochuang.pdf_operation.R;
import cn.com.chaochuang.pdf_operation.model.AttachData;

/**
 * Created by Shicx on 2020/8/11.
 */
public class AttachListAdapter extends BaseAdapter {

    private List<AttachData> attachList;
    private LayoutInflater inflater;

    public AttachListAdapter(Context context,List<AttachData> attachList){
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
            viewHolder.iconView = convertView.findViewById(R.id.iv_icon);
            viewHolder.loadingView = convertView.findViewById(R.id.tv_loading);
            viewHolder.nameView = convertView.findViewById(R.id.tv_name);
            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.nameView.setText(attachList.get(position).getTrueName());
        return convertView;
    }

    public void updateView(){

    }

    class ViewHolder{
        private ImageView iconView;
        private TextView loadingView;
        private TextView nameView;
    }
}
