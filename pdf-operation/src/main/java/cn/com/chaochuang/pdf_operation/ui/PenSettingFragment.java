package cn.com.chaochuang.pdf_operation.ui;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import cn.com.chaochuang.pdf_operation.R;
import cn.com.chaochuang.pdf_operation.model.PenColorAdaptor;

/**
 * 2018-5-9
 *
 * @author Shicx
 */

public class PenSettingFragment extends DialogFragment {

    /**
     * 钢笔
     */
    public static int STROKE_TYPE_PEN = 1;

    public static final String PEN_WIDTH="penMaxSize";
    public static final String PEN_COLOR="penColor";
    public static final String PEN_TYPE="penType";
    public static final String PEN_SETTING_DATA = "pen_info";

    private SharedPreferences penSettingData;

    private View settingView;
    private Context context;

    private OnSaveListener onSaveListener;

    private PenColorAdaptor penColorAdaptor;

    private GridView penColorGv;

    private SeekBar penTypeBar;
    private TextView penWidthTv;
    private PenWidthView penWidthView;
    private Button cancelButton,okButton;

    private int penMaxWidth = 80;
    private int penMinWidth = 2;
    public static int defaultWidth = 12;
    private float penWidth;
    private int penColor;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context=context;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if(context!=null) {
            penSettingData = context.getSharedPreferences(PEN_SETTING_DATA, Context.MODE_PRIVATE);
        }
        settingView = inflater.inflate(R.layout.fg_pen_setting,container);
        return settingView;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return super.onCreateDialog(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();

        if(penSettingData!=null) {
            penWidth = penSettingData.getFloat(PEN_WIDTH, defaultWidth);
            penColor = penSettingData.getInt(PEN_COLOR, Color.BLACK);
        }else{
            penWidth = defaultWidth;
            penColor = Color.BLACK;
        }

        penWidthView = settingView.findViewById(R.id.tv_width_preview);
        penWidthView.setPenConfig(penWidth,penColor);

        penColorGv = settingView.findViewById(R.id.gv_pen_color);
        penColorAdaptor = new PenColorAdaptor(context);
        penColorGv.setAdapter(penColorAdaptor);
        penColorGv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                penColor = penColorAdaptor.getItemValue(position);
                penWidthView.setPenConfig(penWidth,penColor);
            }
        });

        penWidthTv = settingView.findViewById(R.id.tv_pen_width);
        penTypeBar = settingView.findViewById(R.id.sb_pen_width);
        penTypeBar.setProgress((int)penWidth);
        penWidthTv.setText("宽度：" +(penWidth));
        penTypeBar.setMax(penMaxWidth);
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            penTypeBar.setMin(penMinWidth);
        }
        penTypeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                penWidth = progress;
                penWidthTv.setText("宽度：" +(penWidth));
                penWidthView.setPenConfig(penWidth,penColor);

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        cancelButton = settingView.findViewById(R.id.btn_setting_cancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        okButton = settingView.findViewById(R.id.btn_setting_ok);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = penSettingData.edit();
                editor.putFloat(PEN_WIDTH,penWidth);
                editor.putInt(PEN_COLOR,penColor);
                if(editor.commit()&&onSaveListener!=null){
                    onSaveListener.onSaveAction();
                    dismiss();
                }else{
                    Toast.makeText(context, "设置出现了错误", Toast.LENGTH_SHORT).show();
                }
            }
        });

        setSeekBarWidth(penMinWidth);
    }

    private void setSeekBarWidth(int width) {
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            penTypeBar.setMin(width);
        }
        if(penWidth<width){
            penWidth = width;
        }
        penTypeBar.setProgress((int)penWidth);
        penWidthTv.setText("宽度：" +(penWidth));
    }

    public void showFragmentDlg(android.support.v4.app.FragmentManager fragmentManager, String tag){
        this.show(fragmentManager,tag);
    }

    public void setOnSaveListener(OnSaveListener onSaveListener) {
        this.onSaveListener = onSaveListener;
    }

    public interface OnSaveListener{
        void onSaveAction();
    }
}
