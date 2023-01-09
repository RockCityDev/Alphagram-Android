package teleblock.util;

import org.telegram.messenger.Utilities;

import java.io.File;

/**
 * Time:2022/7/5
 * Author:Perry
 * Description：缓存工具类
 */
public class CacheUtil {

    /**
     * 获取文件夹大小
     * @param dir
     * @param documentsMusicType
     * @return
     */
    public static long getDirectorySize(File dir, int documentsMusicType) {
        if (dir == null) {
            return 0;
        }
        long size = 0;
        if (dir.isDirectory()) {
            size = Utilities.getDirSize(dir.getAbsolutePath(), documentsMusicType, false);
        } else if (dir.isFile()) {
            size += dir.length();
        }
        return size;
    }
}
