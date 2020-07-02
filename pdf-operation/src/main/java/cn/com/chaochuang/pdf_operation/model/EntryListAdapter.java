package cn.com.chaochuang.pdf_operation.model;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import cn.com.chaochuang.pdf_operation.R;

/**
 * 2020-7-1
 *
 * @author Shicx
 */
public class EntryListAdapter extends ArrayAdapter<EntryData> {

    private int resourceId;

    public EntryListAdapter(@NonNull Context context, int resource, List<EntryData> entryDataList) {
        super(context, resource,entryDataList);
        this.resourceId = resource;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        EntryData entryData = getItem(position);
        View view;
        ViewHolder viewHolder;
        if (convertView==null){

            view= LayoutInflater.from(getContext()).inflate(resourceId,parent,false);

            viewHolder=new ViewHolder();
            viewHolder.entryTxt=view.findViewById(R.id.item_entry_text);

            view.setTag(viewHolder);
        } else{
            view=convertView;
            viewHolder=(ViewHolder) view.getTag();
        }
        if(entryData!=null) {
            viewHolder.entryTxt.setText(entryData.getWord());
        }
        return view;
    }

    class ViewHolder{
        TextView entryTxt;
    }
}

