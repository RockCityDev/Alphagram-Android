package teleblock.manager;

import android.os.Handler;

import org.telegram.messenger.FileLoader;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import teleblock.model.TGCacheEntity;
import teleblock.model.TGCacheFindEntity;
import teleblock.util.SystemUtil;
import teleblock.util.sort.LastModifiedFileComparator;

/**
 * Created by LSD on 2021/9/29.
 * Desc
 */
public class TGCacheManager {
    Handler handler = new Handler();
    Map<Integer, TGCacheFindEntity> cacheMap = new HashMap<>();
    TGCacheListener listener;

    public interface TGCacheListener {
        void onFind(TGCacheEntity tgCacheEntity);

        void onFinish(Map<Integer, TGCacheFindEntity> cacheMap);
    }

    public void scanFile(TGCacheListener listener) {
        this.listener = listener;
        cacheMap = new HashMap<>();
        new Thread(() -> {
            String path = FileLoader.getTelegramDirectory().getAbsolutePath();
            findCache(path);
            handler.post(() -> {
                listener.onFinish(cacheMap);
            });
        }).start();
    }

    private void findCache(String dirPath) {
        File f = new File(dirPath);
        if (!f.exists()) return;
        File[] files = f.listFiles();
        if (files == null) return;
        Arrays.sort(files, LastModifiedFileComparator.LASTMODIFIED_REVERSE);//排序
        for (File file : files) {//遍历目录
            String extensionName = SystemUtil.getExtensionName(file.getName());
            extensionName = extensionName.toLowerCase();
            String name = file.getName();
            String path = file.getAbsolutePath();
            long size = file.length();
            long time = file.lastModified();
            if (file.isFile()) {
                TGCacheEntity entity = new TGCacheEntity();
                entity.name = name;
                entity.path = path;
                entity.size = size;
                entity.time = time;
                entity.setCacheType(extensionName);
                if (entity.type == -1) continue;//过滤这种文件
                TGCacheFindEntity findEntity = cacheMap.get(entity.type);
                if (findEntity == null) findEntity = new TGCacheFindEntity();
                findEntity.totalSize += size;
                findEntity.list.add(entity);
                cacheMap.put(entity.type, findEntity);
                handler.post(() -> listener.onFind(entity));
            } else if (file.isDirectory()) {//查询子目录
                findCache(file.getAbsolutePath());
            } else {
            }
        }
    }
}
