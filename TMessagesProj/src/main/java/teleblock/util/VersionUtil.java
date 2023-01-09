package teleblock.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

/**
 * Time:2022/6/20
 * Author:Perry
 * Description：版本工具类
 */
public class VersionUtil {

    /**
     * 返回当前程序版本名称
     */
    public static String getAppVersionName(Context context) {
        String versionName = " ";
        try {
            PackageManager pm = context.getPackageManager();
            // 这里的context.getPackageName()可以换成你要查看的程序的包名
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
            versionName = pi.versionName;
            if (versionName == null || versionName.length() <= 0) {
                return "";
            }
        } catch (Exception e) {
            Log.e("VersionInfo", "Exception", e);
        }
        return versionName;
    }
}
