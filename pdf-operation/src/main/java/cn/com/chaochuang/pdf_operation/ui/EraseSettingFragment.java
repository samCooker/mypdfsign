package cn.com.chaochuang.pdf_operation.ui;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
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

public class EraseSettingFragment extends DialogFragment {


    public static final String ERASE_WIDTH="penMaxSize";

    private SharedPreferences penSettingData;

    private View settingContent;
    private AlertDialog settingDialog;
    private Context context;

    private OnSaveListener onSaveListener;

    private SeekBar penTypeBar;
    private TextView penWidthTv;
    private PenWidthView penWidthView;

    private int penMaxWidth = 80;
    private int penMinWidth = 10;
    public static int defaultWidth = 20;
    private float eraseWidth = defaultWidth;


    @TargetApi(23)
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        onAttachToContext(context);
    }

    /*
     * Deprecated on API 23
     * Use onAttachToContext instead
     */
    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            onAttachToContext(activity);
        }
    }

    /*
     * Called when the fragment attaches to the context
     */
    protected void onAttachToContext(Context context) {
        this.context=context;
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        settingContent = LayoutInflater.from(getActivity()).inflate(R.layout.fg_erase_setting, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(),AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
        builder.setPositiveButton("保存",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SharedPreferences.Editor editor = penSettingData.edit();
                editor.putFloat(ERASE_WIDTH,eraseWidth);
                if(editor.commit()&&onSaveListener!=null){
                    onSaveListener.onSaveAction();
                }else{
                    Toast.makeText(context, "设置出现了错误", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("取消",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                settingDialog.dismiss();
            }
        });
        settingDialog = builder.create();
        settingDialog.setCancelable(false);
        setCancelable(false);
        settingDialog.setView(settingContent);
        settingDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {

            }
        });

        if(context!=null) {
            penSettingData = context.getSharedPreferences(PenSettingFragment.PEN_SETTING_DATA, Context.MODE_PRIVATE);
        }

        return settingDialog;
    }

    @Override
    public void onStart() {
        super.onStart();

        if(penSettingData!=null) {
            eraseWidth = penSettingData.getFloat(ERASE_WIDTH, defaultWidth);
        }else{
            eraseWidth = defaultWidth;
        }

        penWidthView = settingContent.findViewById(R.id.tv_erase_width);
        penWidthView.setPenConfig(eraseWidth,Color.BLACK);

        penWidthTv = settingContent.findViewById(R.id.eraser_pen_tv);
        penTypeBar = settingContent.findViewById(R.id.eraser_pen_sb);
        penTypeBar.setProgress((int)eraseWidth);
        penWidthTv.setText("宽度：" +(eraseWidth));
        penTypeBar.setMax(penMaxWidth);
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            penTypeBar.setMin(penMinWidth);
        }
        penTypeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                eraseWidth = progress;
                penWidthTv.setText("宽度：" +(eraseWidth));
                penWidthView.setPenConfig(eraseWidth,Color.BLACK);

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        setSeekBarWidth(penMinWidth);
    }

    private void setSeekBarWidth(int width) {
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            penTypeBar.setMin(width);
        }
        if(eraseWidth<width){
            eraseWidth = width;
        }
        penTypeBar.setProgress((int)eraseWidth);
        penWidthTv.setText("宽度：" +(eraseWidth));
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
