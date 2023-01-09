package teleblock.util;

import com.google.android.exoplayer2.util.Log;

import teleblock.config.AppConfig;
import timber.log.Timber;


/**
 * Author:Perry
 * Time:2022/6/20
 * Description:app日志控制中心
 */
public class TGLog {

    private static String TAG;

    /**
     * 判断是否可以调试
     * @return
     */
    public static boolean isDebuggable() {
        return AppConfig.DEBUG;
    }

    /**
     * 打印一般信息
     * @param log
     */
    public static void verbose(String log) {
        showLog(0, log);
    }

    /**
     * 打印调试日志
     *
     * @param log
     */
    public static void debug(String log) {
        showLog(1, log);
    }

    public static void d(String tag, String log) {
        if (isDebuggable()) Log.d(tag, log);
    }

    /**
     * 打印信息
     *
     * @param log
     */
    public static void info(String log) {
        showLog(2, log);
    }

    /**
     * 打印警告
     *
     * @param log
     */
    public static void warning(String log) {
        showLog(3, log);
    }

    /**
     * 打印错误信息
     *
     * @param log
     */
    public static void erro(String log) {
        showLog(4, log);
    }

    /**
     * 打印严重错误信息，慎用！
     *
     * @param log
     */
    public static void wtf(String log) {
        showLog(5, log);
    }


    /**
     * 打印操作方法，
     *
     * @param code
     * @param log
     */
    private static void showLog(int code, String log) {
        switch (code) {
            case 0:
                Timber.v(log);
                break;
            case 1:
                Timber.d(log);
                break;
            case 2:
                Timber.i(log);
                break;
            case 3:
                Timber.w(log);
                break;
            case 4:
                Timber.e(log);
                break;
            case 5:
                Timber.wtf(log);
                break;

            default:
                break;
        }
    }
}
