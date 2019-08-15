package cn.com.chaochuang.pdf_operation.model;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import cn.com.chaochuang.pdf_operation.R;

/**
 * 2018-5-9
 *
 * @author Shicx
 */

public class PenColorAdaptor extends BaseAdapter{

    private Context context;
    private final int[] penColors = new int[]{
            Color.BLACK,
            Color.GRAY,
            Color.RED,
            Color.GREEN,
            Color.BLUE,
            Color.WHITE
    };

    public PenColorAdaptor(Context mContext) {
        this.context = mContext;
    }

    @Override
    public int getCount() {
        return penColors.length;
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ImageView colorView;
        if(convertView == null){
            convertView = LayoutInflater.from(context).inflate(R.layout.item_pen, null);
        }
        colorView = (ImageView) convertView.findViewById(R.id.iv_pen_setting);
        colorView.setBackgroundColor(penColors[position]);

        return convertView;

    }

    public int getItemValue(int position){
        if(position>0&&position<penColors.length){
            return penColors[position];
        }
        return penColors[0];
    }
}
