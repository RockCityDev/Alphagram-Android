package teleblock.util;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.TrafficStats;
import android.net.Uri;
import android.os.Build;
import android.telephony.TelephonyManager;

import org.telegram.messenger.MessagesController;
import org.telegram.messenger.UserConfig;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Time:2022/6/29
 * Author:Perry
 * Description：系统工具类
 */
public class SystemUtil {

    /**
     * 获取设备唯一id
     * @return
     */
    public static String getUniquePsuedoID() {
        String serial = null;

        String m_szDevIDShort = "35" +
                Build.BOARD.length() % 10 + Build.BRAND.length() % 10 +

                Build.CPU_ABI.length() % 10 + Build.DEVICE.length() % 10 +

                Build.DISPLAY.length() % 10 + Build.HOST.length() % 10 +

                Build.ID.length() % 10 + Build.MANUFACTURER.length() % 10 +

                Build.MODEL.length() % 10 + Build.PRODUCT.length() % 10 +

                Build.TAGS.length() % 10 + Build.TYPE.length() % 10 +

                Build.USER.length() % 10; //13 位

        try {
            serial = android.os.Build.class.getField("SERIAL").get(null).toString();
            //API>=9 使用serial号
            return new UUID(m_szDevIDShort.hashCode(), serial.hashCode()).toString();
        } catch (Exception exception) {
            //serial需要一个初始化
            serial = "serial"; // 随便一个初始化
        }
        //使用硬件信息拼凑出来的15位号码
        return new UUID(m_szDevIDShort.hashCode(), serial.hashCode()).toString();
    }

    /**
     * 获取国家代码
     * @param context
     * @return
     */
    public static String getCountryZipCode(Context context) {
        String CountryZipCode;
        Locale locale = context.getResources().getConfiguration().locale;
        CountryZipCode = locale.getCountry();
        return CountryZipCode;
    }

    /**
     * 获取SIM卡代码
     * @param context
     * @return
     */
    public static String getTelContry(Context context) {
        TelephonyManager teleMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (teleMgr != null) {
            String countryISOCode = teleMgr.getSimCountryIso();
            return countryISOCode;
        }
        return "";
    }

    public static String getExtensionName(String filename) {
        if ((filename != null) && (filename.length() > 0)) {
            int dot = filename.lastIndexOf('.');
            if ((dot > -1) && (dot < (filename.length() - 1))) {
                return filename.substring(dot + 1);
            }
        }
        return "";
    }

    public static String getFileName(String fileUrl) {
        if ((fileUrl != null) && (fileUrl.length() > 0)) {
            int separator = fileUrl.lastIndexOf('/');
            if ((separator > -1) && (separator < (fileUrl.length() - 1))) {
                return fileUrl.substring(separator + 1);
            }
        }
        return "";
    }

    public static File simpleCopyFile(Context context, String oldPath, String newPath) {
        try {
            File outFile = new File(newPath);
            if (outFile.exists()) outFile.delete();
            copyFile(new File(oldPath), outFile);
            return outFile;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void copyFile(File file, File newFile) throws IOException {
        Throwable th;
        Throwable th2;
        if (!newFile.getParentFile().exists()) {
            newFile.getParentFile().mkdirs();
        }
        if (!newFile.exists()) {
            newFile.createNewFile();
        }
        FileChannel fileChannel = null;
        FileChannel fileChannel2 = null;
        FileChannel channel;
        try {
            channel = new FileInputStream(file).getChannel();
            try {
                fileChannel = new FileOutputStream(newFile).getChannel();
            } catch (Throwable th3) {
                th = th3;
                if (channel != null) {
                    channel.close();
                }
                if (fileChannel2 != null) {
                    fileChannel2.close();
                }
                throw th;
            }
            try {
                fileChannel.transferFrom(channel, (long) 0, channel.size());
                if (channel != null) {
                    channel.close();
                }
                if (fileChannel != null) {
                    fileChannel.close();
                }
            } catch (Throwable th4) {
                th2 = th4;
                fileChannel2 = fileChannel;
                th = th2;
                if (channel != null) {
                    channel.close();
                }
                if (fileChannel2 != null) {
                    fileChannel2.close();
                }
                throw th;
            }
        } catch (Throwable th5) {
            th2 = th5;
            channel = fileChannel;
            th = th2;
            if (channel != null) {
                channel.close();
            }
            if (fileChannel2 != null) {
                fileChannel2.close();
            }
        }
    }
    public static String getSizeFormat(long sise) {
        String unit = "KB";
        float formatSize = sise / 1024.0f;//kb
        if (formatSize > 1024) {
            unit = "MB";
            formatSize = formatSize / 1024.0f;//mb
        }
        if (formatSize > 1024) {
            formatSize = formatSize / 1024.0f;//gb
            unit = "GB";
        }
        return String.format("%.1f", formatSize) + unit;
    }

    public static void notifyFileScan(Context context, File file) {
        if (file == null) return;
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        intent.setData(Uri.fromFile(file));
        context.sendBroadcast(intent);
    }

    public static boolean deleteFile(String filePath) {
        try {
            File file = new File(filePath);
            if (file.exists()) {
                return file.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static String timeTransfer(int time) {
        if (time <= 0) return "00:00";
        String h = String.format("%02d", time / 3600);
        String m = String.format("%02d", (time - Integer.parseInt(h) * 3600) / 60);
        String s = String.format("%02d", (time - Integer.parseInt(h) * 3600) % 60);
        String str = m + ":" + s;
        if (!h.equals("00")) {
            str = h + ":" + str;
        }
        return str;
    }

    public static float getDownloadSpeed(int id, long downloadSize) {
        float speed;
        SharedPreferences g_preferences = MessagesController.getMainSettings(UserConfig.selectedAccount);
        long lastSize = g_preferences.getLong("downLoad_" + id, 0l);
        long lastTime = g_preferences.getLong("time_" + id, 0l);

        long now = System.currentTimeMillis();
        speed = (float) (((downloadSize - lastSize) / 1024.0) * 1000 / ((now - lastTime)));//秒转换

        g_preferences.edit().putLong("downLoad_" + id, downloadSize).commit();
        g_preferences.edit().putLong("time_" + id, now).commit();
        if (speed <= 0) speed = 0;
        return speed;
    }

    private static long lastTotalRxBytes = 0;
    private static long lastTimeStamp = 0;
    public static long getNetSpeed() {
        long nowTotalRxBytes = getTotalRxBytes();
        long nowTimeStamp = System.currentTimeMillis();
        long offset = nowTimeStamp - lastTimeStamp;
        if (offset == 0) offset = 1000;
        long speed = (nowTotalRxBytes - lastTotalRxBytes) * 1000 / offset;//秒转换
        if (speed <= 0) speed = 0;
        lastTimeStamp = nowTimeStamp;
        lastTotalRxBytes = nowTotalRxBytes;
        return speed;
    }

    private static long getTotalRxBytes() {
        return TrafficStats.getTotalRxBytes() == TrafficStats.UNSUPPORTED ? 0 : (TrafficStats.getTotalRxBytes() / 1024);//转为KB
    }

    public static boolean isContainChinese(String str) {
        Pattern p = Pattern.compile("[\u4e00-\u9fa5]");
        Matcher m = p.matcher(str);
        if (m.find()) {
            return true;
        }
        return false;
    }

    public static boolean isNumber(String str) {
        Pattern p = Pattern.compile("^[0-9]*$");
        Matcher m = p.matcher(str);
        if (m.find()) {
            return true;
        }
        return false;
    }
}
