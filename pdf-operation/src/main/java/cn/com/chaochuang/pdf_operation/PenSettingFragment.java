package cn.com.chaochuang.pdf_operation;

import android.annotation.TargetApi;
import android.app.*;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import cn.com.chaochuang.pdf_operation.utils.PenColorAdaptor;

/**
 * 2018-5-9
 *
 * @author Shicx
 */

public class PenSettingFragment extends DialogFragment {

    public static final String PEN_WIDTH="penMaxSize";
    public static final String PEN_COLOR="penColor";
    public static final String PEN_ONLY="penOnly";
    public static final String PEN_SETTING_DATA = "pen_info";

    private SharedPreferences penSettingData;

    private View settingContent;
    private AlertDialog settingDialog;
    private Context context;

    private OnSaveListener onSaveListener;

    private PenColorAdaptor penColorAdaptor;

    private GridView penColorGv;
    private SeekBar penTypeBar;
    private TextView penWidthTv;
    private TextView previewWidthTv;
    private Switch penOnlySwitch;

    private int penMaxWidth = 30;
    private int penMinWidth = 1;
    public static int defaultWidth = 2;
    private float penWidth;
    private int penColor;
    private boolean penOnlyFlag;


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

        settingContent = LayoutInflater.from(getActivity()).inflate(R.layout.fg_pen_setting, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(),AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
        builder.setPositiveButton("保存",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SharedPreferences.Editor editor = penSettingData.edit();
                editor.putFloat(PEN_WIDTH,penWidth);
                editor.putInt(PEN_COLOR,penColor);
                editor.putBoolean(PEN_ONLY,penOnlyFlag);
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
            penSettingData = context.getSharedPreferences(PEN_SETTING_DATA, Context.MODE_PRIVATE);
        }

        return settingDialog;
    }

    @Override
    public void onStart() {
        super.onStart();

        if(penSettingData!=null) {
            penWidth = penSettingData.getFloat(PEN_WIDTH, defaultWidth);
            penColor = penSettingData.getInt(PEN_COLOR, Color.BLACK);
            penOnlyFlag = penSettingData.getBoolean(PEN_ONLY,true);
        }else{
            penWidth = defaultWidth;
            penColor = Color.BLACK;
        }

        previewWidthTv = settingContent.findViewById(R.id.tv_width_preview);
        final RelativeLayout.LayoutParams linearParams = (RelativeLayout.LayoutParams) previewWidthTv.getLayoutParams();
        linearParams.height = (int) (penWidth*settingContent.getResources().getDisplayMetrics().density);
        previewWidthTv.setLayoutParams(linearParams);
        previewWidthTv.setBackgroundColor(penColor);

        penOnlySwitch = settingContent.findViewById(R.id.sw_pen_only);
        penOnlySwitch.setChecked(penOnlyFlag);
        penOnlySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                penOnlyFlag = isChecked;
            }
        });

        penColorGv = settingContent.findViewById(R.id.gv_pen_color);
        penColorAdaptor = new PenColorAdaptor(context);
        penColorGv.setAdapter(penColorAdaptor);
        penColorGv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                penColor = penColorAdaptor.getItemValue(position);
                previewWidthTv.setBackgroundColor(penColor);
            }
        });

        penWidthTv = settingContent.findViewById(R.id.tv_pen_width);
        penTypeBar = settingContent.findViewById(R.id.sb_pen_width);
        penTypeBar.setProgress((int)penWidth);
        penWidthTv.setText("宽度：" +(penWidth));
        penTypeBar.setMax(penMaxWidth);
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            penTypeBar.setMin(penMinWidth);
        }
        penTypeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                previewWidthTv.setLayoutParams(linearParams);
                // 使设置好的布局参数应用到控件
                penWidth = progress+penMinWidth;
                linearParams.height = (int) (penWidth*settingContent.getResources().getDisplayMetrics().density);
                penWidthTv.setText("宽度：" +(progress+penMinWidth));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    public void showFragmentDlg(android.support.v4.app.FragmentManager fragmentManager, String tag, OnSaveListener onSaveListener){
        this.show(fragmentManager,tag);
        this.onSaveListener = onSaveListener;
    }

    public interface OnSaveListener{
        void onSaveAction();
    }
}
