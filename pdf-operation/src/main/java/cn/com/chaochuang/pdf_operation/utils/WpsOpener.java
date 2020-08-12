package cn.com.chaochuang.pdf_operation.utils;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.FileProvider;
import android.widget.Toast;

import java.io.File;

/**
 * Created by Shicx on 2020/8/12.
 */
public class WpsOpener {

    public static void wpsOpenFile(Context context, File file, String userName) {
        String openModel = WpsModelUtil.OpenMode.READ_MODE;
        if (!file.exists()) {
            Toast.makeText(context,"文件不存在",Toast.LENGTH_SHORT).show();
        } else if (checkPackage(context, WpsModelUtil.PackageName.NORMAL)) {

            Intent intent = new Intent();
            Bundle bundle = new Bundle();
            bundle.putString(WpsModelUtil.OPEN_MODE, openModel); // 打开模式
            bundle.putString(WpsModelUtil.USER_NAME, userName); // 修改人
            bundle.putBoolean(WpsModelUtil.SEND_CLOSE_BROAD, true); // 关闭时是否发送广播
            bundle.putBoolean(WpsModelUtil.SEND_SAVE_BROAD, true); // 关闭时是否发送广播
            bundle.putString(WpsModelUtil.THIRD_PACKAGE, context.getPackageName()); // 第三方应用的包名，用于对改应用合法性的验证
            bundle.putBoolean(WpsModelUtil.CLEAR_TRACE, true);// 清除打开记录
            bundle.putBoolean(WpsModelUtil.CLEAR_BUFFER, true);// 清除打开记录
            bundle.putBoolean(WpsModelUtil.ENTER_REVISE_MODE, false);//进入修订模式
            // bundle.putBoolean(CLEAR_FILE, true); //关闭后删除打开文件
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setAction(Intent.ACTION_VIEW);
            intent.setClassName(WpsModelUtil.PackageName.NORMAL, WpsModelUtil.ClassName.NORMAL);

            if (!file.exists()) {
                Toast.makeText(context,"无法打开文件",Toast.LENGTH_SHORT).show();
            } else {
                Uri path;
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                    path = Uri.fromFile(file);
                } else {
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    path = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", file);
                }
                intent.setData(path);
                intent.putExtras(bundle);
                try {
                    context.startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    e.printStackTrace();
                }
            }

        } else {
            Toast.makeText(context,"没有安装wps，请先下载安装",Toast.LENGTH_SHORT).show();
        }
    }

    public static void wpsOpenFile(Context context, String filePath, String userName) {
        File file = new File(filePath);
        wpsOpenFile(context,file,userName);
    }

    // 检测该包名所对应的应用是否存在
    public static boolean checkPackage(Context context, String packageName) {
        if (packageName == null || "".equals(packageName))
            return false;
        PackageInfo packageinfo = null;
        try {
            packageinfo = context.getPackageManager().getPackageInfo(packageName, PackageManager.GET_UNINSTALLED_PACKAGES);
        } catch (PackageManager.NameNotFoundException e) {

        }
        return packageinfo!=null;
    }

}
